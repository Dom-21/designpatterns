package com.example.designpatterns.strategy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt implementation of PasswordEncoder strategy
 * Uses BCrypt hashing algorithm for secure password storage
 */
@Component
public class BCryptPasswordEncoderImpl implements PasswordEncoder {
    
    private final BCryptPasswordEncoder bCryptEncoder;
    
    public BCryptPasswordEncoderImpl() {
        this.bCryptEncoder = new BCryptPasswordEncoder(12); // Strength 12
    }
    
    @Override
    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return bCryptEncoder.encode(rawPassword);
    }
    
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return bCryptEncoder.matches(rawPassword, encodedPassword);
    }
}
