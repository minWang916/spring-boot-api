package com.kms.task;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kms.domain.task.Task;
import com.kms.domain.task.TaskRepository;
import com.kms.domain.task.TaskService;
import com.kms.domain.task.dto.SaveTaskRequest;
import com.kms.domain.user.User;
import com.kms.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest()
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TaskTest {

  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;
  private final TaskService taskService;
  private final TaskRepository taskRepository;

  @Autowired
  public TaskTest(
      MockMvc mockMvc,
      ObjectMapper objectMapper,
      TaskService taskService,
      TaskRepository taskRepository) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
    this.taskService = taskService;
    this.taskRepository = taskRepository;
  }

  @BeforeAll
  static void initDatabase(
      @Autowired UserRepository userRepository, @Autowired TaskRepository taskRepository) {
    // Clear existing data
    userRepository.deleteAll();
    taskRepository.deleteAll();

    // Create mock users and tasks
    User mockUser1 =
        new User(1, "username_demo_1", "demo1@gmail.com", "matkhau9161", "John Doe 1", "", true);
    User mockUser2 =
        new User(2, "username_demo_2", "demo2@gmail.com", "matkhau9162", "John Doe 2", "", true);
    Task mockTask1 = new Task(1, "Clean floor", false, mockUser1);
    Task mockTask2 = new Task(2, "Clean kitchen", false, mockUser1);
    Task mockTask3 = new Task(3, "Cook dinner", false, mockUser2);

    // Save mock users and tasks
    userRepository.save(mockUser1);
    userRepository.save(mockUser2);
    taskRepository.save(mockTask1);
    taskRepository.save(mockTask2);
    taskRepository.save(mockTask3);
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void getTask_shouldGetTaskById() throws Exception {
    mockMvc
        .perform(get("/tasks/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.task").value("Clean floor"))
        .andExpect(jsonPath("$.isCompleted").value(false));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void getTasks_shouldReturnNotFoundWithNonExistingId() throws Exception {
    mockMvc
        .perform(get("/tasks/999").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Task not found with id: 999"));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  @Transactional
  void saveTask_shouldUpdateTaskById(@Autowired TaskRepository taskRepository) throws Exception {
    // Assume a task with ID 1 already exists in the database
    SaveTaskRequest updateRequest = new SaveTaskRequest();
    updateRequest.setTask("Updated Task");
    updateRequest.setIsCompleted(true);

    taskService.saveTask(1, updateRequest, 1);

    // Verify the task was updated in the database
    Task updatedTask = taskRepository.findById(1).get();
    Assertions.assertEquals("Updated Task", updatedTask.getTask());
    Assertions.assertTrue(updatedTask.getIsCompleted());
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  @Transactional
  void saveTask_shouldCreateNewTaskWithNonExistingId(@Autowired TaskRepository taskRepository)
      throws Exception {
    // Assume task with ID 999 does not exist
    SaveTaskRequest createRequest = new SaveTaskRequest();
    createRequest.setTask("Newly Created Task");
    createRequest.setIsCompleted(false);

    taskService.saveTask(999, createRequest, 1);

    // Verify the new task was created in the database
    List<Task> tasks = taskRepository.findAll();
    Task newTask =
        tasks.stream()
            .filter(task -> task.getTask().equals("Newly Created Task"))
            .findFirst()
            .orElse(null);

    assertNotNull(newTask);
    Assertions.assertEquals("Newly Created Task", newTask.getTask());
    Assertions.assertFalse(newTask.getIsCompleted());
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void saveTask_shouldReturnBadRequestWhenTaskIsBlank() throws Exception {
    // Arrange
    SaveTaskRequest invalidRequest = new SaveTaskRequest();
    invalidRequest.setTask(""); // Invalid blank task
    invalidRequest.setIsCompleted(true);

    String requestBody = objectMapper.writeValueAsString(invalidRequest);

    // Act & Assert
    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/tasks/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.task", containsString("Task cannot be blank")));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void saveTask_shouldReturnBadRequestWhenIsCompletedIsNull() throws Exception {
    // Arrange
    SaveTaskRequest invalidRequest = new SaveTaskRequest();
    invalidRequest.setTask("Sample Task");
    invalidRequest.setIsCompleted(null); // Invalid null isCompleted

    String requestBody = objectMapper.writeValueAsString(invalidRequest);

    // Act & Assert
    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/tasks/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.isCompleted", containsString("isCompleted must be either true or false")));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  @Transactional
  void deleteTask_shouldDeleteTaskById() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.delete("/tasks/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.task").value("Clean floor"))
        .andExpect(jsonPath("$.isCompleted").value(false));

    mockMvc
        .perform(get("/tasks/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Task not found with id: 1"));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void deleteTask_shouldThrowExceptionWhenTaskIdNotFound() throws Exception {
    mockMvc
        .perform(get("/tasks/999").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Task not found with id: 999"));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void getAllTasks_shouldGetAllTasks() throws Exception {
    List<Task> tasks = taskService.getAllTasks(1);
    assertEquals(2, tasks.size());
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  @Transactional
  void addTasks_shouldAddTasks() throws Exception {
    SaveTaskRequest taskRequest1 = new SaveTaskRequest();
    taskRequest1.setTask("Test Task 1");
    taskRequest1.setIsCompleted(false);

    SaveTaskRequest taskRequest2 = new SaveTaskRequest();
    taskRequest2.setTask("Test Task 2");
    taskRequest2.setIsCompleted(true);

    List<SaveTaskRequest> taskRequests = List.of(taskRequest1, taskRequest2);

    taskService.addTasks(taskRequests, 1);
    List<Task> tasks = taskRepository.findByUser_Id(1);

    // Assert that the latest two tasks match the added tasks
    assertEquals("Test Task 1", tasks.get(tasks.size() - 2).getTask());
    assertFalse(tasks.get(tasks.size() - 2).getIsCompleted());

    assertEquals("Test Task 2", tasks.get(tasks.size() - 1).getTask());
    assertTrue(tasks.get(tasks.size() - 1).getIsCompleted());
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void getTaskByName_shouldGetTasksByName() throws Exception {
    mockMvc
        .perform(get("/tasks/search?name=clean").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].task").value("Clean floor"))
        .andExpect(jsonPath("$[1].task").value("Clean kitchen"));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void getTaskByName_shouldReturnNotFoundWithNonExistingName() throws Exception {
    mockMvc
        .perform(
            get("/tasks/search?name=non existing task").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Task not found with name: non existing task"));
  }
}
