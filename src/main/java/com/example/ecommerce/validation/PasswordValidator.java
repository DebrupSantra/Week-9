package com.example.ecommerce.validation;

public class PasswordValidator {
    public static boolean isValid(String password) {
        return password != null && password.length() >= 8;
    }
}
