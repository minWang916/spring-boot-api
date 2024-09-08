package com.kms.domain.auth;

import com.kms.domain.auth.dto.LoginRequest;
import com.kms.domain.auth.dto.LoginResponse;
import com.kms.domain.auth.dto.LogoutRequest;
import com.kms.domain.auth.dto.RegisterRequest;
import com.kms.exceptions.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

public interface AuthController {

  @Operation(summary = "Register a new user")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "User registered successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(
                        example =
                            "{\"message\":\"User registered successfully. A verification email has been sent, please confirm.\"}"))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid registration details",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Username or email already exists",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/register")
  Map<String, String> registerUser(
      @Parameter(description = "Registration request body") RegisterRequest registerRequest,
      @Parameter(description = "Current register request") HttpServletRequest request);

  @Operation(summary = "Verify a user's email with the verification code")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "User verified successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"message\":\"User verified successfully.\"}"))),
    @ApiResponse(
        responseCode = "404",
        description = "Verification code is invalid",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "User is already verified",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/verify")
  Map<String, String> verifyUser(
      @Parameter(
              description = "Verification code sent to the user's email",
              example = "80af39b6-eac3-454f-b255-c8e5a17934c4")
          String code);

  @Operation(summary = "Log in a user")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "User logged in successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid login details format",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Invalid credentials",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "User is not verified yet",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/login")
  LoginResponse loginUser(@Parameter(description = "Login request body") LoginRequest loginRequest);

  @Operation(summary = "Refresh the access token")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Access token refreshed successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"token\":\"new access token\"}"))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid refresh token",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/refresh-token")
  Map<String, String> refreshToken(@Parameter(description = "Refresh token") String refreshToken);

  @Operation(summary = "Log out a user")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "User logged out successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"message\":\"Logout successful\"}"))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid logout details",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Invalid or expired JWT token",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "User ID not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/logout")
  Map<String, String> logoutUser(
      @Parameter(description = "Logout request body") LogoutRequest logoutRequest);
}
