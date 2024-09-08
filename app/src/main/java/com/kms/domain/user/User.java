package com.kms.domain.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "app_user")
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Entity representing a user in the system")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  @Schema(description = "Unique identifier of the user", example = "1")
  private Integer id;

  @Column(name = "username", unique = true, nullable = false)
  @Schema(description = "Username of the user", example = "johndoe")
  private String username;

  @Column(name = "email", unique = true, nullable = false)
  @Schema(description = "Email address of the user", example = "john.doe@example.com")
  private String email;

  @Schema(description = "Password of the user")
  private String password;

  @Schema(description = "Full name of the user", example = "John Doe")
  private String fullName;

  @Column(name = "verification_code", length = 64)
  @Schema(description = "Verification code for the user", example = "ABC123")
  private String verificationCode;

  @Schema(description = "Whether the user account is enabled")
  private boolean enabled;
}
