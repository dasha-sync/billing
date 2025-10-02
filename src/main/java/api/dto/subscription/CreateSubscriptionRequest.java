package api.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSubscriptionRequest {
    @NotBlank(message = "Payment method ID is required")
    private String paymentMethodId;
}
