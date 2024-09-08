package com.kms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.kms")
public class AssignmentApplication {

  public static void main(String[] args) {
    SpringApplication.run(AssignmentApplication.class, args);
  }
}
