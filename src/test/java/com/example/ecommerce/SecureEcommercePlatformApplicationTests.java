package com.example.ecommerce;

import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.Tenant;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.multitenancy.TenantService;
import com.example.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SecureEcommercePlatformApplicationTests {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
        assertThat(userRepository).isNotNull();
    }

    @Test
    void registerCreatesUserAndTenant() {
        Tenant tenant = tenantService.getOrCreateDefaultTenant("shop-one", "Shop One");
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPasswordHash(passwordEncoder.encode("StrongPass123"));
        user.setTenant(tenant);
        user.getRoles().add(Role.ROLE_CUSTOMER);
        userRepository.save(user);

        assertThat(userRepository.findByUsername("alice")).isPresent();
        assertThat(userRepository.findByEmail("alice@example.com")).isPresent();
    }
}
