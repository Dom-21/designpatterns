package com.example.designpatterns.factory;

import com.example.designpatterns.dto.CreateUserRequest;
import com.example.designpatterns.entity.User;
import com.example.designpatterns.strategy.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory Pattern for User creation
 * Centralizes user creation logic with validation and encoding
 */
@Component
public class UserFactory {
    
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserFactory(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Creates a new User from CreateUserRequest
     * Performs validation and password encoding
     */
    public User createUser(CreateUserRequest request) {
        validateUserRequest(request);
        
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        return User.builder()
                .username(normalizeUsername(request.getUsername()))
                .email(normalizeEmail(request.getEmail()))
                .password(encodedPassword)
                .isActive(true)
                .build();
    }
    
    /**
     * Creates a user from individual fields
     */
    public User createUser(String username, String email, String rawPassword) {
        validateFields(username, email, rawPassword);
        
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        return User.builder()
                .username(normalizeUsername(username))
                .email(normalizeEmail(email))
                .password(encodedPassword)
                .isActive(true)
                .build();
    }
    
    /**
     * Validates user request data
     */
    private void validateUserRequest(CreateUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("User request cannot be null");
        }
        validateFields(request.getUsername(), request.getEmail(), request.getPassword());
    }
    
    /**
     * Validates individual fields
     */
    private void validateFields(String username, String email, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email format is invalid");
        }
        
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
    }
    
    /**
     * Normalizes username (lowercase, trimmed)
     */
    private String normalizeUsername(String username) {
        return username.trim().toLowerCase();
    }
    
    /**
     * Normalizes email (lowercase, trimmed)
     */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
    
    /**
     * Basic email validation
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}