package com.kms.utils.jwt;

import com.kms.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);

  void deleteByToken(String token);

  void deleteByUser(User user);

  Optional<RefreshToken> findByUserId(int userId);
}
