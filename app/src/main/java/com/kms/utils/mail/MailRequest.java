package com.kms.utils.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailRequest {
  @JsonProperty("to_email")
  private String toEmail;

  private String subject;

  private String message;

  @JsonProperty("html")
  private boolean isHTML;
}
