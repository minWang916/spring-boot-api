package com.kms.domain.auth.dto;

import com.kms.domain.user.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

  @NotBlank(message = "Name cannot be blank")
  private String username;

  @NotBlank(message = "Email cannot be blank")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Password cannot be blank")
  private String password;

  private String fullName;

  public User toUser() {
    User user = new User();
    user.setUsername(username);
    user.setEmail(email);
    user.setPassword(password);
    user.setFullName(fullName);
    return user;
  }
}
