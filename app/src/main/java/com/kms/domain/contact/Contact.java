package com.kms.domain.contact;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Contact {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String firstName;
  private String lastName;
  private String title;
  private String department;
  private String project;
  private String avatar;
  private Integer employeeId;
}
