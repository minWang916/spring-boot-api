package com.kms.domain.contact;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
  Optional<Contact> findById(Integer id);

  void deleteById(Integer id);

  List<Contact> findAll();

  List<Contact> findByFirstNameIgnoreCaseOrLastNameIgnoreCase(String firstName, String lastName);

  @Query("SELECT c.firstName, COUNT(c) FROM Contact c GROUP BY c.firstName")
  List<Object[]> countContactsByFirstName();

  @Query("SELECT c.lastName, COUNT(c) FROM Contact c GROUP BY c.lastName")
  List<Object[]> countContactsByLastName();

  @Query("SELECT c.title, COUNT(c) FROM Contact c GROUP BY c.title")
  List<Object[]> countContactsByTitle();

  @Query("SELECT c.department, COUNT(c) FROM Contact c GROUP BY c.department")
  List<Object[]> countContactsByDepartment();

  @Query("SELECT c.project, COUNT(c) FROM Contact c GROUP BY c.project")
  List<Object[]> countContactsByProject();

  @Query("SELECT c.avatar, COUNT(c) FROM Contact c GROUP BY c.avatar")
  List<Object[]> countContactsByAvatar();
}
