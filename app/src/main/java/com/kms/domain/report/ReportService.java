package com.kms.domain.report;

import com.kms.domain.contact.Contact;
import com.kms.domain.contact.ContactRepository;
import com.kms.domain.report.dto.ReportResponse;
import com.kms.domain.task.Task;
import com.kms.domain.task.TaskRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.*;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReportService {

  private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

  private final TaskRepository taskRepository;
  private final ContactRepository contactRepository;
  private final EntityManager entityManager;

  public ReportResponse countByField(String collection, String field) {
    logger.debug("Counting by field '{}' for collection '{}'", field, collection);

    Map<String, Long> values = new HashMap<>();

    if ("task".equalsIgnoreCase(collection)) {
      values = countFieldInCollection(Task.class, field);
    } else if ("contact".equalsIgnoreCase(collection)) {
      values = countFieldInCollection(Contact.class, field);
    } else {
      logger.debug("Collection '{}' not found", collection);
      throw new NoSuchElementException("Collection '" + collection + "' not found");
    }

    logger.debug("Successfully counted by field '{}' for collection '{}'", field, collection);
    return new ReportResponse(collection, field, values);
  }

  private <T> Map<String, Long> countFieldInCollection(Class<T> entityClass, String field) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);

    Root<T> root = query.from(entityClass);
    query.multiselect(root.get(field), cb.count(root)).groupBy(root.get(field));

    List<Object[]> results = entityManager.createQuery(query).getResultList();
    return mapResults(results);
  }

  private Map<String, Long> mapResults(List<Object[]> results) {
    Map<String, Long> countMap = new HashMap<>();
    for (Object[] result : results) {
      String key = result[0].toString();
      Long count = (Long) result[1];
      countMap.put(key, count);
    }
    return countMap;
  }
}
