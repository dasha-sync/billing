package api.dto.card;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CardResponse {
  private Long id;
  private String brand;
  private String last4;
  private Long expMonth;
  private Long expYear;
}
