package com.example.ecommerce.multitenancy;

import com.example.ecommerce.entity.Tenant;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.UnauthorizedTenantException;
import com.example.ecommerce.repository.TenantRepository;
import com.example.ecommerce.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TenantService {
    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Tenant resolveTenant(String tenantIdentifier) {
        return tenantRepository.findByTenantIdentifier(tenantIdentifier)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantIdentifier));
    }

    public void validateTenantAccess(String tenantIdentifier) {
        if (tenantIdentifier == null || tenantIdentifier.isBlank()) {
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return;
        }

        User currentUser = principal.getUser();
        Tenant requested = resolveTenant(tenantIdentifier);
        if (!requested.getId().equals(currentUser.getTenant().getId())) {
            throw new UnauthorizedTenantException("User is not authorized for tenant " + tenantIdentifier);
        }
    }

    public Tenant getOrCreateDefaultTenant(String tenantIdentifier, String tenantName) {
        final String normalizedIdentifier = (tenantIdentifier == null || tenantIdentifier.isBlank()) ? "default" : tenantIdentifier;
        final String normalizedName = (tenantName == null || tenantName.isBlank()) ? normalizedIdentifier : tenantName;

        return tenantRepository.findByTenantIdentifier(normalizedIdentifier)
            .orElseGet(() -> tenantRepository.save(new Tenant(normalizedIdentifier, normalizedName)));
    }
}
