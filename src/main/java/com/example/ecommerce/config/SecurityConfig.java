package com.example.ecommerce.config;

import com.example.ecommerce.multitenancy.TenantResolutionFilter;
import com.example.ecommerce.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantResolutionFilter tenantResolutionFilter;
    private final RateLimitFilter rateLimitFilter;
    private final OAuth2UserService<org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest, OAuth2User> customOAuth2UserService;

    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthenticationFilter,
        TenantResolutionFilter tenantResolutionFilter,
        RateLimitFilter rateLimitFilter,
        @org.springframework.beans.factory.annotation.Qualifier("customOAuth2UserService") OAuth2UserService<org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest, OAuth2User> customOAuth2UserService
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.tenantResolutionFilter = tenantResolutionFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/error", "/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/vendor/**").hasAnyRole("ADMIN", "VENDOR")
                .requestMatchers("/api/customer/**").hasAnyRole("ADMIN", "CUSTOMER")
                .anyRequest().authenticated())
            .oauth2Login(oauth -> oauth
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(tenantResolutionFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
