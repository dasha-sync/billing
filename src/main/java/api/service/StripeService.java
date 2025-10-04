package api.service;

import api.model.Card;
import api.model.User;
import api.repository.CardRepository;
import api.repository.UserRepository;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripeService {
  private final UserRepository userRepository;
  private final CardRepository cardRepository;

  public String createCustomerIfNotExists(User user) throws Exception {
    if (user.getStripeCustomerId() != null) {
      return user.getStripeCustomerId();
    }

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

  public void updateStripeSubscriptionPaymentMethod(String subscriptionId, String paymentMethodId) throws Exception {
    com.stripe.model.Subscription subscription = com.stripe.model.Subscription.retrieve(subscriptionId);
    subscription.update(Map.of("default_payment_method", paymentMethodId));
  }

  public void cancelStripeSubscription(String subscriptionId) throws Exception {
    com.stripe.model.Subscription.retrieve(subscriptionId).cancel();
  }

  public Card attachCard(User user, String paymentMethodId) throws Exception {
    String customerId = createCustomerIfNotExists(user);

    PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);
    pm.attach(Map.of("customer", customerId));

    Card card = new Card();
    card.setPaymentMethodId(pm.getId());
    card.setBrand(pm.getCard().getBrand());
    card.setLast4(pm.getCard().getLast4());
    card.setExpMonth(pm.getCard().getExpMonth().longValue());
    card.setExpYear(pm.getCard().getExpYear().longValue());
    card.setUser(user);

    return cardRepository.save(card);
  }
}
