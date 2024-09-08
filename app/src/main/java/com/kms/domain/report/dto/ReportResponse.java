package com.kms.domain.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(
    description = "Response structure for counting records by a specified field in a collection")
public class ReportResponse {

  @Schema(description = "The name of the collection being counted", example = "task")
  private String collection;

  @Schema(description = "The name of the field being counted", example = "isCompleted")
  private String field;

  @Schema(description = "A map containing the counts of each value for the specified field")
  private Map<String, Long> values;
}
