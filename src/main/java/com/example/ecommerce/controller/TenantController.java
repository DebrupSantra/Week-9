package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Tenant;
import com.example.ecommerce.repository.TenantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TenantController {
    private final TenantRepository tenantRepository;

    public TenantController(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @GetMapping("/tenants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Tenant>> listTenants() {
        return ResponseEntity.ok(tenantRepository.findAll());
    }

    @PostMapping("/admin/tenants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Tenant> createTenant(@RequestBody Tenant tenant) {
        return ResponseEntity.ok(tenantRepository.save(tenant));
    }
}
