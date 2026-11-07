package com.example.ecommerce.repository;

import com.example.ecommerce.entity.RefreshToken;
import com.example.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    Iterable<RefreshToken> findAllByUser(User user);
    void deleteByUser(User user);
}
