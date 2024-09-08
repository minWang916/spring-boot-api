package com.kms.domain.task;

import com.kms.domain.task.dto.SaveTaskRequest;
import com.kms.domain.user.UserRepository;
import com.kms.utils.appuser.AppUserService;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TaskService {

  private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

  private final TaskRepository taskRepository;
  private final AppUserService appUserService;
  private final UserRepository userRepository;

  public Task getTask(int id) {
    Optional<Task> optionalTask = taskRepository.findById(id);

    if (optionalTask.isEmpty()) {
      logger.debug("Task not found with id: {}", id);
      throw new NoSuchElementException("Task not found with id: " + id);
    }

    logger.debug("Task with ID {} is requested successfully", id);

    return optionalTask.get();
  }

  public Task saveTask(int taskId, SaveTaskRequest request, int userId) {
    Optional<Task> optionalTask = taskRepository.findById(taskId);
    Task task;

    if (optionalTask.isPresent()) {
      task = optionalTask.get();
      logger.debug("User with ID {} updates task ID {}", userId, taskId);
    } else {
      task = new Task();
      logger.debug("User with ID {} creates a new task", userId);
    }

    task.setTask(request.getTask());
    task.setIsCompleted(request.getIsCompleted());
    task.setUser(userRepository.findById(userId).get());

    return taskRepository.save(task);
  }

  public Task deleteTask(int id) {
    Optional<Task> optionalTask = taskRepository.findById(id);

    if (optionalTask.isEmpty()) {
      logger.debug("Task not found with ID {}", id);
      throw new NoSuchElementException("Task not found with id: " + id);
    }

    logger.debug("Task with ID {} is deleted", id);
    Task response = optionalTask.get();
    taskRepository.deleteById(id);
    return response;
  }

  public List<Task> getAllTasks(int userId) {
    logger.debug("User with ID {} retrieves all tasks", userId);
    return taskRepository.findByUser_Id(userId);
  }

  public List<Task> addTasks(List<SaveTaskRequest> saveTaskRequests, int userId) {
    List<Task> tasks = new ArrayList<>();

    for (SaveTaskRequest request : saveTaskRequests) {
      Task task = new Task();
      task.setTask(request.getTask());
      task.setIsCompleted(request.getIsCompleted());
      task.setUser(userRepository.findById(userId).get());
      tasks.add(task);
    }

    logger.debug("User with ID {} creates {} new tasks", userId, tasks.size());
    return taskRepository.saveAll(tasks);
  }

  public List<Task> getTasksByName(String name) {
    List<Task> tasks = taskRepository.findByTaskContainingIgnoreCase(name);

    if (tasks.isEmpty()) {
      logger.debug("Task not found with name: {}", name);
      throw new NoSuchElementException("Task not found with name: " + name);
    }

    logger.debug("{} tasks with name {} are found", tasks.size(), name);
    return tasks;
  }
}
