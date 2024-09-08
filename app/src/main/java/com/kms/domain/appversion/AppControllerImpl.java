package com.kms.domain.appversion;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AppControllerImpl implements AppController {

  private final AppService appService;

  @Override
  public AppVersion returnAppVersion() {
    return appService.returnAppVersion();
  }
}
