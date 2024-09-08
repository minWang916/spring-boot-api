package com.kms.domain.auth;

import com.kms.domain.auth.dto.LoginRequest;
import com.kms.domain.auth.dto.LoginResponse;
import com.kms.domain.auth.dto.LogoutRequest;
import com.kms.domain.auth.dto.RegisterRequest;
import com.kms.utils.jwt.JWTUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication and authorization")
@RequestMapping("/auth")
public class AuthControllerImpl implements AuthController {

  private final AuthService authService;
  private final JWTUtils jwtUtils;

  @Override
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, String> registerUser(
      @Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request) {
    return authService.registerUser(registerRequest, request);
  }

  @Override
  public Map<String, String> verifyUser(@RequestParam("code") String code) {
    return authService.verifyUser(code);
  }

  @Override
  public LoginResponse loginUser(@Valid @RequestBody LoginRequest loginRequest) {
    return authService.loginUser(loginRequest);
  }

  @Override
  public Map<String, String> refreshToken(@RequestHeader("refreshToken") String refreshToken) {
    return jwtUtils.refreshToken(refreshToken);
  }

  @Override
  public Map<String, String> logoutUser(@Valid @RequestBody LogoutRequest logoutRequest) {
    return authService.logoutUser(logoutRequest);
  }
}
