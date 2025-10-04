package api.util;

import api.model.Session;
import api.model.User;
import api.repository.SessionRepository;
import jakarta.servlet.http.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@AllArgsConstructor
public class SessionProvider {
  private static final int COOKIE_MAX_AGE = 259200; // 3 days in seconds
  private final SessionRepository sessionRepository;

  public void clearAuthCookies(HttpServletResponse response) {
    addCookie(response, "sessionId", null, 0);
  }

  public void addCookie(HttpServletResponse response, String name, String value, int expiration) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(false); // Consider true for HTTPS
    cookie.setPath("/");
    cookie.setMaxAge(expiration);
    response.addCookie(cookie);
  }

  @Transactional
  public void setAuthCookies(HttpServletResponse response, String jwt, User user) {
    String sessionId = UUID.randomUUID().toString();

    Session session = new Session();
    session.setSessionId(sessionId);
    session.setJwt(jwt);
    session.setUsername(user.getUsername());
    session.setEmail(user.getEmail());
    session.setExpiresAt(LocalDateTime.now().plusSeconds(COOKIE_MAX_AGE));
    sessionRepository.save(session);

    addCookie(response, "sessionId", sessionId, COOKIE_MAX_AGE);
  }

  public Optional<Session> findBySessionCookie(HttpServletRequest request) {
    Map<String, String> cookies = extractCookies(request);
    String sessionId = cookies.get("sessionId");
    if (sessionId == null) {
      return Optional.empty();
    }
    return sessionRepository.findBySessionId(sessionId)
        .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(LocalDateTime.now()));
  }

  @Transactional
  public void deleteSessionByRequest(HttpServletRequest request) {
    Map<String, String> cookies = extractCookies(request);
    String sessionId = cookies.get("sessionId");
    if (sessionId != null) {
      sessionRepository.deleteBySessionId(sessionId);
    }
  }

  public Map<String, String> extractCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Collections.emptyMap();
    }

    return Arrays.stream(cookies)
        .collect(Collectors.toMap(Cookie::getName, Cookie::getValue, (a, b) -> b));
  }
}
