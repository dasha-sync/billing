package api.util;

import io.jsonwebtoken.*;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
  @Value("${billing.api.secret}")
  private String secret;

  @Value("${billing.api.expiration}")
  private long expiration;

  public String generateToken(Authentication auth) throws NoSuchAlgorithmException {
    UserDetailsImplementation userDetails = (UserDetailsImplementation) auth.getPrincipal();

    return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(SignatureAlgorithm.HS256, secret)
        .compact();
  }

  public String getNameFromJwt(String token) {
    return Jwts.parser()
        .setSigningKey(secret)
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }
}
