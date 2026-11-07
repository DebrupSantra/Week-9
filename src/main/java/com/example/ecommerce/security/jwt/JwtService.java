package com.example.ecommerce.security.jwt;

import com.example.ecommerce.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final String issuer;
    private final long expirationMs;
    private final SecretKey secretKey;

    public JwtService(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.issuer:secure-ecommerce-platform}") String issuer,
        @Value("${app.jwt.expiration-ms:900000}") long expirationMs
    ) {
        this.issuer = issuer;
        this.expirationMs = expirationMs;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
            .issuer(issuer)
            .subject(String.valueOf(user.getId()))
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .claim("username", user.getUsername())
            .claim("tenantId", user.getTenant() == null ? null : user.getTenant().getTenantIdentifier())
            .claim("roles", user.getRoles().stream().map(Enum::name).toList())
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).get("username", String.class);
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public boolean isValidToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
