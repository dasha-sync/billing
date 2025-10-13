package api.service.kafka.producer;

import api.model.BillingSubscription;
import api.repository.BillingSubscriptionRepository;
import api.service.billing.BillingUsageCalculatorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsageEventScheduler {
  private final BillingSubscriptionRepository repository;
  private final KafkaUsageProducer producer;
  private final BillingUsageCalculatorService billingCalculator;

  @Scheduled(fixedRate = 10000) // 10 сек, 300000 - 5 мин
  public void sendActiveSubscriptionsUsage() {
    billingCalculator.recalculateUsage();

    processSubscriptions(
        repository.findByStatusWithUser(BillingSubscription.SubscriptionStatus.ACTIVE),
        "scheduled"
    );
  }

  public void sendSubscriptionUsage(Long subscriptionId) {
    repository.findById(subscriptionId)
        .filter(sub -> sub.getStatus() == BillingSubscription.SubscriptionStatus.ACTIVE)
        .ifPresentOrElse(
            sub -> processSubscriptions(List.of(sub), "manual"),
            () -> log.warn("Subscription {} not active or not found", subscriptionId)
        );
  }

  private void processSubscriptions(List<BillingSubscription> subscriptions, String trigger) {
    try {
      log.info("Found {} {} subscriptions", subscriptions.size(), trigger);
      subscriptions.forEach(producer::sendUsageEvent);
      log.info("Processed {} {} subscriptions", subscriptions.size(), trigger);
    } catch (Exception e) {
      log.error("Error processing {} usage events", trigger, e);
    }
  }
}
