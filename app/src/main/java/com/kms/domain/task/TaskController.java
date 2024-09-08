package com.kms.domain.task;

import com.kms.domain.task.dto.SaveTaskRequest;
import com.kms.exceptions.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Task Management", description = "APIs for managing tasks")
@RequestMapping("/tasks")
public interface TaskController {

  @Operation(summary = "Fetch a specific task by its ID")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Task found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Task.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid task ID",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Task not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/{id}")
  Task getTask(
      @Parameter(description = "ID of the task to be retrieved", example = "1") @PathVariable
          int id);

  @Operation(summary = "Save an existing task")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Task saved successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Task.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request data",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Task not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PutMapping("/{id}")
  Task saveOrUpdateTask(
      @Parameter(description = "ID of the task to be updated or created", example = "1")
          @PathVariable("id")
          int id,
      @Valid @RequestBody SaveTaskRequest request);

  @Operation(summary = "Delete a task by its ID")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Task deleted successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Task.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid task ID",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Task not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping("/{id}")
  Task deleteTask(
      @Parameter(description = "ID of the task to be deleted", example = "1") @PathVariable int id);

  @Operation(summary = "Get all tasks")
  @ApiResponse(
      responseCode = "200",
      description = "List of all tasks",
      content =
          @Content(
              mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = Task.class))))
  @GetMapping
  List<Task> getAllTasks();

  @Operation(summary = "Add tasks to the database")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Tasks created successfully",
        content =
            @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = Task.class)))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request data",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  List<Task> addTasks(@Valid @RequestBody List<SaveTaskRequest> saveTaskRequest);

  @Operation(summary = "Search tasks by a given name")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "List of tasks matching the search criteria",
        content =
            @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = Task.class)))),
    @ApiResponse(
        responseCode = "404",
        description = "No tasks found matching the search criteria",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/search")
  List<Task> getTasksByName(
      @Parameter(description = "Name to search for", example = "clean") @RequestParam("name")
          String name);
}
