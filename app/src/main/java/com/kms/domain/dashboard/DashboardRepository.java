package com.kms.domain.dashboard;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardRepository extends JpaRepository<Dashboard, Integer> {
  List<Dashboard> findByUser_Id(Integer userId);

  Optional<Dashboard> findByIdAndUser_Id(Integer dashboardId, Integer userId);
}
