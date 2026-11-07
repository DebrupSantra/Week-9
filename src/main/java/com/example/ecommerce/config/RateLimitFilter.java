package com.example.ecommerce.config;

import com.example.ecommerce.security.UserPrincipal;
import com.example.ecommerce.util.RequestContextUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final int limitPerMinute;
    private final boolean trustedProxy;
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public RateLimitFilter(
        @Value("${app.security.rate-limit-per-minute:60}") int limitPerMinute,
        @Value("${app.security.trusted-proxy:false}") boolean trustedProxy
    ) {
        this.limitPerMinute = limitPerMinute;
        this.trustedProxy = trustedProxy;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String key = resolveKey(request);
        Bucket bucket = cache.computeIfAbsent(key, k -> createBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":429,\"error\":\"TOO_MANY_REQUESTS\",\"message\":\"Rate limit exceeded\"}");
        }
    }

    private Bucket createBucket() {
        Refill refill = Refill.greedy(limitPerMinute, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(limitPerMinute, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal principal) {
            return "user:" + principal.getUser().getId();
        }
        return "ip:" + RequestContextUtil.resolveClientIp(request, trustedProxy);
    }
}
