package api.controller;

import api.dto.common.MetricResponse;
import api.service.business.FaasMetricService;
import api.service.business.UserService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/faas")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class FaasMetricController {
  private final FaasMetricService faasMetricService;
  private final UserService userService;

  @GetMapping
  public ResponseEntity<MetricResponse<List<String>>> getFaas(Principal principal) {
    List<String> faas = faasMetricService.getFaas(principal);
    Long userId = userService.getCurrentUser(principal).getId();
    List<String> data = faas.isEmpty() ? null : faas;

    return ResponseEntity.ok(new MetricResponse<>("User FaaS functions", userId, data));
  }
}
