package api.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricsEvent {
  private String apiKey; // JWT token
  private String funcName;
  private Long metric1;
  private Long metric2;
  private Long metric3;
}
