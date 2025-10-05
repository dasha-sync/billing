package api.dto.subscription;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscriptionResponse {
  private BigDecimal amount;
  private String apiKey;
}
