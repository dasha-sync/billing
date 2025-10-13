package api.service.billing;

import api.metrics.UserResourceMetrics;
import api.util.ContentLengthTrackingResponseWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestMetricsService {

  private final UserResourceMetrics metrics;

  public void record(String username, long startCpuNs,
                     HttpServletRequest request,
                     ContentLengthTrackingResponseWrapper responseWrapper) {

    long cpuTimeMs = (System.nanoTime() - startCpuNs) / 1_000_000;
    long bytesIn = Math.max(request.getContentLengthLong(), 0);
    long bytesOut = responseWrapper.getContentLength();
    double memUsageMb = getMemoryUsageMb();

    metrics.incrementRequests(username);
    metrics.addCpuTime(username, cpuTimeMs);
    metrics.addNetworkIn(username, bytesIn);
    metrics.addNetworkOut(username, bytesOut);
    metrics.setMemoryUsage(username, memUsageMb);
  }

  private double getMemoryUsageMb() {
    Runtime rt = Runtime.getRuntime();
    return (rt.totalMemory() - rt.freeMemory()) / (1024.0 * 1024.0);
  }
}
