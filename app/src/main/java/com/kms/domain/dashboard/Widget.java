package com.kms.domain.dashboard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Map;
import lombok.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Widget {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String title;
  private String widgetType;
  private Integer minWidth;
  private Integer minHeight;

  @ElementCollection
  @CollectionTable(name = "widget_configs", joinColumns = @JoinColumn(name = "id"))
  @MapKeyColumn(name = "config_key")
  @Column(name = "config_value")
  private Map<String, String> configs;

  @ManyToOne
  @JoinColumn(name = "dashboard_id", nullable = false)
  @JsonIgnore
  private Dashboard dashboard;
}
