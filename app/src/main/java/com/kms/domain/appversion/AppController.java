package com.kms.domain.appversion;

import com.kms.exceptions.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Application", description = "APIs related to application information")
@RequestMapping("/app/version")
public interface AppController {

  @Operation(summary = "Get the current version of the application")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved the application version",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AppVersion.class))),
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping
  AppVersion returnAppVersion();
}
