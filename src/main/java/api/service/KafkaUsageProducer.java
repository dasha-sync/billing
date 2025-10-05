package api.service;

import api.dto.kafka.UsageEvent;
import api.model.BillingSubscription;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaUsageProducer {

  private final KafkaTemplate<String, UsageEvent> kafkaTemplate;

  private static final String TOPIC_NAME = "stripe-usage-topic";

  public void sendUsageEvent(BillingSubscription subscription) {
    try {
      long units = subscription.getAmount().longValue();

      UsageEvent usageEvent = new UsageEvent(
          subscription.getUser().getStripeCustomerId(), // используем stripe_subscription_id как subscriptionItemId
          units,
          System.currentTimeMillis());

      CompletableFuture<SendResult<String, UsageEvent>> future = kafkaTemplate.send(TOPIC_NAME,
          subscription.getUser().getStripeCustomerId(), usageEvent);

      future.whenComplete((result, ex) -> {
        if (ex == null) {
          log.info("Successfully sent usage event for customer {} with units: {}",
              subscription.getUser().getStripeCustomerId(), units);
        } else {
          log.error("Failed to send usage event for customer {}: {}",
              subscription.getUser().getStripeCustomerId(), ex.getMessage());
        }
      });

    } catch (Exception e) {
      log.error("Error creating usage event for customer {}: {}",
          subscription.getUser().getStripeCustomerId(), e.getMessage());
    }
  }

  public void sendUsageEvent(String customerId, long units) {
    try {
      UsageEvent usageEvent = new UsageEvent(
          customerId,
          units,
          System.currentTimeMillis());

      CompletableFuture<SendResult<String, UsageEvent>> future = kafkaTemplate.send(TOPIC_NAME,
          customerId, usageEvent);

      future.whenComplete((result, ex) -> {
        if (ex == null) {
          log.info("Successfully sent usage event for customer {} with units: {}",
              customerId, units);
        } else {
          log.error("Failed to send usage event for customer {}: {}",
              customerId, ex.getMessage());
        }
      });

    } catch (Exception e) {
      log.error("Error creating usage event for customer {}: {}",
          customerId, e.getMessage());
    }
  }
}
