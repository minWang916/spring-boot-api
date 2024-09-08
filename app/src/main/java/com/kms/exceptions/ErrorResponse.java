package com.kms.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Error response containing a message")
public class ErrorResponse {

  @Schema(description = "The error message", example = "error message")
  private String message;
}
