package api.service.business;

import api.dto.auth.*;
import api.dto.user.*;
import api.exception.GlobalException;
import api.model.User;
import api.repository.UserRepository;
import api.util.JwtTokenProvider;
import api.util.SessionProvider;
import jakarta.servlet.http.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.*;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserService userService;
  private final SessionProvider sessionProvider;

  public UserResponse signup(SignupRequest request) {
    checkIfUserExists(request.getUsername(), request.getEmail());

    User user = createUser(request);
    userRepository.save(user);
    return userService.mapToUserDto(user);
  }

  public UserResponse signin(SigninRequest request, HttpServletResponse response) {
    try {
      User user = getUserByEmail(request.getEmail());
      Authentication auth = authenticateUser(user, request.getPassword());
      SecurityContextHolder.getContext().setAuthentication(auth);

      String jwt = jwtTokenProvider.generateToken(auth);
      sessionProvider.setAuthCookies(response, jwt, user);

      return userService.mapToUserDto(user);
    } catch (BadCredentialsException | NoSuchAlgorithmException e) {
      throw new BadCredentialsException("Invalid credentials");
    }
  }

  public void signout(HttpServletRequest request, HttpServletResponse response) {
    sessionProvider.deleteSessionByRequest(request);
    sessionProvider.clearAuthCookies(response);
  }

  public CheckResponse checkAuth(HttpServletRequest request) {
    boolean authenticated = sessionProvider.findBySessionCookie(request).isPresent();

    return new CheckResponse(authenticated);
  }

  private void checkIfUserExists(String username, String email) {
    if (userRepository.existsUserByUsername(username)) {
      throw new GlobalException("Username already taken", "CONFLICT");
    }

    if (userRepository.existsUserByEmail(email)) {
      throw new GlobalException("Email already registered", "CONFLICT");
    }
  }

  private User createUser(SignupRequest request) {
    User user = new User();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    return user;
  }

  private Authentication authenticateUser(User user, String password) {
    return authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(user.getUsername(), password));
  }

  private User getUserByEmail(String email) {
    return userRepository.findUserByEmail(email)
        .orElseThrow(() -> new GlobalException("User not found", "NOT_FOUND"));
  }

  private Map<String, String> extractCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Collections.emptyMap();
    }

    return Arrays.stream(cookies)
        .collect(Collectors.toMap(Cookie::getName, Cookie::getValue, (a, b) -> b));
  }
}
