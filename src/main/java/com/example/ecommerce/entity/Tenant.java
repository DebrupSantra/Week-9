package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tenants")
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tenantIdentifier;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status = TenantStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Tenant() {}

    public Tenant(String tenantIdentifier, String name) {
        this.tenantIdentifier = tenantIdentifier;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantIdentifier() { return tenantIdentifier; }
    public void setTenantIdentifier(String tenantIdentifier) { this.tenantIdentifier = tenantIdentifier; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public TenantStatus getStatus() { return status; }
    public void setStatus(TenantStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
