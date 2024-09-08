package com.kms.report;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kms.domain.contact.Contact;
import com.kms.domain.contact.ContactRepository;
import com.kms.domain.report.ReportService;
import com.kms.domain.task.Task;
import com.kms.domain.task.TaskRepository;
import com.kms.domain.user.User;
import com.kms.domain.user.UserRepository;
import java.util.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest()
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestReport {
  private final MockMvc mockMvc;
  private final TaskRepository taskRepository;
  private final ContactRepository contactRepository;
  private ReportService reportService;

  @Autowired
  public TestReport(
      MockMvc mockMvc, TaskRepository taskRepository, ContactRepository contactRepository) {
    this.mockMvc = mockMvc;
    this.taskRepository = taskRepository;
    this.contactRepository = contactRepository;
  }

  @BeforeAll
  static void setUp(
      @Autowired ContactRepository contactRepository,
      @Autowired TaskRepository taskRepository,
      @Autowired UserRepository userRepository)
      throws Exception {
    // Clear all data before begin
    contactRepository.deleteAll();
    taskRepository.deleteAll();
    userRepository.deleteAll();

    // Mock Contact records
    Contact contact1 =
        new Contact(null, "John", "Doe", "Manager", "IT", "Alpha Project", "avatar1.png", 1001);
    Contact contact2 =
        new Contact(null, "Jane", "Smith", "Developer", "IT", "Beta Project", "default.png", 1002);
    Contact contact3 =
        new Contact(
            null, "Bob", "Johnson", "Designer", "Design", "Beta Project", "avatar3.png", 1003);
    Contact contact4 =
        new Contact(
            null, "Alice", "Smith", "Developer", "IT", "Alpha Project", "default.png", 1004);
    Contact contact5 =
        new Contact(
            null, "Bob", "Brown", "Manager", "Sales", "Epsilon Project", "avatar5.png", 1005);

    List<Contact> contacts = Arrays.asList(contact1, contact2, contact3, contact4, contact5);
    contactRepository.saveAll(contacts);

    // Mock User records
    User user1 = new User(1, "username1", "mockemail1@gmail.com", "password", "", "", true);
    User user2 = new User(2, "username2", "mockemail2@gmail.com", "password", "", "", true);
    userRepository.saveAll(Arrays.asList(user1, user2));

    // Mock Task records
    Task task1 = new Task(null, "Complete project report", true, user1);
    Task task2 = new Task(null, "Fix bugs in code", false, user1);
    Task task3 = new Task(null, "Prepare presentation", true, user2);

    List<Task> tasks = Arrays.asList(task1, task2, task3);
    taskRepository.saveAll(tasks);
  }

  @Test
  @WithMockUser(username = "testuser")
  void shouldReturnBadRequestForInvalidField() throws Exception {
    // Test for invalid field in task collection
    mockMvc
        .perform(get("/reports/_countBy/task/invalidField"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("application/json"))
        .andExpect(
            jsonPath("$.message").value(org.hamcrest.Matchers.containsString("invalidField")))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("com.kms.domain.task.Task")));
  }

  @Test
  @WithMockUser(username = "testuser")
  void shouldReturnNotFoundForInvalidCollection() throws Exception {
    // Test for invalid collection input
    mockMvc
        .perform(get("/reports/_countBy/invalidCollection/title"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.message").value("Collection 'invalidCollection' not found"));
  }

  @Test
  @WithMockUser(username = "testuser")
  void shouldReturnCorrectResultForBasicTaskQuery() throws Exception {
    // Test the basic query using the task data
    mockMvc
        .perform(get("/reports/_countBy/task/isCompleted"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.collection").value("task"))
        .andExpect(jsonPath("$.field").value("isCompleted"))
        .andExpect(jsonPath("$.values.true").value(2))
        .andExpect(jsonPath("$.values.false").value(1));
  }

  @Test
  @WithMockUser(username = "testuser")
  void shouldReturnUpdatedResultAfterContactDatabaseModification() throws Exception {
    // Initial query check
    mockMvc
        .perform(get("/reports/_countBy/contact/title"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.values.Manager").value(2))
        .andExpect(jsonPath("$.values.Developer").value(2))
        .andExpect(jsonPath("$.values.Designer").value(1));

    // Simulate database modifications
    // 1. Delete a record
    contactRepository.deleteById(5); // Deleting Bob Brown
    // 2. Modify a field
    Contact contactToUpdate = contactRepository.findById(2).orElseThrow();
    contactToUpdate.setTitle("Manager");
    contactRepository.save(contactToUpdate);
    // 3. Add a new record
    Contact newContact =
        new Contact(
            null, "Charles", "Davis", "Analyst", "Finance", "Gamma Project", "avatar6.png", 1006);
    contactRepository.save(newContact);

    // Check query after modification
    mockMvc
        .perform(get("/reports/_countBy/contact/title"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.values.Manager").value(2))
        .andExpect(jsonPath("$.values.Developer").value(1))
        .andExpect(jsonPath("$.values.Designer").value(1))
        .andExpect(jsonPath("$.values.Analyst").value(1));
  }
}
