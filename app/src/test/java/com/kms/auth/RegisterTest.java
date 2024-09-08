package com.kms.auth;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kms.domain.user.*;
import jakarta.transaction.Transactional;
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

@SpringBootTest()
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RegisterTest {

  private final MockMvc mockMvc;

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  @Autowired
  public RegisterTest(
      MockMvc mockMvc, UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.mockMvc = mockMvc;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @BeforeAll
  static void setUp(@Autowired UserRepository userRepository) {
    userRepository.deleteAll();

    User mockUser =
        new User(1, "username_demo_1", "demo1@gmail.com", "matkhau9161", "John Doe 1", "", true);
    userRepository.save(mockUser);
  }

  @Test
  void registerUser_shouldReturnOkAndRegisterUserSuccessfully() throws Exception {
    String json =
        """
    {
        "username": "testuser",
        "email": "test@example.com",
        "password": "password123"
    }
    """;

    mockMvc
        .perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isCreated())
        .andExpect(
            jsonPath(
                "$.message",
                is(
                    "User registered successfully. A verification email has been sent, please confirm.")));

    // Verify that the user is saved in the repository
    Optional<User> optionalRegisteredUser = userRepository.findByEmail("test@example.com");
    User registeredUser = optionalRegisteredUser.get();
    assertNotNull(registeredUser);
    Assertions.assertFalse(registeredUser.isEnabled());
    assertNotNull(registeredUser.getVerificationCode());

    // Check if the password is encoded
    Assertions.assertNotEquals("password123", registeredUser.getPassword());
    Assertions.assertTrue(passwordEncoder.matches("password123", registeredUser.getPassword()));
  }

  @Test
  void registerUser_shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
    String json =
        """
    {
        "username": "testuser",
        "email": "invalidEmail",
        "password": "password123"
    }
    """;

    mockMvc
        .perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.email", is("Invalid email format")));
  }

  @Test
  void registerUser_shouldReturnBadRequestWhenPasswordIsBlank() throws Exception {
    String json =
        """
    {
        "username": "testuser",
        "email": "test@example.com",
        "password": ""
    }
    """;

    mockMvc
        .perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.password", is("Password cannot be blank")));
  }

  @Test
  void registerUser_shouldReturnBadRequestWhenUsernameAlreadyExists() throws Exception {
    String json =
        """
    {
        "username": "username_demo_1",
        "email": "test@example.com",
        "password": "password123"
    }
    """;

    mockMvc
        .perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", is("Username username_demo_1 already exists")));
  }

  @Test
  void registerUser_shouldReturnBadRequestWhenEmailAlreadyExists() throws Exception {
    String json =
        """
    {
        "username": "testuser123",
        "email": "demo1@gmail.com",
        "password": "password123"
    }
    """;

    mockMvc
        .perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", is("Email demo1@gmail.com already exists")));
  }

  @Test
  @Transactional
  void verifyUser_ShouldReturnOKWithValidVerificationCodeAndValidUser() throws Exception {
    // 1. Mock registering a new unverified user
    String json =
        """
    {
        "username": "testuser",
        "email": "testuser@example.com",
        "password": "password123"
    }
    """;

    mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(json));

    User user = userRepository.findByEmail("testuser@example.com").get();
    String verificationCode = user.getVerificationCode();

    // 2. Verify newly registered user
    mockMvc.perform(get("/auth/verify").param("code", verificationCode));

    // 3. Verify that the user is now verified
    User verifiedUser = userRepository.findByEmail("testuser@example.com").get();
    Assertions.assertTrue(verifiedUser.isEnabled());
    assertNull(verifiedUser.getVerificationCode());
  }

  @Test
  void verifyUser_ShouldReturnBadRequestWithInvalidVerificationCode() throws Exception {
    mockMvc
        .perform(get("/auth/verify").param("code", "invalidCode"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", is("Verification code is invalid.")));
  }

  @Test
  @Transactional
  void verifyUser_ShouldReturnBadRequestWithAlreadyVerifiedUser() throws Exception {
    String verificationCode = "abc";

    User mockUser =
        new User(
            3,
            "username_demo_3",
            "demo3@gmail.com",
            "matkhau9161",
            "John Doe 3",
            verificationCode,
            true);
    userRepository.save(mockUser);

    mockMvc
        .perform(get("/auth/verify").param("code", verificationCode))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message", is("User is already verified.")));
  }
}
