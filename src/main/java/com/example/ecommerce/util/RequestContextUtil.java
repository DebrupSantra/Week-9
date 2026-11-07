package com.example.ecommerce.util;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestContextUtil {
    private RequestContextUtil() {}

    public static String resolveClientIp(HttpServletRequest request, boolean trustedProxy) {
        if (!trustedProxy) {
            return request.getRemoteAddr();
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
