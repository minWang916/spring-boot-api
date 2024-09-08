package com.kms.domain.dashboard;

import com.kms.domain.dashboard.dto.SaveDashboardRequest;
import com.kms.exceptions.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

public interface DashboardController {

  @Operation(summary = "Get all dashboards for the current user")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved the dashboards",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Dashboard.class))),
    @ApiResponse(
        responseCode = "500",
        description = "Error retrieving requested dashboards",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping
  List<Dashboard> getAllDashboards();

  @Operation(summary = "Update an existing dashboard or create a new one")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Dashboard updated successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Dashboard.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid dashboard data",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Dashboard not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "Error saving requested dashboard",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PutMapping("/{dashboardId}")
  Dashboard saveDashboard(
      @Parameter(description = "The dashboard id to update", required = true) @PathVariable
          int dashboardId,
      @Parameter(description = "The dashboard data to update", required = true) @Valid @RequestBody
          SaveDashboardRequest saveDashboardRequest);
}
