package com.kms.domain.task;

import com.kms.domain.task.dto.SaveTaskRequest;
import com.kms.utils.jwt.JWTUtils;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TaskControllerImpl implements TaskController {

  private final TaskService taskService;
  private final JWTUtils jwtUtils;

  @Override
  public Task getTask(int id) {
    return taskService.getTask(id);
  }

  @Override
  public Task saveOrUpdateTask(int taskId, @Valid SaveTaskRequest request) {
    int userId = Integer.parseInt(jwtUtils.getUserIdFromToken());
    return taskService.saveTask(taskId, request, userId);
  }

  @Override
  public Task deleteTask(int id) {
    return taskService.deleteTask(id);
  }

  @Override
  public List<Task> getAllTasks() {
    int userId = Integer.parseInt(jwtUtils.getUserIdFromToken());
    return taskService.getAllTasks(userId);
  }

  @Override
  public List<Task> addTasks(@Valid List<SaveTaskRequest> saveTaskRequest) {
    int userId = Integer.parseInt(jwtUtils.getUserIdFromToken());
    return taskService.addTasks(saveTaskRequest, userId);
  }

  @Override
  public List<Task> getTasksByName(String name) {
    return taskService.getTasksByName(name);
  }
}
