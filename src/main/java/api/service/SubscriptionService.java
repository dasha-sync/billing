package api.service;

import api.dto.subscription.CreateSubscriptionRequest;
import api.dto.subscription.SubscriptionResponse;
import api.dto.subscription.UpdatePaymentMethodRequest;
import api.exception.GlobalException;
import api.model.Subscription;
import api.repository.SubscriptionRepository;
import api.repository.UserRepository;
import com.stripe.model.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final StripeService stripeService;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request, String username)
            throws Exception {
        var user = getUserByUsername(username);

        // Проверяем, есть ли уже активная подписка
        var existingSubscription = subscriptionRepository.findByUserAndStatus(user,
                Subscription.SubscriptionStatus.ACTIVE);
        if (existingSubscription.isPresent()) {
            throw new GlobalException("User already has an active subscription", "CONFLICT");
        }

        String customerId = stripeService.createCustomerIfNotExists(user);

        // Получаем payment method
        PaymentMethod paymentMethod = PaymentMethod.retrieve(request.getPaymentMethodId());

        // Attach payment method к customer если еще не привязан
        if (paymentMethod.getCustomer() == null) {
            paymentMethod.attach(Map.of("customer", customerId));
        }

        // Создаем подписку в Stripe с amount = 0
        Map<String, Object> subscriptionParams = new HashMap<>();
        subscriptionParams.put("customer", customerId);
        subscriptionParams.put("items", new Object[] {
                Map.of(
                        "price_data", Map.of(
                                "currency", "eur",
                                "product_data", Map.of("name", "Subscription"),
                                "unit_amount", 0 // Начинаем с 0
                        ),
                        "recurring", Map.of("interval", "month"))
        });
        subscriptionParams.put("default_payment_method", request.getPaymentMethodId());
        subscriptionParams.put("collection_method", "charge_automatically");

        com.stripe.model.Subscription stripeSubscription = com.stripe.model.Subscription.create(subscriptionParams);

        // Сохраняем в базу данных
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setAmount(BigDecimal.ZERO);
        subscription.setPaymentMethodId(request.getPaymentMethodId());
        subscription.setStripeSubscriptionId(stripeSubscription.getId());
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        return mapToDto(savedSubscription);
    }

    public SubscriptionResponse cancelSubscription(String username) throws Exception {
        var user = getUserByUsername(username);

        var subscription = subscriptionRepository.findByUserAndStatus(user, Subscription.SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new GlobalException("No active subscription found", "NOT_FOUND"));

        // Отменяем подписку в Stripe
        com.stripe.model.Subscription stripeSubscription = com.stripe.model.Subscription
                .retrieve(subscription.getStripeSubscriptionId());
        stripeSubscription.cancel();

        // Обновляем статус в базе данных
        subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        return mapToDto(savedSubscription);
    }

    public SubscriptionResponse updatePaymentMethod(UpdatePaymentMethodRequest request, String username)
            throws Exception {
        var user = getUserByUsername(username);

        var subscription = subscriptionRepository.findByUserAndStatus(user, Subscription.SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new GlobalException("No active subscription found", "NOT_FOUND"));

        String customerId = stripeService.createCustomerIfNotExists(user);

        // Получаем новый payment method
        PaymentMethod paymentMethod = PaymentMethod.retrieve(request.getPaymentMethodId());

        // Attach payment method к customer если еще не привязан
        if (paymentMethod.getCustomer() == null) {
            paymentMethod.attach(Map.of("customer", customerId));
        }

        // Обновляем payment method в Stripe подписке
        com.stripe.model.Subscription stripeSubscription = com.stripe.model.Subscription
                .retrieve(subscription.getStripeSubscriptionId());
        Map<String, Object> updateParams = Map.of("default_payment_method", request.getPaymentMethodId());
        stripeSubscription.update(updateParams);

        // Обновляем в базе данных
        subscription.setPaymentMethodId(request.getPaymentMethodId());
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        return mapToDto(savedSubscription);
    }

    public SubscriptionResponse getSubscription(String username) {
        var user = getUserByUsername(username);

        var subscription = subscriptionRepository.findByUser(user)
                .orElseThrow(() -> new GlobalException("No subscription found", "NOT_FOUND"));

        return mapToDto(subscription);
    }

    private api.model.User getUserByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new GlobalException("User not found", "NOT_FOUND"));
    }

    private SubscriptionResponse mapToDto(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getAmount(),
                subscription.getPaymentMethodId(),
                subscription.getStripeSubscriptionId(),
                subscription.getStatus(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt());
    }
}
