package com.kms.domain.report;

import com.kms.domain.report.dto.ReportResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Tag(name = "Reports", description = "APIs for generating various reports")
public class ReportControllerImpl implements ReportController {
  private final ReportService reportService;

  @Override
  public ReportResponse countByField(@PathVariable String collection, @PathVariable String field) {

    return reportService.countByField(collection, field);
  }
}
