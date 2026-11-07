package com.example.ecommerce.controller;

import com.example.ecommerce.dto.AuthResponse;
import com.example.ecommerce.dto.LoginRequest;
import com.example.ecommerce.dto.RefreshTokenRequest;
import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.AuthService;
import com.example.ecommerce.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request.username(), request.email(), request.password(), request.tenantIdentifier(), request.tenantName(), "ROLE_CUSTOMER");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request.usernameOrEmail(), request.password(), httpRequest));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.refreshToken(request.refreshToken(), httpRequest));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ResourceNotFoundException("No authenticated user");
        }
        return ResponseEntity.ok(userRepository.findById(principal.getUser().getId()).orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }
}
