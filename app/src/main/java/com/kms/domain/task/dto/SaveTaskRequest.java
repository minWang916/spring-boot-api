package com.kms.domain.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveTaskRequest {

  @NotBlank(message = "Task cannot be blank")
  private String task;

  @NotNull(message = "isCompleted must be either true or false")
  private Boolean isCompleted;
}
