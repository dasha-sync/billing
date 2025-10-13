package api.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class TokenCache {
  private static final long TTL_MS = 5 * 60 * 1000; // 5 минут
  private final Map<String, CachedAuth> cache = new ConcurrentHashMap<>();

  public void put(String jwt, UsernamePasswordAuthenticationToken auth) {
    cache.put(jwt, new CachedAuth(auth));
  }

  public UsernamePasswordAuthenticationToken get(String jwt) {
    CachedAuth entry = cache.get(jwt);
    if (entry == null || entry.isExpired()) {
      cache.remove(jwt);
      return null;
    }
    return entry.auth;
  }

  public void remove(String jwt) {
    cache.remove(jwt);
  }

  public void cleanup() {
    cache.entrySet().removeIf(e -> e.getValue().isExpired());
  }

  private record CachedAuth(UsernamePasswordAuthenticationToken auth, long createdAt) {
    CachedAuth(UsernamePasswordAuthenticationToken auth) {
      this(auth, System.currentTimeMillis());
    }

    boolean isExpired() {
      return System.currentTimeMillis() - createdAt > TTL_MS;
    }
  }
}
