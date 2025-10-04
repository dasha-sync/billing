package api.dto.card;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddCardRequest {
  @NotBlank(message = "PaymentMethodId is required")
  private String paymentMethodId;
}
