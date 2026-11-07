package com.example.ecommerce.security.oauth2;

import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.Tenant;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.multitenancy.TenantService;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final TenantService tenantService;

    public CustomOAuth2UserService(UserRepository userRepository, TenantService tenantService) {
        this.userRepository = userRepository;
        this.tenantService = tenantService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User delegate = super.loadUser(userRequest);
        Map<String, Object> attributes = delegate.getAttributes();
        String email = (String) attributes.getOrDefault("email", "oauth-user@example.com");
        String username = (String) attributes.getOrDefault("login", attributes.getOrDefault("name", "/oauth-user"));

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            Tenant tenant = tenantService.getOrCreateDefaultTenant("default", "Default Tenant");
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPasswordHash("oauth2");
            newUser.setEnabled(true);
            newUser.setEmailVerified(true);
            newUser.setTenant(tenant);
            newUser.setRoles(Set.of(Role.ROLE_CUSTOMER));
            return userRepository.save(newUser);
        });

        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_CUSTOMER")),
            attributes,
            "login"
        );
    }
}
