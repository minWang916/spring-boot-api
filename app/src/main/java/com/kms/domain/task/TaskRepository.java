package com.kms.domain.task;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TaskRepository extends JpaRepository<Task, Integer> {
  Optional<Task> findById(int id);

  void deleteById(int id);

  List<Task> findAll();

  List<Task> findByUser_Id(int userId);

  List<Task> findByTaskContainingIgnoreCase(String name);

  @Query("SELECT t.isCompleted, COUNT(t) FROM Task t GROUP BY t.isCompleted")
  List<Object[]> countTasksByIsCompleted();

  @Query("SELECT t.task, COUNT(t) FROM Task t GROUP BY t.task")
  List<Object[]> countTasksByTask();

  @Query("SELECT t.user.username, COUNT(t) FROM Task t GROUP BY t.user.username")
  List<Object[]> countTasksByUser();
}
