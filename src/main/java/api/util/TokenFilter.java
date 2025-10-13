package api.util;

import api.model.Session;
import api.service.business.RequestMetricsService;
import api.service.security.JwtAuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class TokenFilter extends OncePerRequestFilter {
  private final SessionProvider sessionProvider;
  private final JwtAuthenticationService authenticationService;
  private final RequestMetricsService metricsService;

  public TokenFilter(
      SessionProvider sessionProvider,
      JwtAuthenticationService authenticationService,
      RequestMetricsService metricsService) {

    this.sessionProvider = sessionProvider;
    this.authenticationService = authenticationService;
    this.metricsService = metricsService;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    long startCpuNs = System.nanoTime();
    var responseWrapper = new ContentLengthTrackingResponseWrapper(response);

    sessionProvider.findBySessionCookie(request)
        .map(Session::getJwt)
        .ifPresent(authenticationService::authenticateIfNecessary);

    String username = authenticationService.getAuthenticatedUsername();

    try {
      filterChain.doFilter(request, responseWrapper);
    } finally {
      if (username != null) {
        metricsService.record(username, startCpuNs, request, responseWrapper);
      }
    }
  }
}
