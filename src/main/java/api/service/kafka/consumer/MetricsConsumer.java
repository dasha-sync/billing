package api.service.kafka.consumer;

import api.dto.kafka.MetricsEvent;
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

  @Value("${app.kafka.metrics-topic:metrics}")
  private String metricsTopic;

  @KafkaListener(topics = "${app.kafka.metrics-topic:metrics}")
  public void handleMetrics(@Payload MetricsEvent event) {
    if (event == null) {
      log.warn("Received null MetricsEvent");
      return;
    }

    try {
      String username = jwtTokenProvider.getNameFromJwt(event.getApiKey());
      log.info(
          "Metrics received: user={}, func={}, m1={}, m2={}, m3={}",
          username,
          event.getFuncName(),
          event.getMetric1(),
          event.getMetric2(),
          event.getMetric3());
      // TODO: persist or process metrics as needed
    } catch (Exception ex) {
      log.warn("Invalid apiKey in MetricsEvent for func {}: {}", event.getFuncName(), ex.getMessage());
    }
  }
}
