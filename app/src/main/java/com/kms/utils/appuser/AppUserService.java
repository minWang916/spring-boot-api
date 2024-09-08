package com.kms.utils.appuser;

import com.kms.domain.user.*;
import java.util.NoSuchElementException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppUserService {
  private final UserRepository userRepository;

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Object principal = authentication.getPrincipal();

    if (principal instanceof com.kms.domain.user.User) {
      return (User) principal;
    } else if (principal instanceof org.springframework.security.core.userdetails.User) {
      String username =
          ((org.springframework.security.core.userdetails.User) principal).getUsername();
      return userRepository
          .findByUsername(username)
          .orElseThrow(
              () -> new NoSuchElementException("User not found with username: " + username));
    } else {
      throw new IllegalStateException(
          "Unexpected principal type: " + principal.getClass().getName());
    }
  }
}
