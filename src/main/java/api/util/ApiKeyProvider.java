package api.util;

import api.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyProvider {

  @Value("${billing.api.secret}")
  private String secret;

  public String generate(User user) {
    return Jwts.builder()
        .setSubject(user.getUsername())
        .claim("userId", user.getId())
        .claim("type", "api_key")
        .setIssuedAt(new Date())
        .signWith(SignatureAlgorithm.HS256, getSecretBytes())
        .compact();
  }

  public String getUsernameFromApiKey(String apiKey) {
    return Jwts.parser()
        .setSigningKey(getSecretBytes())
        .parseClaimsJws(apiKey)
        .getBody()
        .getSubject();
  }

  public Long getUserIdFromApiKey(String apiKey) {
    return Jwts.parser()
        .setSigningKey(getSecretBytes())
        .parseClaimsJws(apiKey)
        .getBody()
        .get("userId", Long.class);
  }

  public boolean isValidApiKey(String apiKey) {
    try {
      Jwts.parser()
          .setSigningKey(getSecretBytes())
          .parseClaimsJws(apiKey);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  // приватный метод для конвертации секрета в массив байт
  private byte[] getSecretBytes() {
    // HS256 требует минимум 32 байта (256 бит)
    if (secret.length() < 32) {
      throw new RuntimeException("Секрет слишком короткий для HS256! Минимум 32 символа.");
    }
    return secret.getBytes();
  }
}
