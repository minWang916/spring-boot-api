package com.kms.utils.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.kms.domain.user.User;
import com.kms.domain.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class JWTUtils {
  private final Algorithm algorithm;
  private final String BLACKLIST_SET;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final RedisTemplate<String, String> redisTemplate;

  public enum TokenValidationResult {
    VALID,
    INVALID_SIGNATURE,
    INVALID_PAYLOAD,
    EXPIRED
  }

  public JWTUtils(
      @Value("${jwt.secret}") String secretKey,
      @Value("${jwt.blacklistTokensSetName}") String BLACKLIST_SET,
      @Value("${jwt.expirationMs}") String accessTokenExpiration,
      @Value("${jwt.refreshExpirationMs}") String refreshTokenExpiration,
      UserRepository userRepository,
      RefreshTokenRepository refreshTokenRepository,
      RedisTemplate<String, String> redisTemplate) {
    this.algorithm = Algorithm.HMAC256(secretKey.getBytes());
    this.refreshTokenRepository = refreshTokenRepository;
    this.accessTokenExpiration = Long.parseLong(accessTokenExpiration);
    this.refreshTokenExpiration = Long.parseLong(refreshTokenExpiration);
    this.userRepository = userRepository;
    this.redisTemplate = redisTemplate;
    this.BLACKLIST_SET = BLACKLIST_SET;
  }

  // Generate Access Token
  public String generateAccessToken(String username, Map<String, String> claims) {
    return JWT.create()
        .withSubject(username)
        .withIssuedAt(new Date())
        .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
        .withPayload(claims)
        .sign(algorithm);
  }

  // Generate Refresh Token
  public String generateRefreshToken(String username) {
    // Save the new refresh token in the database
    RefreshToken refreshToken = new RefreshToken();
    String refreshTokenString =
        JWT.create()
            .withSubject(username)
            .withIssuedAt(new Date())
            .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpiration))
            .sign(algorithm);

    Optional<User> optionalUser = userRepository.findByUsername(username);
    if (optionalUser.isEmpty()) {
      throw new RuntimeException("User not found");
    }
    User user = optionalUser.get();
    refreshToken.setToken(refreshTokenString);
    refreshToken.setUser(user);
    refreshToken.setExpiryDate(
        new Date(System.currentTimeMillis() + refreshTokenExpiration).toInstant());

    refreshTokenRepository.save(refreshToken);

    return refreshTokenString;
  }

  // Validate Token
  public TokenValidationResult validateToken(String token) {
    try {
      JWTVerifier verifier = JWT.require(algorithm).build();
      verifier.verify(token);
      return TokenValidationResult.VALID;
    } catch (SignatureVerificationException e) {
      return TokenValidationResult.INVALID_SIGNATURE;
    } catch (TokenExpiredException e) {
      return TokenValidationResult.EXPIRED;
    } catch (JWTVerificationException e) {
      return TokenValidationResult.INVALID_PAYLOAD;
    }
  }

  // Refresh Access Token using a valid Refresh Token
  public Map<String, String> refreshToken(String refreshToken) {
    RefreshToken token =
        refreshTokenRepository
            .findByToken(refreshToken)
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

    if (token.getExpiryDate().isBefore(Instant.now())) {
      refreshTokenRepository.delete(token);
      throw new IllegalStateException("Refresh token is expired");
    }

    User user = token.getUser();
    Map<String, String> claims =
        Map.of(
            "email", user.getEmail(),
            "userId", String.valueOf(user.getId()));
    String newToken = generateAccessToken(user.getUsername(), claims);
    return Map.of("token", newToken);
  }

  // Blacklist a token when user logout
  public void blacklistToken(int userId, String token, long expiryDuration) {
    redisTemplate
        .opsForValue()
        .set(String.valueOf(userId), token, expiryDuration, TimeUnit.MILLISECONDS);
  }

  // Check if an access token is blacklisted
  public boolean isTokenBlacklisted(String userId, String token) {
    String blacklistedToken = redisTemplate.opsForValue().get(userId);
    return token.equals(blacklistedToken);
  }

  // Method to get the expiry duration in milliseconds
  public long getExpiryDuration(String token) {
    DecodedJWT decodedJWT = JWT.decode(token);
    Instant expirationTime = decodedJWT.getExpiresAt().toInstant();
    Instant currentTime = Instant.now();

    long expiryDuration = expirationTime.toEpochMilli() - currentTime.toEpochMilli();

    if (expiryDuration < 0) {
      expiryDuration = 0;
    }

    return expiryDuration;
  }

  public String getUserIdFromToken() {
    HttpServletRequest request =
        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    String token = request.getHeader("Authorization").substring(7); // Remove "Bearer " prefix
    DecodedJWT decodedJWT = JWT.decode(token);

    return decodedJWT.getClaim("userId").asString();
  }
}
