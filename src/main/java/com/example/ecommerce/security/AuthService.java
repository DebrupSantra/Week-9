package com.example.ecommerce.security;

import com.example.ecommerce.dto.AuthResponse;
import com.example.ecommerce.entity.RefreshToken;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.Tenant;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.multitenancy.TenantService;
import com.example.ecommerce.repository.RefreshTokenRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.audit.AuditService;
import com.example.ecommerce.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TenantService tenantService;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final long refreshExpirationMs;

    public AuthService(
        UserRepository userRepository,
        RefreshTokenRepository refreshTokenRepository,
        TenantService tenantService,
        JwtService jwtService,
        AuditService auditService,
        @Value("${app.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tenantService = tenantService;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public AuthResponse register(String username, String email, String password, String tenantIdentifier, String tenantName, String requestedRole) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        Tenant tenant = tenantService.getOrCreateDefaultTenant(tenantIdentifier, tenantName);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setTenant(tenant);
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setRoles(Set.of(Role.ROLE_CUSTOMER));
        if (requestedRole != null) {
            switch (requestedRole) {
                case "ROLE_ADMIN" -> user.getRoles().add(Role.ROLE_ADMIN);
                case "ROLE_VENDOR" -> user.getRoles().add(Role.ROLE_VENDOR);
                case "ROLE_CUSTOMER" -> user.getRoles().add(Role.ROLE_CUSTOMER);
                default -> user.getRoles().add(Role.ROLE_CUSTOMER);
            }
        }
        User saved = userRepository.save(user);
        return issueTokens(saved, null);
    }

    @Transactional
    public AuthResponse login(String usernameOrEmail, String password, HttpServletRequest request) {
        User user = userRepository.findByUsername(usernameOrEmail)
            .or(() -> userRepository.findByEmail(usernameOrEmail))
            .orElseThrow(() -> new ResourceNotFoundException("Invalid username or password"));

        if (user.isLocked() || user.getFailedLoginAttempts() >= 5) {
            auditService.log("ACCOUNT_LOCKOUT", request.getRequestURI(), false, "username=" + user.getUsername(), request);
            throw new IllegalArgumentException("Account is locked due to repeated failed login attempts");
        }

        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setLocked(true);
                auditService.log("ACCOUNT_LOCKOUT", request.getRequestURI(), false, "username=" + user.getUsername(), request);
            }
            userRepository.save(user);
            auditService.log("FAILED_LOGIN", request.getRequestURI(), false, "username=" + user.getUsername(), request);
            throw new IllegalArgumentException("Invalid username or password");
        }

        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        auditService.log("SUCCESSFUL_LOGIN", request.getRequestURI(), true, "username=" + user.getUsername(), request);
        return issueTokens(user, request);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken, HttpServletRequest request) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }
        String normalized = refreshToken.trim();
        String hashed = hashToken(normalized);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hashed)
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        auditService.log("TOKEN_REFRESH", request.getRequestURI(), true, "family=" + stored.getFamilyId(), request);
        return issueTokens(stored.getUser(), request);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(hashToken(refreshToken.trim()))
            .ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
    }

    public AuthResponse issueTokensForUser(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = createRefreshToken(user);
        List<String> roles = user.getRoles().stream().map(Enum::name).toList();
        return new AuthResponse(accessToken, newRefreshToken, "Bearer", jwtService.getExpirationMs(), user.getId(), user.getUsername(), user.getTenant().getTenantIdentifier(), roles);
    }

    private AuthResponse issueTokens(User user, HttpServletRequest request) {
        return issueTokensForUser(user);
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to hash token", e);
        }
    }

    private String createRefreshToken(User user) {
        String raw = UUID.randomUUID().toString();
        String family = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setFamilyId(family);
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        refreshToken.setTokenHash(hashToken(raw));
        refreshTokenRepository.save(refreshToken);
        return raw;
    }
}
