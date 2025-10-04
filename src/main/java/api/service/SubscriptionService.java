package api.service;

import api.dto.subscription.StripeSubscriptionRequest;
import api.dto.subscription.SubscriptionResponse;
import api.dto.subscription.UpdatePaymentMethodRequest;
import api.exception.GlobalException;
import api.model.BillingSubscription;
import api.model.Card;
import api.model.User;
import api.repository.BillingSubscriptionRepository;
import api.repository.CardRepository;
import api.repository.UserRepository;
import com.stripe.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
  private final StripeService stripeService;
  private final BillingSubscriptionRepository billingSubscriptionRepository;
  private final UserRepository userRepository;
  private final CardRepository cardRepository;

  public SubscriptionResponse createSubscription(Long cardId, String username) throws Exception {
    User user = getUser(username);
    ensureNoActiveSubscription(user);

    Card card = cardRepository.findById(cardId)
        .orElseThrow(() -> new GlobalException("Card not found", "NOT_FOUND"));

    if (!card.getUser().getId().equals(user.getId())) {
      throw new GlobalException("Card does not belong to the user", "CONFLICT");
    }

    String customerId = stripeService.createCustomerIfNotExists(user);
    stripeService.attachPaymentMethodIfNeeded(card.getPaymentMethodId(), customerId);

    Subscription stripeSubscription = createStripeSubscription(customerId, card.getPaymentMethodId());
    BillingSubscription billingSubscription = buildBillingSubscription(user, card.getPaymentMethodId(), stripeSubscription.getId());

    return mapToDto(billingSubscriptionRepository.save(billingSubscription));
  }

  public SubscriptionResponse cancelSubscription(String username) throws Exception {
    User user = getUser(username);
    BillingSubscription subscription = getActiveSubscription(user);

    stripeService.cancelStripeSubscription(subscription.getStripeSubscriptionId());
    subscription.setStatus(BillingSubscription.SubscriptionStatus.CANCELLED);

    return mapToDto(billingSubscriptionRepository.save(subscription));
  }

  public SubscriptionResponse updatePaymentMethod(UpdatePaymentMethodRequest request, String username) throws Exception {
    User user = getUser(username);
    BillingSubscription subscription = getActiveSubscription(user);

    String customerId = stripeService.createCustomerIfNotExists(user);
    stripeService.attachPaymentMethodIfNeeded(request.getPaymentMethodId(), customerId);
    stripeService.updateStripeSubscriptionPaymentMethod(subscription.getStripeSubscriptionId(), request.getPaymentMethodId());

    subscription.setPaymentMethodId(request.getPaymentMethodId());
    return mapToDto(billingSubscriptionRepository.save(subscription));
  }

  public SubscriptionResponse getSubscription(String username) {
    User user = getUser(username);

    BillingSubscription subscription = billingSubscriptionRepository.findByUser(user)
        .orElseThrow(() -> new GlobalException("No subscription found", "NOT_FOUND"));

    return mapToDto(subscription);
  }

  private void ensureNoActiveSubscription(User user) {
    billingSubscriptionRepository.findByUserAndStatus(user, BillingSubscription.SubscriptionStatus.ACTIVE)
        .ifPresent(s -> {
          throw new GlobalException("User already has an active subscription", "CONFLICT");
        });
  }

  private BillingSubscription getActiveSubscription(User user) {
    return billingSubscriptionRepository.findByUserAndStatus(user, BillingSubscription.SubscriptionStatus.ACTIVE)
        .orElseThrow(() -> new GlobalException("No active subscription found", "NOT_FOUND"));
  }

  private Subscription createStripeSubscription(String customerId, String paymentMethodId) throws Exception {
    StripeSubscriptionRequest dto = new StripeSubscriptionRequest(
        customerId,
        paymentMethodId,
        "eur",
        "Subscription",
        0L,
        "month"
    );
    return Subscription.create(dto.toParams());
  }

  private User getUser(String username) {
    return userRepository.findUserByUsername(username)
        .orElseThrow(() -> new GlobalException("User not found", "NOT_FOUND"));
  }

  private BillingSubscription buildBillingSubscription(User user, String paymentMethodId, String stripeSubscriptionId) {
    BillingSubscription subscription = new BillingSubscription();
    subscription.setUser(user);
    subscription.setAmount(BigDecimal.ZERO);
    subscription.setPaymentMethodId(paymentMethodId);
    subscription.setStripeSubscriptionId(stripeSubscriptionId);
    subscription.setStatus(BillingSubscription.SubscriptionStatus.ACTIVE);
    return subscription;
  }

  private SubscriptionResponse mapToDto(BillingSubscription subscription) {
    return new SubscriptionResponse(
        subscription.getId(),
        subscription.getAmount(),
        subscription.getPaymentMethodId(),
        subscription.getStripeSubscriptionId(),
        subscription.getStatus(),
        subscription.getCreatedAt(),
        subscription.getUpdatedAt()
    );
  }
}
