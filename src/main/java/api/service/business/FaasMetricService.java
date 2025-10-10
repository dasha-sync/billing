package api.service.business;

import api.dto.kafka.MetricsEvent;
import api.exception.GlobalException;
import api.model.FaasMetric;
import api.model.User;
import api.repository.FaasMetricRepository;
import api.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FaasMetricService {
  private final FaasMetricRepository faasMetricRepository;
  private final UserRepository userRepository;

  @Transactional
  public void saveMetrics(String username, MetricsEvent event) {
    User user = userRepository.findUserByUsername(username)
        .orElseThrow(() -> new GlobalException("User not found: " + username, "NOT_FOUND"));

    List<FaasMetric> metricsToSave = buildMetrics(user, event);
    faasMetricRepository.saveAll(metricsToSave);
    log.debug("Saved {} metrics for user={} func={}", metricsToSave.size(), username, event.getFuncName());
  }

  private List<FaasMetric> buildMetrics(User user, MetricsEvent event) {
    return event.toMetricsMap().entrySet().stream()
        .filter(e -> e.getValue() != null && e.getValue() != 0)
        .map(e -> new FaasMetric(null, user, event.getFuncName(), e.getKey(), e.getValue(), null))
        .toList();
  }
}
