package com.kms.utils.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kms.domain.user.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JWTUtils jwtUtils;

  public JwtAuthenticationFilter(JWTUtils jwtUtils) {
    this.jwtUtils = jwtUtils;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    String token = null;
    String username = null;
    String userId = null;

    if (header != null && header.startsWith("Bearer ")) {
      token = header.substring(7);

      JWTUtils.TokenValidationResult validationResult = jwtUtils.validateToken(token);

      if (validationResult == JWTUtils.TokenValidationResult.VALID) {
        DecodedJWT decodedJWT = JWT.decode(token);
        username = decodedJWT.getSubject();
        userId = decodedJWT.getClaims().get("userId").asString();

        // Check if the token is blacklisted
        if (jwtUtils.isTokenBlacklisted(userId, token)) {
          handleBlacklistedToken(response);
          return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(username, null, null);
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } else {
        handleInvalidToken(response, validationResult);
        return;
      }
    }

    chain.doFilter(request, response);
  }

  private void handleBlacklistedToken(HttpServletResponse response) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    Map<String, String> responseBody = Map.of("message", "Token has been blacklisted");
    new ObjectMapper().writeValue(response.getWriter(), responseBody);
  }

  private void handleInvalidToken(
      HttpServletResponse response, JWTUtils.TokenValidationResult validationResult)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");

    String message =
        switch (validationResult) {
          case INVALID_SIGNATURE -> "Invalid token signature";
          case EXPIRED -> "Token has expired";
          case INVALID_PAYLOAD -> "Invalid token payload";
          default -> "Invalid token";
        };

    Map<String, String> responseBody = Map.of("message", message);
    new ObjectMapper().writeValue(response.getWriter(), responseBody);
  }
}
