package com.example.designpatterns.strategy;

/**
 * Strategy Pattern for Password Encoding
 * Allows different encoding strategies to be used interchangeably
 */
public interface PasswordEncoder {
    
    /**
     * Encodes a raw password
     * @param rawPassword the plain text password
     * @return encoded password
     */
    String encode(String rawPassword);
    
    /**
     * Checks if raw password matches the encoded password
     * @param rawPassword the plain text password
     * @param encodedPassword the encoded password
     * @return true if passwords match
     */
    boolean matches(String rawPassword, String encodedPassword);
}