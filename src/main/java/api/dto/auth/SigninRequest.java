package api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SigninRequest {
  @Email(message = "Non correct email format")
  private String email;

  @Size(min = 6, max = 20, message = "Password must contain 6 - 20 symbols")
  private String password;
}
