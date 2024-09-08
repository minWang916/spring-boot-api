package com.kms.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kms.domain.auth.AuthService;
import com.kms.domain.auth.dto.LoginRequest;
import com.kms.domain.auth.dto.LoginResponse;
import com.kms.domain.auth.dto.LogoutRequest;
import com.kms.domain.user.User;
import com.kms.domain.user.UserRepository;
import com.kms.utils.jwt.JWTUtils;
import com.kms.utils.jwt.RefreshToken;
import com.kms.utils.jwt.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest()
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LoginTest {

  private final MockMvc mockMvc;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;
  private final AuthService authService;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JWTUtils jwtUtils;

  @Autowired
  public LoginTest(
      MockMvc mockMvc,
      UserRepository userRepository,
      ObjectMapper objectMapper,
      AuthService authService,
      RefreshTokenRepository refreshTokenRepository,
      JWTUtils jwtUtils) {
    this.mockMvc = mockMvc;
    this.userRepository = userRepository;
    this.objectMapper = objectMapper;
    this.authService = authService;
    this.refreshTokenRepository = refreshTokenRepository;
    this.jwtUtils = jwtUtils;
  }

  @BeforeAll
  static void setUp(
      @Autowired UserRepository userRepository, @Autowired PasswordEncoder passwordEncoder) {
    userRepository.deleteAll();

    User user = new User();
    user.setUsername("testuser");
    user.setEmail("testuser@example.com");
    user.setPassword(passwordEncoder.encode("password123"));
    user.setEnabled(true); // User is verified
    userRepository.save(user);
  }

  @Test
  @Transactional
  void loginUser_shouldReturnTokensWhenSuccessfulLogin() throws Exception {
    String requestBody =
        objectMapper.writeValueAsString(new LoginRequest("testuser", "password123"));

    mockMvc
        .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.message").value("User logged in successfully."))
        .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());
  }

  @Test
  void loginUser_shouldReturnsNotFoundWhenNonExistingUser() throws Exception {
    String requestBody =
        objectMapper.writeValueAsString(new LoginRequest("nonexistinguser", "password123"));

    mockMvc
        .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User not found"));
  }

  @Test
  @Transactional
  void loginUser_shouldReturnsConflictUnverifiedUser() throws Exception {

    User user = userRepository.findByUsername("testuser").get();
    user.setEnabled(false); // User is not verified
    userRepository.save(user);

    String requestBody =
        objectMapper.writeValueAsString(new LoginRequest("testuser", "password123"));

    mockMvc
        .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User is not verified yet."));
  }

  @Test
  void loginUser_shouldReturnsUnauthorizedBadCredentials() throws Exception {
    String requestBody =
        objectMapper.writeValueAsString(new LoginRequest("testuser", "wrongpassword"));

    mockMvc
        .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Invalid credentials."));
  }

  @Test
  void logoutUser_shouldRemovesTokenWhenSuccessfulLogout() throws Exception {
    LoginResponse loginResponse =
        authService.loginUser(new LoginRequest("testuser", "password123"));
    String accessToken = loginResponse.getToken();

    LogoutRequest logoutRequest = new LogoutRequest(accessToken, "1");
    String requestBody = objectMapper.writeValueAsString(logoutRequest);

    mockMvc
        .perform(post("/auth/logout").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Logout successful"));

    Optional<RefreshToken> refreshToken = refreshTokenRepository.findByUserId(1);

    // Assert that the refresh token is removed after logout in the database
    assertTrue(refreshToken.isEmpty(), "Refresh token should be removed from the database.");
    // Assert that the access token is blacklisted after logout
    Assertions.assertTrue(
        jwtUtils.isTokenBlacklisted(String.valueOf(1), accessToken),
        "Access token should be blacklisted.");
  }

  @Test
  void logoutUser_shouldThrowExceptionWhenUserIdNotFound() {
    String accessToken = "someToken";
    LogoutRequest logoutRequest =
        new LogoutRequest(accessToken, "999"); // Assuming userId 999 does not exist

    NoSuchElementException exception =
        assertThrows(
            NoSuchElementException.class,
            () -> {
              authService.logoutUser(logoutRequest);
            });

    assertEquals("User ID not found.", exception.getMessage());
  }

  @Test
  void logoutUser_shouldThrowExceptionWhenTokenIsInvalidOrExpired() {
    // Arrange
    String accessToken = "invalidToken";
    LogoutRequest logoutRequest = new LogoutRequest(accessToken, "1");

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              authService.logoutUser(logoutRequest);
            });

    assertEquals("Invalid or expired JWT token.", exception.getMessage());
  }
}
