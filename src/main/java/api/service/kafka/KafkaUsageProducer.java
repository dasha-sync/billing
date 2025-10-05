package api.service.kafka;

import api.dto.kafka.UsageEvent;
import api.model.BillingSubscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaUsageProducer {

  private static final String TOPIC_NAME = "stripe-usage-topic";
  private final KafkaTemplate<String, UsageEvent> kafkaTemplate;

  public void sendUsageEvent(BillingSubscription subscription) {
    if (subscription == null || subscription.getUser() == null) {
      log.warn("Skipping null subscription or user");
      return;
    }

    String customerId = subscription.getUser().getStripeCustomerId();
    if (isBlank(customerId)) {
      log.warn("Subscription {} has no stripe_customer_id, skipping", subscription.getId());
      return;
    }

    sendUsageEvent(customerId, subscription.getAmount().longValue());
  }

  public void sendUsageEvent(String customerId, long units) {
    if (isBlank(customerId)) {
      log.warn("Customer ID is null or blank, skipping usage event");
      return;
    }

    UsageEvent event = new UsageEvent(customerId, units, System.currentTimeMillis());
    kafkaTemplate.send(TOPIC_NAME, customerId, event)
        .whenComplete((result, ex) -> {
          if (ex == null) {
            log.info("✅ Sent usage event for customer={} units={}", customerId, units);
          } else {
            log.error("❌ Failed to send usage event for customer={}: {}", customerId, ex.getMessage());
          }
        });
  }

  private boolean isBlank(String str) {
    return str == null || str.isBlank();
  }
}
