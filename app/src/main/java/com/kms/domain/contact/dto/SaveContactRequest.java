package com.kms.domain.contact.dto;

import com.kms.domain.contact.Contact;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveContactRequest {

  @NotBlank(message = "Firstname cannot be blank")
  private String firstName;

  @NotBlank(message = "Lastname cannot be blank")
  private String lastName;

  private String title;
  private String department;
  private String project;
  private String avatar;

  @NotNull(message = "Employee ID cannot be null")
  @Min(value = 1, message = "Employee ID must be a positive number")
  private Integer employeeId;

  public void updateContact(Contact contact) {
    contact.setFirstName(firstName);
    contact.setLastName(lastName);
    contact.setTitle(title);
    contact.setDepartment(department);
    contact.setProject(project);
    contact.setAvatar(avatar);
    contact.setEmployeeId(employeeId);
  }

  public Contact toContact() {
    Contact contact = new Contact();
    updateContact(contact);
    return contact;
  }
}
