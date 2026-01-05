package com.example.designpatterns.service;

import com.example.designpatterns.dto.CreateUserRequest;
import com.example.designpatterns.dto.UpdateUserRequest;
import com.example.designpatterns.dto.UserResponse;
import com.example.designpatterns.exception.UserAlreadyExistsException;
import com.example.designpatterns.exception.UserNotFoundException;
import com.example.designpatterns.factory.UserFactory;
import com.example.designpatterns.mapper.UserMapper;
import com.example.designpatterns.entity.User;
import com.example.designpatterns.repository.UserRepository;
import com.example.designpatterns.strategy.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Refactored UserService with Design Patterns:
 * - Factory Pattern (UserFactory for creation)
 * - Strategy Pattern (PasswordEncoder for password handling)
 * - DTO Pattern (Request/Response separation)
 * - Builder Pattern (User entity construction)
 */
@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final UserFactory userFactory;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserService(UserRepository userRepository,
                       UserFactory userFactory,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userFactory = userFactory;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Creates a new user
     * @param request CreateUserRequest DTO
     * @return UserResponse DTO (without password)
     * @throws UserAlreadyExistsException if username or email already exists
     */
    public UserResponse createUser(CreateUserRequest request) {
        logger.info("Creating user with username: {}", request.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername().toLowerCase())) {
            throw new UserAlreadyExistsException("username", request.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new UserAlreadyExistsException("email", request.getEmail());
        }
        
        // Use Factory to create user with encoded password
        User user = userFactory.createUser(request);
        
        // Save to database
        User savedUser = userRepository.save(user);
        
        logger.info("User created successfully with id: {}", savedUser.getId());
        
        // Convert to response DTO (excludes password)
        return userMapper.toResponse(savedUser);
    }
    
    /**
     * Gets user by ID
     * @param id User ID
     * @return UserResponse DTO
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        logger.debug("Fetching user with id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        return userMapper.toResponse(user);
    }
    
    /**
     * Gets user by username
     * @param username Username
     * @return UserResponse DTO
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        logger.debug("Fetching user with username: {}", username);
        
        User user = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        
        return userMapper.toResponse(user);
    }
    
    /**
     * Gets all users
     * @return List of UserResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        logger.debug("Fetching all users");
        
        List<User> users = userRepository.findAll();
        return userMapper.toResponseList(users);
    }
    
    /**
     * Gets all active users
     * @return List of active UserResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllActiveUsers() {
        logger.debug("Fetching all active users");
        
        List<User> users = userRepository.findByIsActiveTrue();
        return userMapper.toResponseList(users);
    }
    
    /**
     * Checks if user exists by username
     * @param username Username to check
     * @return true if exists
     */
    @Transactional(readOnly = true)
    public boolean doesUserExistByUsername(String username) {
        return userRepository.existsByUsername(username.toLowerCase());
    }
    
    /**
     * Checks if user exists by email
     * @param email Email to check
     * @return true if exists
     */
    @Transactional(readOnly = true)
    public boolean doesUserExistByEmail(String email) {
        return userRepository.existsByEmail(email.toLowerCase());
    }
    
    /**
     * Updates user information
     * @param id User ID
     * @param request UpdateUserRequest DTO
     * @return Updated UserResponse DTO
     * @throws UserNotFoundException if user not found
     * @throws UserAlreadyExistsException if new username/email already exists
     */
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        logger.info("Updating user with id: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        // Update username if provided
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            String normalizedUsername = request.getUsername().toLowerCase();
            
            // Check if new username already exists (but not for current user)
            if (!existingUser.getUsername().equals(normalizedUsername) && 
                userRepository.existsByUsername(normalizedUsername)) {
                throw new UserAlreadyExistsException("username", request.getUsername());
            }
            
            existingUser.setUsername(normalizedUsername);
        }
        
        // Update email if provided
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            String normalizedEmail = request.getEmail().toLowerCase();
            
            // Check if new email already exists (but not for current user)
            if (!existingUser.getEmail().equals(normalizedEmail) && 
                userRepository.existsByEmail(normalizedEmail)) {
                throw new UserAlreadyExistsException("email", request.getEmail());
            }
            
            existingUser.setEmail(normalizedEmail);
        }
        
        // Update password if provided (encode it using Strategy)
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            existingUser.setPassword(encodedPassword);
        }
        
        User updatedUser = userRepository.save(existingUser);
        
        logger.info("User updated successfully with id: {}", updatedUser.getId());
        
        return userMapper.toResponse(updatedUser);
    }
    
    /**
     * Soft deletes a user (sets isActive to false)
     * @param id User ID
     * @throws UserNotFoundException if user not found
     */
    public void deactivateUser(Long id) {
        logger.info("Deactivating user with id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        user.setActive(false);
        userRepository.save(user);
        
        logger.info("User deactivated successfully with id: {}", id);
    }
    
    /**
     * Hard deletes a user from database
     * @param id User ID
     * @throws UserNotFoundException if user not found
     */
    public void deleteUser(Long id) {
        logger.info("Deleting user with id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        userRepository.delete(user);
        
        logger.info("User deleted successfully with id: {}", id);
    }
    
    /**
     * Searches users by username (partial match)
     * @param username Username to search
     * @return List of matching UserResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsersByUsername(String username) {
        logger.debug("Searching users with username containing: {}", username);
        
        List<User> users = userRepository.searchByUsername(username);
        return userMapper.toResponseList(users);
    }
}