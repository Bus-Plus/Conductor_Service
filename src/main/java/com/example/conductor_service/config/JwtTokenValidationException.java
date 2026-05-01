package com.example.conductor_service.config;

public class JwtTokenValidationException extends RuntimeException {
    private final boolean expired;

    public JwtTokenValidationException(String message, boolean expired) {
        super(message);
        this.expired = expired;
    }

    public boolean isExpired() {
        return expired;
    }
}
