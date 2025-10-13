package api.metric;

import io.micrometer.core.instrument.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserResourceMetric {
  private final MeterRegistry meterRegistry;

  private final ConcurrentHashMap<String, Counter> requestCounters = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Gauge> memoryGauges = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Counter> networkInCounters = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Counter> networkOutCounters = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Counter> cpuTimeCounters = new ConcurrentHashMap<>();

  private final ConcurrentHashMap<String, Double> memoryUsageMap = new ConcurrentHashMap<>();

  /** 🔹 Счётчик HTTP-запросов пользователя */
  public void incrementRequests(String username) {
    requestCounters
        .computeIfAbsent(username, name -> Counter.builder("user_requests_total")
            .description("Total number of requests by user")
            .tag("user", name)
            .register(meterRegistry))
        .increment();
  }

  /** 🔹 Устанавливает usage памяти пользователя (в MB) */
  public void setMemoryUsage(String username, double memoryMb) {
    memoryUsageMap.put(username, memoryMb);
    memoryGauges.computeIfAbsent(username, name ->
        Gauge.builder("user_memory_usage_mb", memoryUsageMap, map -> map.getOrDefault(name, 0.0))
            .description("Current memory usage by user in MB")
            .tag("user", name)
            .register(meterRegistry)
    );
  }

  /** 🔹 Увеличивает входящий сетевой трафик (в байтах) */
  public void addNetworkIn(String username, long bytes) {
    networkInCounters
        .computeIfAbsent(username, name -> Counter.builder("user_network_in_bytes_total")
            .description("Total incoming traffic (bytes) per user")
            .tag("user", name)
            .baseUnit("bytes")
            .register(meterRegistry))
        .increment(bytes);
  }

  /** 🔹 Увеличивает исходящий сетевой трафик (в байтах) */
  public void addNetworkOut(String username, long bytes) {
    networkOutCounters
        .computeIfAbsent(username, name -> Counter.builder("user_network_out_bytes_total")
            .description("Total outgoing traffic (bytes) per user")
            .tag("user", name)
            .baseUnit("bytes")
            .register(meterRegistry))
        .increment(bytes);
  }

  /** 🔹 Увеличивает время CPU пользователя (в миллисекундах) */
  public void addCpuTime(String username, long cpuTimeMs) {
    cpuTimeCounters
        .computeIfAbsent(username, name -> Counter.builder("user_cpu_time_ms_total")
            .description("Total CPU processing time (ms) per user")
            .tag("user", name)
            .baseUnit("milliseconds")
            .register(meterRegistry))
        .increment(cpuTimeMs);
  }

  public double getCounterValue(String metricName, String username) {
    return switch (metricName) {
      case "user_requests_total" ->
          requestCounters.getOrDefault(username, Counter.builder(metricName).register(meterRegistry)).count();
      case "user_cpu_time_ms_total" ->
          cpuTimeCounters.getOrDefault(username, Counter.builder(metricName).register(meterRegistry)).count();
      case "user_network_in_bytes_total" ->
          networkInCounters.getOrDefault(username, Counter.builder(metricName).register(meterRegistry)).count();
      case "user_network_out_bytes_total" ->
          networkOutCounters.getOrDefault(username, Counter.builder(metricName).register(meterRegistry)).count();
      default -> 0.0;
    };
  }

  public double getMemoryUsage(String username) {
    return memoryUsageMap.getOrDefault(username, 0.0);
  }
}
