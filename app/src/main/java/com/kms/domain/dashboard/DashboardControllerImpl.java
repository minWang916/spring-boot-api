package com.kms.domain.dashboard;

import com.kms.domain.dashboard.dto.SaveDashboardRequest;
import com.kms.utils.appuser.AppUserService;
import com.kms.utils.jwt.JWTUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboards")
@AllArgsConstructor
@Tag(name = "Dashboards", description = "APIs for managing dashboards")
public class DashboardControllerImpl implements DashboardController {

  private final DashboardService dashboardService;
  private final AppUserService appUserService;
  private final JWTUtils jwtUtils;

  @Override
  public List<Dashboard> getAllDashboards() {
    int userId = Integer.parseInt(jwtUtils.getUserIdFromToken());
    return dashboardService.getDashboards(userId);
  }

  @Override
  public Dashboard saveDashboard(
      @PathVariable int dashboardId,
      @Valid @RequestBody SaveDashboardRequest saveDashboardRequest) {
    int userId = Integer.parseInt(jwtUtils.getUserIdFromToken());
    return dashboardService.saveDashboard(dashboardId, saveDashboardRequest, userId);
  }
}
