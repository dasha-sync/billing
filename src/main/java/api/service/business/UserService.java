package api.service.business;

import api.dto.user.*;
import api.exception.GlobalException;
import api.model.User;
import api.repository.*;
import api.util.SessionProvider;
import jakarta.servlet.http.*;
import jakarta.transaction.Transactional;
import java.security.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final SessionProvider sessionProvider;

  @Transactional
  public void deleteUser(
      DeleteUserRequest request,
      Principal principal,
      HttpServletResponse response) {
    User currentUser = getCurrentUser(principal);

    if (!passwordEncoder.matches(request.getPassword(), currentUser.getPassword())) {
      throw new GlobalException("Wrong password", "CONFLICT");
    }

    Long userId = currentUser.getId();
    userRepository.delete(currentUser);

    if (userRepository.existsById(userId)) {
      throw new GlobalException("Failed to delete user", "CONFLICT");
    }

    sessionProvider.clearAuthCookies(response);
  }

  public User getCurrentUser(Principal principal) {
    return userRepository.findUserByUsername(principal.getName())
        .orElseThrow(() -> new GlobalException("Current user not found", "NOT_FOUND"));
  }

  public UserResponse mapToUserDto(User user) {
    return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
  }
}
