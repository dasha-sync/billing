package api.controller;

import api.dto.common.ApiResponse;
import api.dto.auth.*;
import api.dto.user.*;
import api.service.AuthService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class SecurityController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(
            @Valid @RequestBody SignupRequest request) {
        UserResponse user = authService.signup(request);
        return ResponseEntity.ok(new ApiResponse<>("Signup successful", user));
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<UserResponse>> signin(
            @Valid @RequestBody SigninRequest request,
            HttpServletResponse response) {
        UserResponse userResponse = authService.signin(request, response);
        return ResponseEntity.ok(new ApiResponse<>("Signin successful", userResponse));
    }

    @PostMapping("/signout")
    public ResponseEntity<ApiResponse<Void>> signout(HttpServletRequest request, HttpServletResponse response) {
        authService.signout(request, response);
        return ResponseEntity.ok(new ApiResponse<>("Successfully loged out", null));
    }

    @GetMapping("/check")
    public ResponseEntity<CheckResponse> checkAuth(HttpServletRequest request) {
        return ResponseEntity.ok(authService.checkAuth(request));
    }
}
