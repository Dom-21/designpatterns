package com.example.designpatterns.mapper;

import com.example.designpatterns.dto.UserResponse;
import com.example.designpatterns.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between User entities and DTOs
 * Part of DTO Pattern implementation
 */
@Component
public class UserMapper {
    
    /**
     * Converts User entity to UserResponse DTO
     * IMPORTANT: Does not include password in response
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setActive(user.isActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        
        return response;
    }
    
    /**
     * Converts list of User entities to list of UserResponse DTOs
     */
    public List<UserResponse> toResponseList(List<User> users) {
        if (users == null) {
            return List.of();
        }
        
        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}