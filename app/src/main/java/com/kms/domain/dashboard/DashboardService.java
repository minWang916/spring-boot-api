package com.kms.domain.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kms.domain.dashboard.dto.SaveDashboardRequest;
import com.kms.domain.user.User;
import com.kms.domain.user.UserRepository;
import com.kms.utils.appuser.AppUserService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DashboardService {

  private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

  private final DashboardRepository dashboardRepository;
  private final AppUserService appUserService;
  private final WidgetRepository widgetRepository;
  private final ObjectMapper objectMapper;
  private final UserRepository userRepository;

  List<Dashboard> getDashboards(int userId) {
    List<Dashboard> dashboards = dashboardRepository.findByUser_Id(userId);
    logger.debug("Retrieved {} dashboards for user with ID {}", dashboards.size(), userId);
    return dashboards;
  }

  Dashboard saveDashboard(int dashboardId, SaveDashboardRequest saveDashboardRequest, int userId) {

    Optional<Dashboard> optionalDashboard =
        dashboardRepository.findByIdAndUser_Id(dashboardId, userId);

    if (optionalDashboard.isPresent()) {
      Dashboard dashboard = optionalDashboard.get();
      logger.debug(
          "Dashboard with ID {} found. Updating the existing dashboard with ID {}.",
          userId,
          dashboardId);
      return updateDashboard(dashboard, saveDashboardRequest, userId);
    } else {
      logger.debug("Dashboard with ID {} not found. Creating a new dashboard.", dashboardId);
      return createDashboard(saveDashboardRequest, userId);
    }
  }

  @Transactional
  Dashboard updateDashboard(
      Dashboard dashboard, SaveDashboardRequest saveDashboardRequest, int userId) {
    User user = userRepository.findById(userId).get();
    dashboard.setUser(user);
    dashboard.setTitle(saveDashboardRequest.getTitle());
    dashboard.setLayoutType(saveDashboardRequest.getLayoutType());

    widgetRepository.deleteByDashboardId(dashboard.getId());
    dashboard.getWidgets().clear();

    List<Widget> newWidgets =
        saveDashboardRequest.getWidgets().stream()
            .map(
                widgetRequest -> {
                  Widget widget = new Widget();
                  widget.setTitle(widgetRequest.getTitle());
                  widget.setWidgetType(widgetRequest.getWidgetType());
                  widget.setMinWidth(widgetRequest.getMinWidth());
                  widget.setMinHeight(widgetRequest.getMinHeight());
                  widget.setConfigs(widgetRequest.getConfigs());
                  widget.setDashboard(dashboard); // Set the dashboard reference
                  return widget;
                })
            .collect(Collectors.toList());

    dashboard.setWidgets(newWidgets);

    return dashboardRepository.save(dashboard);
  }

  Dashboard createDashboard(SaveDashboardRequest saveDashboardRequest, int userId) {
    Dashboard dashboard = new Dashboard();

    User user = userRepository.findById(userId).get();
    dashboard.setUser(user);
    dashboard.setTitle(saveDashboardRequest.getTitle());
    dashboard.setLayoutType(saveDashboardRequest.getLayoutType());

    List<Widget> newWidgets =
        saveDashboardRequest.getWidgets().stream()
            .map(
                widgetRequest -> {
                  Widget widget = new Widget();
                  widget.setTitle(widgetRequest.getTitle());
                  widget.setWidgetType(widgetRequest.getWidgetType());
                  widget.setMinWidth(widgetRequest.getMinWidth());
                  widget.setMinHeight(widgetRequest.getMinHeight());
                  widget.setConfigs(widgetRequest.getConfigs());
                  widget.setDashboard(dashboard); // Set the dashboard reference
                  return widget;
                })
            .collect(Collectors.toList());

    dashboard.setWidgets(newWidgets);

    return dashboardRepository.save(dashboard);
  }
}
