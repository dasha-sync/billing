package api.dto.subscription;

import api.model.BillingSubscription;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class SubscriptionResponse {
  private Long id;
  private BigDecimal amount;
  private String paymentMethodId;
  private String stripeSubscriptionId;
  private BillingSubscription.SubscriptionStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
