package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.Tenant;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.TenantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductRepository productRepository;
    private final TenantRepository tenantRepository;

    public ProductController(ProductRepository productRepository, TenantRepository tenantRepository) {
        this.productRepository = productRepository;
        this.tenantRepository = tenantRepository;
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'CUSTOMER')")
    public ResponseEntity<List<Product>> listProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @PostMapping("/vendor/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<Product> createProduct(@RequestBody Product product, @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {
        if (tenantId != null && !tenantId.isBlank()) {
            Tenant tenant = tenantRepository.findByTenantIdentifier(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
            product.setTenant(tenant);
        }
        return ResponseEntity.ok(productRepository.save(product));
    }
}
