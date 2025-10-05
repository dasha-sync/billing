package api.service.kafka;

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

  private final BillingSubscriptionRepository repository;
  private final KafkaUsageProducer producer;

  @Scheduled(fixedRate = 300000)
  public void sendActiveSubscriptionsUsage() {
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
