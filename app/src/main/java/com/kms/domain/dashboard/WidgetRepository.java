package com.kms.domain.dashboard;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface WidgetRepository extends JpaRepository<Widget, Integer> {
  @Modifying
  @Transactional
  @Query("DELETE FROM Widget w WHERE w.dashboard.id = :dashboardId")
  void deleteByDashboardId(int dashboardId);
}
