package com.kms.domain.dashboard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kms.domain.user.User;
import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Dashboard {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  @JsonIgnore
  private User user;

  private String title;
  private String layoutType;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "dashboard_id")
  private List<Widget> widgets;
}
