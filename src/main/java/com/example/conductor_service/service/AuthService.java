package com.example.conductor_service.service;

import org.springframework.stereotype.Service;

import com.example.conductor_service.config.JwtTokenProvider;

@Service
public class AuthService {

    private final JwtTokenProvider tokenProvider;

    public AuthService(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public boolean validateConductorRole(String token) {
        tokenProvider.validateToken(token);
        String role = tokenProvider.getRole(token);
        return role != null && "CONDUCTOR".equalsIgnoreCase(role);
    }
}
