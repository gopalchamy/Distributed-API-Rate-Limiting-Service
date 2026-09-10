package com.ratelimiter.controller;

import com.ratelimiter.dto.AuthRequests;
import com.ratelimiter.security.UserPrincipal;
import com.ratelimiter.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Developer registration and JWT authentication APIs")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new developer account")
    public ResponseEntity<AuthRequests.AuthResponse> register(@Valid @RequestBody AuthRequests.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT authentication token")
    public ResponseEntity<AuthRequests.AuthResponse> login(@Valid @RequestBody AuthRequests.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated developer profile")
    public ResponseEntity<AuthRequests.UserProfileResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authService.getCurrentUser(principal));
    }
}
