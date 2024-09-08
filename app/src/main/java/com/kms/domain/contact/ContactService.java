package com.kms.domain.contact;

import com.kms.domain.contact.dto.SaveContactRequest;
import com.kms.utils.appuser.AppUserService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class ContactService {

  private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

  private final ContactRepository contactRepository;
  private final AppUserService appUserService;

  public Contact getContact(int id) {
    Optional<Contact> optionalContact = contactRepository.findById(id);

    if (optionalContact.isEmpty()) {
      logger.debug("Contact not found with id: {}", id);
      throw new NoSuchElementException("Contact not found with id: " + id);
    }

    logger.debug("Contact ID {} is requested successfully", id);
    return optionalContact.get();
  }

  public Contact saveContact(int contactId, SaveContactRequest request) {
    Optional<Contact> optionalContact = contactRepository.findById(contactId);

    Contact contact;
    if (optionalContact.isPresent()) {
      contact = optionalContact.get();
      logger.debug("contact ID {} was updated", contact.getId());
    } else {
      contact = request.toContact();
      logger.debug("A new contact is created");
    }

    request.updateContact(contact);

    return contactRepository.save(contact);
  }

  public Contact deleteContact(int contactId) {
    Optional<Contact> optionalContact = contactRepository.findById(contactId);

    if (optionalContact.isEmpty()) {
      logger.debug("Contact not found with ID {}", contactId);
      throw new NoSuchElementException("Contact not found with id: " + contactId);
    }

    Contact response = optionalContact.get();

    logger.debug("Contact with ID {} is deleted", contactId);
    contactRepository.deleteById(contactId);
    return response;
  }

  public List<Contact> getAllContacts() {
    List<Contact> contacts = contactRepository.findAll();
    logger.debug("All contacts are retrieved");
    return contacts;
  }

  public List<Contact> addContacts(List<SaveContactRequest> saveContactRequests) {
    List<Contact> contacts = new ArrayList<>();

    for (SaveContactRequest request : saveContactRequests) {
      Contact contact = new Contact();
      request.updateContact(contact);
      contacts.add(contact);
    }

    logger.debug("{} new contacts are added", contacts.size());

    return contactRepository.saveAll(contacts);
  }

  public List<Contact> getContactsByName(String name) {
    List<Contact> contacts =
        contactRepository.findByFirstNameIgnoreCaseOrLastNameIgnoreCase(name, name);

    if (contacts.isEmpty()) {
      logger.debug("Found 0 contacts with name {}", name);
      throw new NoSuchElementException("Contact not found with name: " + name);
    }

    logger.debug("Found {} contacts with name {}", contacts.size(), name);

    return contacts;
  }

  public List<Contact> importContacts(MultipartFile file) {
    List<Contact> contacts = new ArrayList<>();

    try (Reader reader = new InputStreamReader(file.getInputStream());
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

      for (CSVRecord csvRecord : csvParser) {
        Contact contact = new Contact();
        contact.setFirstName(csvRecord.get("firstName"));
        contact.setLastName(csvRecord.get("lastName"));
        contact.setTitle(csvRecord.get("title"));
        contact.setDepartment(csvRecord.get("department"));
        contact.setProject(csvRecord.get("project"));
        contact.setAvatar(csvRecord.get("avatar"));
        contact.setEmployeeId(Integer.parseInt(csvRecord.get("employeeId")));

        contacts.add(contact);
      }

      logger.debug(
          "Successfully imported {} contacts from file {}", contacts.size(), file.getName());
      return contactRepository.saveAll(contacts);

    } catch (IOException e) {
      logger.debug("There was an error while importing the {}", file.getName());
      throw new RuntimeException("Failed to import contacts from CSV file", e);
    }
  }

  public void exportContacts(HttpServletResponse response) {
    try {
      List<Contact> contacts = contactRepository.findAll();

      response.setContentType("text/csv");
      response.setHeader("Content-Disposition", "attachment; filename=contacts.csv");

      StringBuilder csvContent = new StringBuilder();
      csvContent.append("id,firstName,lastName,title,department,project,avatar,employeeId\n");

      for (Contact contact : contacts) {
        csvContent
            .append(contact.getId())
            .append(",")
            .append(contact.getFirstName())
            .append(",")
            .append(contact.getLastName())
            .append(",")
            .append(contact.getTitle())
            .append(",")
            .append(contact.getDepartment())
            .append(",")
            .append(contact.getProject())
            .append(",")
            .append(contact.getAvatar())
            .append(",")
            .append(contact.getEmployeeId())
            .append("\n");
      }

      // Write the CSV content to the response
      PrintWriter writer = response.getWriter();
      writer.write(csvContent.toString());
      writer.flush();
      logger.debug("Successfully exported {} contacts to CSV file", contacts.size());
    } catch (IOException e) {
      logger.debug("Failed to export contacts to CSV file");
      throw new RuntimeException("Failed to export contacts to CSV file", e);
    }
  }
}
