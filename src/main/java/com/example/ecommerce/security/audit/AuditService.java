package com.example.ecommerce.security.audit;

import com.example.ecommerce.repository.SecurityAuditLogRepository;
import com.example.ecommerce.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final SecurityAuditLogRepository securityAuditLogRepository;
    private final boolean trustedProxy;

    public AuditService(SecurityAuditLogRepository securityAuditLogRepository,
                       @Value("${app.security.trusted-proxy:false}") boolean trustedProxy) {
        this.securityAuditLogRepository = securityAuditLogRepository;
        this.trustedProxy = trustedProxy;
    }

    public void log(String eventType, String requestPath, boolean success, String metadata, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SecurityAuditLog auditLog = new SecurityAuditLog();
        auditLog.setEventType(eventType);
        auditLog.setRequestPath(requestPath);
        auditLog.setSuccess(success);
        auditLog.setMetadata(metadata);
        auditLog.setIpAddress(RequestContextUtil.resolveClientIp(request, trustedProxy));

        if (auth != null && auth.getPrincipal() instanceof com.example.ecommerce.security.UserPrincipal principal) {
            auditLog.setUserId(principal.getUser().getId());
            if (principal.getUser().getTenant() != null) {
                auditLog.setTenantId(principal.getUser().getTenant().getId());
            }
        }
        securityAuditLogRepository.save(auditLog);
    }
}
