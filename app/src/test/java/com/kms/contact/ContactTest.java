package com.kms.contact;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kms.domain.contact.Contact;
import com.kms.domain.contact.ContactRepository;
import com.kms.domain.contact.ContactService;
import com.kms.domain.contact.dto.SaveContactRequest;
import com.kms.domain.user.User;
import com.kms.domain.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest()
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ContactTest {
  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;
  private final ContactRepository contactRepository;

  @Autowired
  public ContactTest(
      MockMvc mockMvc,
      ObjectMapper objectMapper,
      ContactRepository contactRepository,
      UserRepository userRepository) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
    this.contactRepository = contactRepository;
  }

  @BeforeAll
  static void initDatabase(
      @Autowired ContactRepository contactRepository, @Autowired UserRepository userRepository) {
    // Clear existing data
    contactRepository.deleteAll();
    // Clear existing user
    User mockUser =
        new User(1, "username_demo_1", "demo1@gmail.com", "matkhau9161", "John Doe 1", "", true);
    userRepository.save(mockUser);

    Contact contact1 =
        new Contact(null, "John", "Doe", "Manager", "IT", "Alpha Project", "default", 1001);
    Contact contact2 =
        new Contact(null, "Jane", "Smith", "Developer", "IT", "Beta Project", "default", 1002);
    Contact contact3 =
        new Contact(null, "Bob", "Johnson", "Designer", "Design", "Gamma Project", "default", 1003);
    Contact contact4 =
        new Contact(
            null, "Alice", "Williams", "HR Manager", "HR", "Human Resources", "default", 1004);
    Contact contact5 =
        new Contact(
            null, "Michael", "Brown", "Sales Lead", "Sales", "Sales Strategy", "default", 1005);

    contactRepository.saveAll(List.of(contact1, contact2, contact3, contact4, contact5));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void getContact_shouldReturnsContactWithExistingId() throws Exception {
    mockMvc
        .perform(get("/contacts/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.lastName").value("Doe"))
        .andExpect(jsonPath("$.title").value("Manager"))
        .andExpect(jsonPath("$.department").value("IT"))
        .andExpect(jsonPath("$.project").value("Alpha Project"))
        .andExpect(jsonPath("$.avatar").value("default"))
        .andExpect(jsonPath("$.employeeId").value(1001));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void getContact_shouldReturnsNotFoundWithNonExistingId() throws Exception {
    mockMvc
        .perform(get("/contacts/999").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Contact not found with id: 999"));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  @Transactional
  void saveContact_shouldUpdatesExistingContactWithExistingId() throws Exception {
    int contactId = 2; // Assuming a contact with ID 2 already exists

    SaveContactRequest updateRequest = new SaveContactRequest();
    updateRequest.setFirstName("Teo");
    updateRequest.setLastName("Nguyen");
    updateRequest.setTitle("Staff");
    updateRequest.setDepartment("Mechanical");
    updateRequest.setProject("Delta Project");
    updateRequest.setAvatar("image1.png");
    updateRequest.setEmployeeId(1042);

    mockMvc
        .perform(
            put("/contacts/{id}", contactId) // Include the contact ID in the URL
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Teo"))
        .andExpect(jsonPath("$.lastName").value("Nguyen"))
        .andExpect(jsonPath("$.title").value("Staff"))
        .andExpect(jsonPath("$.department").value("Mechanical"))
        .andExpect(jsonPath("$.project").value("Delta Project"))
        .andExpect(jsonPath("$.avatar").value("image1.png"))
        .andExpect(jsonPath("$.employeeId").value(1042));

    // Verify the contact was updated in the database
    Contact updatedContact = contactRepository.findById(contactId).get();

    Assertions.assertEquals("Teo", updatedContact.getFirstName());
    Assertions.assertEquals("Nguyen", updatedContact.getLastName());
    Assertions.assertEquals("Staff", updatedContact.getTitle());
    Assertions.assertEquals("Mechanical", updatedContact.getDepartment());
    Assertions.assertEquals("Delta Project", updatedContact.getProject());
    Assertions.assertEquals("image1.png", updatedContact.getAvatar());
    Assertions.assertEquals(1042, updatedContact.getEmployeeId());
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  @Transactional
  void saveContact_shouldCreateNewContactIfIdDoesNotExist() throws Exception {
    int contactId = 99; // Assuming contact with ID 99 does not exist

    SaveContactRequest newRequest = new SaveContactRequest();
    newRequest.setFirstName("New");
    newRequest.setLastName("Contact");
    newRequest.setTitle("Developer");
    newRequest.setDepartment("IT");
    newRequest.setProject("New Project");
    newRequest.setAvatar("image2.png");
    newRequest.setEmployeeId(1046);

    mockMvc
        .perform(
            put("/contacts/{id}", contactId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("New"))
        .andExpect(jsonPath("$.lastName").value("Contact"))
        .andExpect(jsonPath("$.title").value("Developer"))
        .andExpect(jsonPath("$.department").value("IT"))
        .andExpect(jsonPath("$.project").value("New Project"))
        .andExpect(jsonPath("$.avatar").value("image2.png"))
        .andExpect(jsonPath("$.employeeId").value(1046));

    // Verify the contact was created in the database with a new ID
    Optional<Contact> createdContact =
        contactRepository.findById(6); // Should be ID 6 since there are already 5 contacts
    assertTrue(createdContact.isPresent());
    Assertions.assertEquals("New", createdContact.get().getFirstName());
    Assertions.assertEquals("Contact", createdContact.get().getLastName());
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void saveContact_shouldReturnBadRequestWhenFirstNameIsBlank() throws Exception {
    int contactId = 2; // Assuming a contact with ID 2 exists

    SaveContactRequest updateRequest = new SaveContactRequest();
    updateRequest.setFirstName(""); // Invalid blank first name
    updateRequest.setLastName("Nguyen");
    updateRequest.setTitle("Staff");
    updateRequest.setDepartment("Mechanical");
    updateRequest.setProject("Delta Project");
    updateRequest.setAvatar("image1.png");
    updateRequest.setEmployeeId(1042);

    mockMvc
        .perform(
            put("/contacts/{id}", contactId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.firstName").value("Firstname cannot be blank"));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void saveContact_shouldReturnBadRequestWhenEmployeeIdIsInvalid() throws Exception {
    int contactId = 2; // Assuming a contact with ID 2 exists

    String updateRequestJson =
        """
    {
        "firstName": "Teo",
        "lastName": "Nguyen",
        "title": "Staff",
        "department": "Mechanical",
        "project": "Delta Project",
        "avatar": "image1.png",
        "employeeId": -99
    }
    """;

    mockMvc
        .perform(
            put("/contacts/{id}", contactId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.employeeId").value("Employee ID must be a positive number"));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  @Transactional
  void deleteContact_shouldDeletesContactWithExistingId() throws Exception {
    mockMvc
        .perform(delete("/contacts/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.lastName").value("Doe"))
        .andExpect(jsonPath("$.title").value("Manager"))
        .andExpect(jsonPath("$.department").value("IT"))
        .andExpect(jsonPath("$.project").value("Alpha Project"))
        .andExpect(jsonPath("$.avatar").value("default"))
        .andExpect(jsonPath("$.employeeId").value(1001));

    Optional<Contact> deleteContact = contactRepository.findById(1);
    assertTrue(deleteContact.isEmpty());
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void deleteContact_shouldReturnsNotFoundWithNonExistingId() throws Exception {
    mockMvc
        .perform(delete("/contacts/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Contact not found with id: 999"));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void getAllContacts_shouldReturnsAllContacts() throws Exception {
    mockMvc
        .perform(get("/contacts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(5));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  @Transactional
  void addContacts_shouldCreatesContacts() throws Exception {
    SaveContactRequest request1 = new SaveContactRequest();
    request1.setFirstName("Teo");
    request1.setLastName("Nguyen");
    request1.setTitle("Developer");
    request1.setDepartment("Mechanical");
    request1.setProject("Delta Project");
    request1.setAvatar("default");
    request1.setEmployeeId(1042);

    SaveContactRequest request2 = new SaveContactRequest();
    request2.setFirstName("Kim");
    request2.setLastName("Jong");
    request2.setTitle("Project Manager");
    request2.setDepartment("Admin");
    request2.setProject("Nuclear");
    request2.setAvatar("default");
    request2.setEmployeeId(1051);

    List<SaveContactRequest> requests = List.of(request1, request2);
    String requestJson = objectMapper.writeValueAsString(requests);

    mockMvc
        .perform(post("/contacts").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].firstName").value("Teo"))
        .andExpect(jsonPath("$[1].firstName").value("Kim"));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void addContacts_shouldReturnJsonParseErrorWithInvalidRequestBody() throws Exception {
    String requestJson =
        """
            invalid content
            """;

    mockMvc
        .perform(post("/contacts").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", containsString("JSON parse error")));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void getContactsByName_shouldReturnsMatchingContactsWithExistingName() throws Exception {
    mockMvc
        .perform(get("/contacts/search?name=John").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].firstName").value("John"));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void testGetContactsByName_shouldReturnsNotFoundWithNonExistingName() throws Exception {
    mockMvc
        .perform(
            get("/contacts/search?name=non-existing-name").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Contact not found with name: non-existing-name"));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  @Transactional
  void importContacts_shouldCreatesContactsFromCsv() throws Exception {
    String csvContent =
        "firstName,lastName,title,department,project,avatar,employeeId\n"
            + "Donald,Trump,Manager,IT,Alpha Project,default,1001\n"
            + "Bin,Laden,Developer,IT,Beta Project,default,1002\n";

    MockMultipartFile csvFile =
        new MockMultipartFile("file", "contacts.csv", "text/csv", csvContent.getBytes());

    mockMvc.perform(multipart("/contacts/import").file(csvFile)).andExpect(status().isCreated());

    List<Contact> contacts = contactRepository.findAll();
    assertEquals(7, contacts.size());

    Contact contact1 = contacts.get(5);
    Assertions.assertEquals("Donald", contact1.getFirstName());
    Assertions.assertEquals("Trump", contact1.getLastName());

    Contact contact2 = contacts.get(6);
    Assertions.assertEquals("Bin", contact2.getFirstName());
    Assertions.assertEquals("Laden", contact2.getLastName());
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void importContacts_shouldThrowRuntimeExceptionWhenIOExceptionOccurs(
      @Autowired ContactService contactService) throws Exception {
    MultipartFile mockFile = mock(MultipartFile.class);

    // Simulate an IOException when trying to get the input stream from the file
    when(mockFile.getInputStream()).thenThrow(new IOException("Fake IO Exception"));

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              contactService.importContacts(mockFile);
            });

    assertEquals("Failed to import contacts from CSV file", exception.getMessage());
    assertInstanceOf(IOException.class, exception.getCause());
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void exportContacts_shouldReturnsCsvFile() throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/contacts/export"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=contacts.csv"))
            .andReturn();

    String csvContent = result.getResponse().getContentAsString();
    assertTrue(csvContent.contains("John,Doe,Manager,IT,Alpha Project,default,1001"));
    assertTrue(csvContent.contains("Jane,Smith,Developer,IT,Beta Project,default,1002"));
    assertTrue(csvContent.contains("Bob,Johnson,Designer,Design,Gamma Project,default,1003"));
    assertTrue(csvContent.contains("Alice,Williams,HR Manager,HR,Human Resources,default,1004"));
    assertTrue(csvContent.contains("Michael,Brown,Sales Lead,Sales,Sales Strategy,default,1005"));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void exportContacts_shouldThrowRuntimeExceptionWhenIOExceptionOccurs(
      @Autowired ContactService contactService) throws Exception {
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    PrintWriter mockWriter = mock(PrintWriter.class);

    // Simulate an IOException when trying to write to the response's PrintWriter
    when(mockResponse.getWriter()).thenReturn(mockWriter);
    doThrow(new IOException("Fake IO Exception")).when(mockWriter).write(anyString());

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              contactService.exportContacts(mockResponse);
            });

    assertEquals("Failed to export contacts to CSV file", exception.getMessage());
    assertInstanceOf(IOException.class, exception.getCause());
  }
}
