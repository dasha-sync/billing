package api.dto.subscription;

import api.model.Subscription;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SubscriptionResponse {
    private Long id;
    private BigDecimal amount;
    private String paymentMethodId;
    private String stripeSubscriptionId;
    private Subscription.SubscriptionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
