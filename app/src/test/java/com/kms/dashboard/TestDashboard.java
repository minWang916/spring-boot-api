package com.kms.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kms.domain.dashboard.Dashboard;
import com.kms.domain.dashboard.DashboardRepository;
import com.kms.domain.dashboard.Widget;
import com.kms.domain.dashboard.WidgetRepository;
import com.kms.domain.user.User;
import com.kms.domain.user.UserRepository;
import com.kms.utils.jwt.JWTUtils;
import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest()
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestDashboard {
  private final DashboardRepository dashboardRepository;
  private final MockMvc mockMvc;
  private static String token;

  @Autowired
  public TestDashboard(
      DashboardRepository dashboardRepository, MockMvc mockMvc, JWTUtils jwtUtils) {
    this.dashboardRepository = dashboardRepository;
    this.mockMvc = mockMvc;
  }

  @BeforeAll
  static void initDatabase(
      @Autowired UserRepository userRepository,
      @Autowired DashboardRepository dashboardRepository,
      @Autowired WidgetRepository widgetRepository,
      @Autowired JWTUtils jwtUtils) {
    // Clear all data
    userRepository.deleteAll();
    dashboardRepository.deleteAll();
    widgetRepository.deleteAll();

    // Generate a mock user
    User mockUser =
        new User(1, "username_demo_1", "demo1@gmail.com", "matkhau9161", "John Doe 1", "", true);
    userRepository.save(mockUser);
    token = jwtUtils.generateAccessToken("username_demo_1", Map.of("userId", "1"));

    // Generate mock dashboards
    // Dashboard 1 with 2 widgets each with 1 config
    Dashboard dashboard1 = new Dashboard();
    dashboard1.setTitle("Dashboard 1");
    dashboard1.setLayoutType("grid");
    dashboard1.setUser(mockUser);

    Widget widget1 = new Widget();
    widget1.setTitle("Widget 1");
    widget1.setWidgetType("chart");
    widget1.setMinWidth(4);
    widget1.setMinHeight(3);
    widget1.setConfigs(Map.of("config1", "value1"));
    widget1.setDashboard(dashboard1);

    Widget widget2 = new Widget();
    widget2.setTitle("Widget 2");
    widget2.setWidgetType("table");
    widget2.setMinWidth(6);
    widget2.setMinHeight(2);
    widget2.setConfigs(Map.of("config2", "value2"));
    widget2.setDashboard(dashboard1);

    dashboard1.setWidgets(Arrays.asList(widget1, widget2));

    // Dashboard 2 with 1 widget having 3 configs
    Dashboard dashboard2 = new Dashboard();
    dashboard2.setTitle("Dashboard 2");
    dashboard2.setLayoutType("list");
    dashboard2.setUser(mockUser);

    Widget widget3 = new Widget();
    widget3.setTitle("Widget 3");
    widget3.setWidgetType("list");
    widget3.setMinWidth(5);
    widget3.setMinHeight(4);
    widget3.setConfigs(
        Map.of(
            "configA", "valueA",
            "configB", "valueB",
            "configC", "valueC"));
    widget3.setDashboard(dashboard2);

    dashboard2.setWidgets(Arrays.asList(widget3));

    dashboardRepository.saveAll(Arrays.asList(dashboard1, dashboard2));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  void getDashboards_shouldRetrieveAllDashboardsForUser() throws Exception {
    // Perform the GET request to retrieve dashboards for user with ID 1
    mockMvc
        .perform(
            get("/dashboards")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("Dashboard 1"))
        .andExpect(jsonPath("$[0].widgets[0].title").value("Widget 1"))
        .andExpect(jsonPath("$[0].widgets[1].title").value("Widget 2"))
        .andExpect(jsonPath("$[1].title").value("Dashboard 2"))
        .andExpect(jsonPath("$[1].widgets[0].title").value("Widget 3"))
        .andExpect(jsonPath("$[1].widgets[0].configs.configA").value("valueA"))
        .andExpect(jsonPath("$[1].widgets[0].configs.configB").value("valueB"))
        .andExpect(jsonPath("$[1].widgets[0].configs.configC").value("valueC"));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  @Transactional
  void saveDashboard_shouldCreateNewDashboardIfIdDoesNotExist() throws Exception {
    // Manually create the JSON payload for SaveDashboardRequest
    String createRequestJson =
        """
        {
            "title": "Outtro",
            "layoutType": "Ontop",
            "widgets": [
                {
                    "title": "Setting123",
                    "widgetType": "On screen123",
                    "minWidth": 400,
                    "minHeight": 300,
                    "configs": {
                        "Effect": "None",
                        "Direction": "2-way"
                    }
                }
            ]
        }
    """;

    // Perform the PUT request with a non-existing ID
    mockMvc
        .perform(
            put("/dashboards/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson)
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Outtro"))
        .andExpect(jsonPath("$.layoutType").value("Ontop"))
        .andExpect(jsonPath("$.widgets[0].title").value("Setting123"))
        .andExpect(jsonPath("$.widgets[0].widgetType").value("On screen123"))
        .andExpect(jsonPath("$.widgets[0].minWidth").value(400))
        .andExpect(jsonPath("$.widgets[0].minHeight").value(300))
        .andExpect(jsonPath("$.widgets[0].configs.Effect").value("None"))
        .andExpect(jsonPath("$.widgets[0].configs.Direction").value("2-way"));

    // Verify the new dashboard was created in the database
    List<Dashboard> dashboards = dashboardRepository.findAll();
    assertEquals(3, dashboards.size()); // 2 from initialization, 1 newly created
    Dashboard newDashboard =
        dashboards.stream().filter(d -> "Outtro".equals(d.getTitle())).findFirst().orElse(null);
    assertNotNull(newDashboard);
    Assertions.assertEquals("Ontop", newDashboard.getLayoutType());
    Assertions.assertEquals(1, newDashboard.getWidgets().size());
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  @Transactional
  void saveDashboard_shouldUpdateExistingDashboardIfIdExists() throws Exception {
    // Manually create the JSON payload for SaveDashboardRequest
    String updateRequestJson =
        """
        {
            "title": "Updated Outtro",
            "layoutType": "Overlay",
            "widgets": [
                {
                    "title": "Updated Setting123",
                    "widgetType": "Updated On screen123",
                    "minWidth": 500,
                    "minHeight": 400,
                    "configs": {
                        "Effect": "Fade",
                        "Direction": "Left"
                    }
                }
            ]
        }
    """;

    // Perform the PUT request with an existing ID (using the first dashboard's ID, which is 1)
    mockMvc
        .perform(
            put("/dashboards/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestJson)
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated Outtro"))
        .andExpect(jsonPath("$.layoutType").value("Overlay"))
        .andExpect(jsonPath("$.widgets[0].title").value("Updated Setting123"))
        .andExpect(jsonPath("$.widgets[0].widgetType").value("Updated On screen123"))
        .andExpect(jsonPath("$.widgets[0].minWidth").value(500))
        .andExpect(jsonPath("$.widgets[0].minHeight").value(400))
        .andExpect(jsonPath("$.widgets[0].configs.Effect").value("Fade"))
        .andExpect(jsonPath("$.widgets[0].configs.Direction").value("Left"));

    // Verify the dashboard was updated in the database
    Dashboard updatedDashboard = dashboardRepository.findById(1).orElse(null);
    assertNotNull(updatedDashboard);
    Assertions.assertEquals("Updated Outtro", updatedDashboard.getTitle());
    Assertions.assertEquals("Overlay", updatedDashboard.getLayoutType());
    Assertions.assertEquals(1, updatedDashboard.getWidgets().size());
    Widget updatedWidget = updatedDashboard.getWidgets().get(0);
    Assertions.assertEquals("Updated Setting123", updatedWidget.getTitle());
    Assertions.assertEquals("Updated On screen123", updatedWidget.getWidgetType());
    Assertions.assertEquals(500, updatedWidget.getMinWidth());
    Assertions.assertEquals(400, updatedWidget.getMinHeight());
    Assertions.assertEquals("Fade", updatedWidget.getConfigs().get("Effect"));
    Assertions.assertEquals("Left", updatedWidget.getConfigs().get("Direction"));
  }

  @Test
  @WithMockUser(username = "username_demo_1")
  @Transactional
  void saveDashboard_shouldHandleJsonParsingError() throws Exception {
    // Manually create an invalid JSON payload (e.g., missing a closing brace)
    String invalidJson =
        """
        {
            "title"fsdfsdfsdfsype": "grid",
            "widgets": [
                {
                    "title": "Invalid Widget",
                    "widgetType": "chart",
                    "minWiefesfdfsdfd
                    }
                }fdsfsdsfd
            ]
        """; // Missing closing braces for the widget and the entire JSON

    // Perform the PUT request with invalid JSON
    mockMvc
        .perform(
            put("/dashboards/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Error while parsing input JSON:")));
  }
}
