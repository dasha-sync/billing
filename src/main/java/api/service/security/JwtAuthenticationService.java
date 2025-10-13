package api.service.security;

import api.util.JwtTokenProvider;
import api.util.TokenCache;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtAuthenticationService {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;
  private final TokenCache tokenCache = new TokenCache();

  public JwtAuthenticationService(JwtTokenProvider jwtTokenProvider,
                                  UserDetailsService userDetailsService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userDetailsService = userDetailsService;
  }

  public void authenticateIfNecessary(String jwt) {
    if (SecurityContextHolder.getContext().getAuthentication() != null) return;

    tokenCache.cleanup();

    var cachedAuth = tokenCache.get(jwt);
    if (cachedAuth != null) {
      SecurityContextHolder.getContext().setAuthentication(cachedAuth);
      return;
    }

    try {
      String username = jwtTokenProvider.getNameFromJwt(jwt);
      if (username == null) return;

      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(auth);
      tokenCache.put(jwt, auth);

    } catch (ExpiredJwtException e) {
      log.warn("JWT expired: {}", e.getMessage());
      tokenCache.remove(jwt);
    } catch (Exception e) {
      log.error("JWT authentication error", e);
      tokenCache.remove(jwt);
    }
  }

  public String getAuthenticatedUsername() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof UserDetails ud) {
      return ud.getUsername();
    }
    return null;
  }
}
