package com.kms.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutRequest {

  @NotBlank(message = "Token cannot be blank")
  private String token;

  @NotBlank(message = "User ID cannot be blank")
  private String userId;
}
