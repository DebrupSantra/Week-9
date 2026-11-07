package com.example.ecommerce.security.oauth2;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.multitenancy.TenantService;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final AuthService authService;
    private final TenantService tenantService;

    public OAuth2LoginSuccessHandler(UserRepository userRepository, AuthService authService, TenantService tenantService) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.tenantService = tenantService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String username = oauthUser.getAttribute("login");
        if (email == null || email.isBlank()) {
            email = username + "@example.com";
        }
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> userRepository.save(new User()));
        if (user.getId() == null) {
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash("oauth2");
            user.setEnabled(true);
            user.setEmailVerified(true);
            user.setTenant(tenantService.getOrCreateDefaultTenant("default", "Default Tenant"));
            userRepository.save(user);
        }
        var authResponse = authService.issueTokensForUser(user);
        response.setHeader("Authorization", "Bearer " + authResponse.accessToken());
        response.setHeader("X-Refresh-Token", authResponse.refreshToken());
        response.sendRedirect("/api/products");
    }
}
