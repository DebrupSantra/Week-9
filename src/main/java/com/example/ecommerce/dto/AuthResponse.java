package com.example.ecommerce.dto;

import java.util.List;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    Long userId,
    String username,
    String tenantIdentifier,
    List<String> roles
) {}
