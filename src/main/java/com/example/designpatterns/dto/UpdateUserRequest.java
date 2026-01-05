package com.example.designpatterns.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating user information
 * All fields are optional (null means no update)
 */
public class UpdateUserRequest {
    
    @Size(min = 3, max = 16, message = "Username must be between 3 and 16 characters")
    private String username;
    
    @Email(message = "Email must be valid")
    @Size(max = 30, message = "Email must not exceed 30 characters")
    private String email;
    
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    // Default constructor
    public UpdateUserRequest() {
    }

    // Parameterized constructor
    public UpdateUserRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UpdateUserRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
