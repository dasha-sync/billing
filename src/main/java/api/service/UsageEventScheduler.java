package api.service;

import api.model.BillingSubscription;
import api.repository.BillingSubscriptionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsageEventScheduler {
  private final BillingSubscriptionRepository billingSubscriptionRepository;
  private final KafkaUsageProducer kafkaUsageProducer;

  @Scheduled(fixedRate = 5000) // 5 сек, 300000 - 5 мин
  public void sendActiveSubscriptionsUsage() {
    try {
      List<BillingSubscription> activeSubscriptions =
          billingSubscriptionRepository.findByStatusWithUser(BillingSubscription.SubscriptionStatus.ACTIVE);


      log.info("Found {} active subscriptions to send usage data", activeSubscriptions.size());

      for (BillingSubscription subscription : activeSubscriptions) {
        if (subscription.getUser().getStripeCustomerId() != null
            && !subscription.getUser().getStripeCustomerId().isEmpty()) {
          kafkaUsageProducer.sendUsageEvent(subscription);
        } else {
          log.warn("Subscription {} has no stripe_subscription_id, skipping", subscription.getId());
        }
      }

      log.info("Successfully processed {} active subscriptions", activeSubscriptions.size());

    } catch (Exception e) {
      log.error("Error sending usage events for active subscriptions: {}", e.getMessage(), e);
    }
  }

  public void sendSubscriptionUsage(Long stripeCustomerId) {
    try {
      BillingSubscription subscription = billingSubscriptionRepository.findById(stripeCustomerId)
            .orElseThrow(() -> new RuntimeException("Customer not found with id: " + stripeCustomerId));

      if (subscription.getStatus() == BillingSubscription.SubscriptionStatus.ACTIVE) {
        if (subscription.getUser().getStripeCustomerId() != null
               && !subscription.getUser().getStripeCustomerId().isEmpty()) {
          kafkaUsageProducer.sendUsageEvent(subscription);
          log.info("Manually sent usage event for customer {}", stripeCustomerId);
        } else {
          log.warn("Customer {} has no stripe_customer_id, cannot send usage event", stripeCustomerId);
        }
      } else {
        log.warn("Subscription {} is not active, status: {}", stripeCustomerId, subscription.getStatus());
      }

    } catch (Exception e) {
      log.error("Error sending usage event for subscription {}: {}", stripeCustomerId, e.getMessage(), e);
    }
  }
}
