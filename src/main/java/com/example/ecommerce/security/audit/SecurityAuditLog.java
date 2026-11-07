package com.example.ecommerce.security.audit;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "security_audit_logs")
public class SecurityAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Long userId;

    @Column(nullable = true)
    private Long tenantId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = true)
    private String ipAddress;

    @Column(nullable = false)
    private String requestPath;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    @Column(columnDefinition = "TEXT")
    private String metadata;

    public SecurityAuditLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getRequestPath() { return requestPath; }
    public void setRequestPath(String requestPath) { this.requestPath = requestPath; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
