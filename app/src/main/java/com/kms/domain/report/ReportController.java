package com.kms.domain.report;

import com.kms.domain.report.dto.ReportResponse;
import com.kms.exceptions.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ReportController {

  @Operation(summary = "Get a count of records by specified field in a collection")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved the count",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReportResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request parameters",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Collection or field not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/reports/_countBy/{collection}/{field}")
  ReportResponse countByField(
      @Parameter(description = "The collection to count records from", example = "task")
          @PathVariable
          String collection,
      @Parameter(description = "The field to count by", example = "isCompleted") @PathVariable
          String field);
}
