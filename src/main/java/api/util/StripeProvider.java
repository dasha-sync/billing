package api.util;

import api.model.BillingSubscription;
import api.model.Card;
import api.model.User;
import api.repository.CardRepository;
import api.repository.UserRepository;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionCreateParams;
import java.math.BigDecimal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StripeProvider {
  private final UserRepository userRepository;
  private final CardRepository cardRepository;

  @Value("${stripe.api.price-id}")
  private String priceId;

  // -------------------- Customer / Payment --------------------

  public String createCustomerIfNotExists(User user) throws Exception {
    if (user.getStripeCustomerId() != null) return user.getStripeCustomerId();

    Customer customer = Customer.create(Map.of("email", user.getEmail()));
    user.setStripeCustomerId(customer.getId());
    userRepository.save(user);
    return customer.getId();
  }

  public void attachPaymentMethodIfNeeded(String paymentMethodId, String customerId) throws Exception {
    PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);
    if (pm.getCustomer() == null) {
      pm.attach(Map.of("customer", customerId));
    }
  }

  public Card attachCard(User user, String paymentMethodId) throws Exception {
    String customerId = createCustomerIfNotExists(user);
    PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);
    pm.attach(Map.of("customer", customerId));

    Card card = new Card();
    card.setPaymentMethodId(pm.getId());
    card.setBrand(pm.getCard().getBrand());
    card.setLast4(pm.getCard().getLast4());
    card.setExpMonth(pm.getCard().getExpMonth());
    card.setExpYear(pm.getCard().getExpYear());
    card.setUser(user);

    return cardRepository.save(card);
  }

  // -------------------- Subscription --------------------

  public BillingSubscription createSubscription(User user, Card card) throws Exception {
    String customerId = createCustomerIfNotExists(user);
    attachPaymentMethodIfNeeded(card.getPaymentMethodId(), customerId);

    Subscription stripeSub = createStripeSubscription(customerId, card.getPaymentMethodId());
    return buildBillingSubscription(user, card, stripeSub);
  }

  public void cancelSubscription(BillingSubscription subscription) throws Exception {
    Subscription.retrieve(subscription.getStripeSubscriptionId()).cancel();
  }

  public void updateSubscriptionPaymentMethod(BillingSubscription subscription, Card card) throws Exception {
    String customerId = createCustomerIfNotExists(card.getUser());
    attachPaymentMethodIfNeeded(card.getPaymentMethodId(), customerId);

    Subscription stripeSub = Subscription.retrieve(subscription.getStripeSubscriptionId());
    stripeSub.update(Map.of("default_payment_method", card.getPaymentMethodId()));
  }

  private Subscription createStripeSubscription(String customerId, String paymentMethodId) throws Exception {
    SubscriptionCreateParams params = SubscriptionCreateParams.builder()
        .setCustomer(customerId)
        .addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build())
        .setDefaultPaymentMethod(paymentMethodId)
        .build();

    return Subscription.create(params);
  }

  private BillingSubscription buildBillingSubscription(User user, Card card, Subscription stripeSub) {
    BillingSubscription sub = new BillingSubscription();
    sub.setUser(user);
    sub.setAmount(BigDecimal.ZERO);
    sub.setPaymentMethodId(card.getPaymentMethodId());
    sub.setStripeSubscriptionId(stripeSub.getId());
    sub.setStatus(BillingSubscription.SubscriptionStatus.ACTIVE);
    return sub;
  }
}
