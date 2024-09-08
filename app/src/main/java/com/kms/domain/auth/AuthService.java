package com.kms.domain.auth;

import com.kms.domain.auth.dto.LoginRequest;
import com.kms.domain.auth.dto.LoginResponse;
import com.kms.domain.auth.dto.LogoutRequest;
import com.kms.domain.auth.dto.RegisterRequest;
import com.kms.domain.user.User;
import com.kms.domain.user.UserRepository;
import com.kms.utils.jwt.JWTUtils;
import com.kms.utils.jwt.RefreshToken;
import com.kms.utils.jwt.RefreshTokenRepository;
import com.kms.utils.mail.MailUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final MailUtils mailUtils;
  private final JWTUtils jwtUtils;

  public Map<String, String> registerUser(
      RegisterRequest registerRequest, HttpServletRequest request) {
    User user = registerRequest.toUser();

    if (userRepository.existsByUsername(user.getUsername())) {
      logger.debug("Username {} already exists", user.getUsername());
      throw new IllegalArgumentException("Username " + user.getUsername() + " already exists");
    }

    if (userRepository.existsByEmail(user.getEmail())) {
      logger.debug("Email {} already exists", user.getEmail());
      throw new IllegalArgumentException("Email " + user.getEmail() + " already exists");
    }

    // Send verification code
    String verificationCode = UUID.randomUUID().toString();
    user.setVerificationCode(verificationCode);
    user.setEnabled(false);

    // Encrypt password with bcrypt
    String encodedPassword = passwordEncoder.encode(user.getPassword());
    user.setPassword(encodedPassword);

    userRepository.save(user);
    mailUtils.sendVerificationEmail(user, request);

    logger.debug(
        "User with email {} registered successfully and is waiting for verification",
        registerRequest.getEmail());

    return Map.of(
        "message",
        "User registered successfully. A verification email has been sent, please confirm.");
  }

  public Map<String, String> verifyUser(String verificationCode) {
    Optional<User> optionalUser = userRepository.findByVerificationCode(verificationCode);

    if (optionalUser.isEmpty()) {
      logger.debug("Verification code {} is invalid", verificationCode);
      throw new IllegalArgumentException("Verification code is invalid.");
    }

    User user = optionalUser.get();
    if (user.isEnabled()) {
      logger.debug("User {} is already verified", user.getUsername());
      throw new IllegalStateException("User is already verified.");
    }

    user.setVerificationCode(null);
    user.setEnabled(true);
    userRepository.save(user);

    logger.debug("User with email {} verified successfully", user.getEmail());

    return Map.of("message", "User verified successfully.");
  }

  public LoginResponse loginUser(LoginRequest loginRequest) {
    Optional<User> optionalUser = userRepository.findByUsername(loginRequest.getUsername());

    if (optionalUser.isEmpty()) {
      logger.debug("User {} not found", loginRequest.getUsername());
      throw new NoSuchElementException("User not found");
    }

    User user = optionalUser.get();
    if (!user.isEnabled()) {
      logger.debug("User {} is not verified yet", loginRequest.getUsername());
      throw new IllegalStateException("User is not verified yet.");
    }

    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
      logger.debug("Invalid login credentials for user {}", loginRequest.getUsername());
      throw new BadCredentialsException("Invalid credentials.");
    }

    // Check for existing refresh token
    Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByUserId(user.getId());
    optionalRefreshToken.ifPresent(refreshTokenRepository::delete);

    // Generate jwt access and refresh token
    Map<String, String> claims =
        Map.of(
            "email", user.getEmail(),
            "userId", String.valueOf(user.getId()));
    String message = "User logged in successfully.";
    String token = jwtUtils.generateAccessToken(user.getUsername(), claims);
    String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

    logger.debug("User with email {} logged in successfully", user.getEmail());

    return new LoginResponse(message, token, refreshToken);
  }

  public Map<String, String> logoutUser(LogoutRequest logoutRequest) {
    // Handle userId validation
    int userId = Integer.parseInt(logoutRequest.getUserId());
    boolean userExists = userRepository.existsById(userId);
    if (!userExists) {
      logger.debug("User with id {} not found", userId);
      throw new NoSuchElementException("User ID not found.");
    }

    // Blacklist the current JWT token
    String token = logoutRequest.getToken();
    if (jwtUtils.validateToken(token) == JWTUtils.TokenValidationResult.VALID) {
      long expiryDuration = jwtUtils.getExpiryDuration(token);
      jwtUtils.blacklistToken(userId, token, expiryDuration);
    } else {
      throw new IllegalArgumentException("Invalid or expired JWT token.");
    }

    // Remove the refresh token associated with the userId from the database
    Optional<RefreshToken> refreshTokenOptional =
        refreshTokenRepository.findByUserId(Integer.parseInt(logoutRequest.getUserId()));
    if (refreshTokenOptional.isPresent()) {
      refreshTokenRepository.delete(refreshTokenOptional.get());
    } else {
      throw new NoSuchElementException("Refresh token not found.");
    }

    logger.debug("User with id {} logged out successfully", userId);

    return Map.of("message", "Logout successful");
  }
}
