package com.example.ecommerce.exception;

public class UnauthorizedTenantException extends RuntimeException {
    public UnauthorizedTenantException(String message) {
        super(message);
    }
}
