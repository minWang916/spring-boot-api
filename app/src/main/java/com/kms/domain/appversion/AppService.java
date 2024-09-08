package com.kms.domain.appversion;

import org.springframework.stereotype.Service;

@Service
public class AppService {
  public AppVersion returnAppVersion() {
    return new AppVersion(1L, "Assignment", "1.0.0");
  }
}
