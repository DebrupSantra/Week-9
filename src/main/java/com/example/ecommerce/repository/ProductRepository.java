package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByTenant(Tenant tenant);
}
