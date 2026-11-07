package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByTenantIdentifier(String tenantIdentifier);
}
