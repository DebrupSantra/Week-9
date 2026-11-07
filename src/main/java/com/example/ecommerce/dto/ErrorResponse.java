package com.example.ecommerce.dto;

public record ErrorResponse(int status, String error, String message) {}
