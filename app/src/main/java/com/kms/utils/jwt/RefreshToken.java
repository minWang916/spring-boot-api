package com.kms.utils.jwt;

import com.kms.domain.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;

@Data
@Entity
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private Instant expiryDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}
