package com.example.ecommerce.exception;

import com.example.ecommerce.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, "NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedTenantException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedTenant(UnauthorizedTenantException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(403, "TENANT_FORBIDDEN", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(400, "BAD_REQUEST", ex.getMessage()));
    }
}
