package com.kms.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
  private String message;
  private String token;
  private String refreshToken;
}
