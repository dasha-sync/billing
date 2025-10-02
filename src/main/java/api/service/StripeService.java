package api.service;

import api.model.Card;
import api.model.User;
import api.repository.CardRepository;
import api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentIntent;

import java.util.HashMap;
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

        Map<String, Object> params = new HashMap<>();
        params.put("email", user.getEmail());
        Customer customer = Customer.create(params);

        user.setStripeCustomerId(customer.getId());
        userRepository.save(user);

        return customer.getId();
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
