package com.kms.domain.contact;

import com.kms.domain.contact.dto.SaveContactRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
public class ContactControllerImpl implements ContactController {

  private final ContactService contactService;

  @Override
  public Contact getContact(int id) {
    return contactService.getContact(id);
  }

  @Override
  public Contact updateContact(int id, @Valid SaveContactRequest request) {
    return contactService.saveContact(id, request);
  }

  @Override
  public Contact deleteContact(int id) {
    return contactService.deleteContact(id);
  }

  @Override
  public List<Contact> getAllContacts() {
    return contactService.getAllContacts();
  }

  @Override
  public List<Contact> addContacts(@Valid List<SaveContactRequest> saveContactRequest) {
    return contactService.addContacts(saveContactRequest);
  }

  @Override
  public List<Contact> getContactsByName(String name) {
    return contactService.getContactsByName(name);
  }

  @Override
  public List<Contact> importContacts(MultipartFile file) {
    return contactService.importContacts(file);
  }

  @Override
  public void exportContacts(HttpServletResponse response) throws IOException {
    contactService.exportContacts(response);
  }
}
