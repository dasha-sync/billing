package api.service;

import api.dto.subscription.SubscriptionResponse;
import api.exception.GlobalException;
import api.model.BillingSubscription;
import api.model.Card;
import api.model.User;
import api.repository.BillingSubscriptionRepository;
import api.repository.CardRepository;
import api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

  private final StripeService stripeService;
  private final BillingSubscriptionRepository billingSubscriptionRepository;
  private final UserRepository userRepository;
  private final CardRepository cardRepository;

  public SubscriptionResponse createSubscription(Long cardId, String username) throws Exception {
    User user = findUser(username);
    ensureNoActiveSubscription(user);
    Card card = validateUserCard(cardId, user);

    BillingSubscription subscription = stripeService.createSubscription(user, card);
    BillingSubscription saved = billingSubscriptionRepository.save(subscription);

    return mapToDto(saved);
  }

  public SubscriptionResponse cancelSubscription(String username) throws Exception {
    User user = findUser(username);
    BillingSubscription subscription = findActiveSubscription(user);

    stripeService.cancelSubscription(subscription);
    subscription.setStatus(BillingSubscription.SubscriptionStatus.CANCELLED);

    return mapToDto(billingSubscriptionRepository.save(subscription));
  }

  public SubscriptionResponse updatePaymentMethod(Long cardId, String username) throws Exception {
    User user = findUser(username);
    BillingSubscription subscription = findActiveSubscription(user);
    Card card = validateUserCard(cardId, user);

    stripeService.updateSubscriptionPaymentMethod(subscription, card);
    subscription.setPaymentMethodId(card.getPaymentMethodId());

    return mapToDto(billingSubscriptionRepository.save(subscription));
  }

  public SubscriptionResponse getSubscription(String username) {
    return mapToDto(findActiveSubscription(findUser(username)));
  }

  private User findUser(String username) {
    return userRepository.findUserByUsername(username)
        .orElseThrow(() -> new GlobalException("User not found", "NOT_FOUND"));
  }

  private Card validateUserCard(Long cardId, User user) {
    Card card = cardRepository.findById(cardId)
        .orElseThrow(() -> new GlobalException("Card not found", "NOT_FOUND"));

    if (!card.getUser().getId().equals(user.getId())) {
      throw new GlobalException("Card does not belong to the user", "CONFLICT");
    }
    return card;
  }

  private void ensureNoActiveSubscription(User user) {
    billingSubscriptionRepository.findByUserAndStatus(user, BillingSubscription.SubscriptionStatus.ACTIVE)
        .ifPresent(s -> { throw new GlobalException("User already has an active subscription", "CONFLICT"); });
  }

  private BillingSubscription findActiveSubscription(User user) {
    return billingSubscriptionRepository.findByUserAndStatus(user, BillingSubscription.SubscriptionStatus.ACTIVE)
        .orElseThrow(() -> new GlobalException("No active subscription found", "NOT_FOUND"));
  }

  private SubscriptionResponse mapToDto(BillingSubscription sub) {
    return new SubscriptionResponse(
        sub.getId(),
        sub.getAmount(),
        sub.getPaymentMethodId(),
        sub.getStripeSubscriptionId(),
        sub.getStatus(),
        sub.getCreatedAt(),
        sub.getUpdatedAt()
    );
  }
}
