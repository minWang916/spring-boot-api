package com.kms.domain.dashboard.dto;

import com.kms.domain.dashboard.Widget;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;

@Data
public class SaveDashboardRequest {

  @NotBlank(message = "Title cannot be blank")
  private String title;

  @NotBlank(message = "Layout type cannot be blank")
  private String layoutType;

  private List<Widget> widgets;
}
