package api.dto.subscription;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class StripeSubscriptionRequest {
  private String customerId;
  private String paymentMethodId;
  private String currency;
  private String productName;
  private Long unitAmount; // в центах
  private String interval; // например, "month"

  public Map<String, Object> toParams() {
    Map<String, Object> subscriptionParams = new HashMap<>();
    subscriptionParams.put("customer", customerId);
    subscriptionParams.put("items", new Object[]{
        Map.of(
            "price_data", Map.of(
                "currency", currency,
                "product_data", Map.of("name", productName),
                "unit_amount", unitAmount
            ),
            "recurring", Map.of("interval", interval)
        )
    });
    subscriptionParams.put("default_payment_method", paymentMethodId);
    subscriptionParams.put("collection_method", "charge_automatically");

    return subscriptionParams;
  }
}


