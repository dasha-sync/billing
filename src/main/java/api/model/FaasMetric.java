package api.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Entity
@Table(name = "faas_metrics")
@AllArgsConstructor
public class FaasMetric {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "func_name", nullable = false)
  private String funcName;

  @Column(name = "metric_name", nullable = false)
  private String metricName; // metric1 / metric2 / metric3

  @Column(name = "metric_value", nullable = false)
  private Long metricValue;

  @CreationTimestamp
  @Column(name = "ts", nullable = false, updatable = false)
  private OffsetDateTime ts;
}

