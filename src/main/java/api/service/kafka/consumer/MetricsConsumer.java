package api.service.kafka.consumer;

import api.dto.kafka.MetricsEvent;
import api.service.business.FaasMetricService;
import api.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsConsumer {
  private final JwtTokenProvider jwtTokenProvider;
  private final FaasMetricService faasMetricService;

  @Value("${app.kafka.metrics-topic:metrics}")
  private String metricsTopic;

  @KafkaListener(topics = "${app.kafka.metrics-topic:metrics}")
  public void handleMetrics(@Payload MetricsEvent event) {
    if (event == null) {
      log.warn("Received null MetricsEvent");
      return;
    }

    String username = extractUsername(event);
    if (username == null) {
      return;
    }

    log.info("Metrics received: user={}, func={}", username, event.getFuncName());
    faasMetricService.saveMetrics(username, event);
  }

  private String extractUsername(MetricsEvent event) {
    try {
      return jwtTokenProvider.getNameFromJwt(event.getApiKey());
    } catch (Exception ex) {
      log.warn("Invalid apiKey for func {}: {}", event.getFuncName(), ex.getMessage());
      return null;
    }
  }
}
