package api.controller;

import api.dto.common.ApiResponse;
import api.dto.user.*;
import api.service.business.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @DeleteMapping("/destroy")
  public ResponseEntity<ApiResponse<Void>> deleteUser(
      @Valid @RequestBody DeleteUserRequest request,
      Principal principal,
      HttpServletResponse response) {
    userService.deleteUser(request, principal, response);
    return ResponseEntity.ok(new ApiResponse<>("User deleted successfully", null));
  }
}
