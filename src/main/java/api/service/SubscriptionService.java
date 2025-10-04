package api.service;

import api.dto.subscription.SubscriptionResponse;
import api.exception.GlobalException;
import api.model.BillingSubscription;
import api.model.Card;
import api.model.User;
import api.repository.BillingSubscriptionRepository;
import api.repository.CardRepository;
import api.repository.UserRepository;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionCreateParams;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class SubscriptionService {
  private final StripeService stripeService;
  private final BillingSubscriptionRepository billingSubscriptionRepository;
  private final UserRepository userRepository;
  private final CardRepository cardRepository;

  private static final String PRICE_ID = "price_1SEAYn1g3VqUNGQrRQvLmpui"; // твой price ID

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

    Subscription stripeSubscription = createStripeSubscription(
        customerId, card.getPaymentMethodId());

    BillingSubscription billingSubscription = buildBillingSubscription(
        user, card.getPaymentMethodId(), stripeSubscription.getId());

    return mapToDto(billingSubscriptionRepository.save(billingSubscription));
  }

  public SubscriptionResponse cancelSubscription(String username) throws Exception {
    User user = getUser(username);
    BillingSubscription subscription = getActiveSubscription(user);

    stripeService.cancelStripeSubscription(subscription.getStripeSubscriptionId());
    subscription.setStatus(BillingSubscription.SubscriptionStatus.CANCELLED);

    return mapToDto(billingSubscriptionRepository.save(subscription));
  }

  public SubscriptionResponse updatePaymentMethod(Long cardId, String username) throws Exception {
    User user = getUser(username);
    BillingSubscription subscription = getActiveSubscription(user);

    Card card = cardRepository.findById(cardId)
        .orElseThrow(() -> new GlobalException("Card not found", "NOT_FOUND"));

    if (!card.getUser().getId().equals(user.getId())) {
      throw new GlobalException("Card does not belong to the user", "CONFLICT");
    }

    String customerId = stripeService.createCustomerIfNotExists(user);
    stripeService.attachPaymentMethodIfNeeded(card.getPaymentMethodId(), customerId);
    stripeService.updateStripeSubscriptionPaymentMethod(
        subscription.getStripeSubscriptionId(), card.getPaymentMethodId());

    subscription.setPaymentMethodId(card.getPaymentMethodId());
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

  // Создание подписки через Stripe с Price ID и quantity
  private Subscription createStripeSubscription(
      String customerId, String paymentMethodId) throws Exception {
    SubscriptionCreateParams params = SubscriptionCreateParams.builder()
        .setCustomer(customerId)
        .addItem(SubscriptionCreateParams.Item.builder()
            .setPrice(PRICE_ID)
            .build())
        .setDefaultPaymentMethod(paymentMethodId)
        .build();

    return Subscription.create(params);
  }

  private User getUser(String username) {
    return userRepository.findUserByUsername(username)
        .orElseThrow(() -> new GlobalException("User not found", "NOT_FOUND"));
  }

  private BillingSubscription buildBillingSubscription(
      User user, String paymentMethodId, String stripeSubscriptionId) {
    BillingSubscription subscription = new BillingSubscription();
    subscription.setUser(user);
    subscription.setAmount(BigDecimal.valueOf(0)); // количество units
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
