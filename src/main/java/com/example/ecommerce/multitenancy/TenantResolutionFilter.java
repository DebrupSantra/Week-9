package com.example.ecommerce.multitenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantResolutionFilter extends OncePerRequestFilter {
    private final TenantService tenantService;

    public TenantResolutionFilter(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") || path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/") || path.startsWith("/error");
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String tenantId = request.getHeader("X-Tenant-ID");
        if (tenantId != null && !tenantId.isBlank()) {
            tenantService.validateTenantAccess(tenantId);
            TenantContext.set(tenantId);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
