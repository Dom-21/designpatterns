# Refactored User Service with Design Patterns

## Overview
This is a production-ready refactored implementation of the User Service that incorporates multiple design patterns to improve code quality, maintainability, security, and scalability.

## Design Patterns Implemented

### 1. **Builder Pattern** 
**Location:** `User.java`

**Purpose:** Provides a fluent API for constructing complex User objects.

**Benefits:**
- Immutable object construction
- Clear and readable code
- Easy to add new fields without breaking existing code
- Reduces constructor parameter hell

**Example Usage:**
```java
User user = User.builder()
    .username("johndoe")
    .email("john@example.com")
    .password("encodedPassword")
    .isActive(true)
    .build();
```

---

### 2. **DTO (Data Transfer Object) Pattern**
**Location:** `dto/` package

**Purpose:** Separates API layer from domain entities, prevents exposing sensitive data.

**Components:**
- `CreateUserRequest` - Input for creating users
- `UpdateUserRequest` - Input for updating users
- `UserResponse` - Output that NEVER contains password

**Benefits:**
- Security: Passwords never exposed in responses
- API stability: Internal entity changes don't affect API contracts
- Validation: Field-level validation with annotations
- Flexibility: Different views of the same entity

**Example:**
```java
// Request
CreateUserRequest request = new CreateUserRequest("john", "john@example.com", "password123");

// Response (no password!)
UserResponse response = userService.createUser(request);
// response contains: id, username, email, isActive, createdAt, updatedAt
```

---

### 3. **Strategy Pattern**
**Location:** `strategy/` package

**Purpose:** Encapsulates password encoding algorithms, making them interchangeable.

**Components:**
- `PasswordEncoder` interface - Strategy contract
- `BCryptPasswordEncoderImpl` - Concrete BCrypt implementation

**Benefits:**
- Easy to switch encoding algorithms
- Supports multiple algorithms simultaneously
- Testability: Can inject mock encoders
- Open/Closed Principle compliance

**Example:**
```java
@Component
public class BCryptPasswordEncoderImpl implements PasswordEncoder {
    public String encode(String rawPassword) {
        return bCryptEncoder.encode(rawPassword);
    }
    
    public boolean matches(String raw, String encoded) {
        return bCryptEncoder.matches(raw, encoded);
    }
}
```

---

### 4. **Factory Pattern**
**Location:** `factory/UserFactory.java`

**Purpose:** Centralizes user creation logic with validation and encoding.

**Benefits:**
- Single Responsibility: All creation logic in one place
- Consistent validation across the application
- Automatic password encoding
- Username/email normalization (lowercase, trim)
- Easy to modify creation logic globally

**Example:**
```java
@Component
public class UserFactory {
    public User createUser(CreateUserRequest request) {
        validateUserRequest(request);
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        return User.builder()
            .username(normalizeUsername(request.getUsername()))
            .email(normalizeEmail(request.getEmail()))
            .password(encodedPassword)
            .build();
    }
}
```

---

### 5. **Specification Pattern**
**Location:** `repository/UserRepository.java`

**Purpose:** Encapsulates query logic for complex, reusable queries.

**Benefits:**
- Composable queries
- Type-safe query building
- Reusable query components
- Dynamic query construction

**Example:**
```java
public interface UserRepository extends JpaRepository<User, Long>, 
                                       JpaSpecificationExecutor<User> {
    // Standard methods
    Optional<User> findByUsername(String username);
    
    // Custom query methods
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    List<User> searchByUsername(@Param("username") String username);
}
```

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                Controller Layer                         │
│              (UserController.java)                      │
│              - REST endpoints                           │
│              - Request validation                       │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                  Service Layer                          │
│                (UserService.java)                       │
│              - Business logic                           │
│              - Transaction management                   │
│              - Uses: Factory, Strategy, Mapper          │
└───────────────────────┬─────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
┌───────▼──────┐ ┌───── ▼ ───┐  ┌────── ▼ ──────┐
│   Factory    │ │  Strategy │  │   Mapper    │
│ UserFactory  │ │  Password │  │ UserMapper  │
│              │ │  Encoder  │  │             │
└───────┬──────┘ └─────┬─────┘  └──────┬──────┘
        │               │               │
        └───────────────┼───────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                Repository Layer                         │
│               (UserRepository.java)                     │
│              - Database operations                      │
│              - JPA queries                              │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                      Entity Layer                       │
│                  (User.java)                            │
│              - Domain model                             │
│              - Builder pattern                          │
└─────────────────────────────────────────────────────────┘
```

## API Endpoints

### Create User
```http
POST /api/users
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePassword123"
}

Response: 201 Created
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "isActive": true,
  "createdAt": "2024-01-05T10:30:00",
  "updatedAt": "2024-01-05T10:30:00"
}
```

### Get User by ID
```http
GET /api/users/1

Response: 200 OK
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "isActive": true,
  "createdAt": "2024-01-05T10:30:00",
  "updatedAt": "2024-01-05T10:30:00"
}
```

### Update User
```http
PUT /api/users/1
Content-Type: application/json

{
  "email": "newemail@example.com",
  "password": "newPassword123"
}

Response: 200 OK
```

### Deactivate User (Soft Delete)
```http
PATCH /api/users/1/deactivate

Response: 204 No Content
```

### Delete User (Hard Delete)
```http
DELETE /api/users/1

Response: 204 No Content
```

### Search Users
```http
GET /api/users/search?username=john

Response: 200 OK
[
  {
    "id": 1,
    "username": "johndoe",
    ...
  }
]
```

---

## Configuration

### Dependencies (pom.xml)
```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Security for password encoding -->
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-crypto</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### Application Properties
```properties
# Database Configuration
spring.datasource.url=jdbc:h2:mem:userdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Logging
logging.level.com.ecommerce.userservice=DEBUG
```

---

## Testing Examples

### Unit Test Example
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserFactory userFactory;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void createUser_Success() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("john", "john@test.com", "pass123");
        User user = User.builder().username("john").build();
        UserResponse expected = new UserResponse();
        
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userFactory.createUser(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(expected);
        
        // Act
        UserResponse actual = userService.createUser(request);
        
        // Assert
        assertNotNull(actual);
        verify(userRepository).save(user);
    }
}
```

---

## Benefits Summary

| Pattern       | Primary Benefit                             |
|---------------|---------------------------------------------|
| Builder       | Clean object construction, immutability     |
| DTO           | Security, API stability, validation         |
| Strategy      | Flexible algorithm switching, testability   |
| Factory       | Centralized creation, consistent validation |
| Specification | Reusable, composable queries                |

---

## Best Practices Demonstrated

1. **Never expose entities directly** - Always use DTOs
2. **Never store plain-text passwords** - Always encode
3. **Validate at boundaries** - DTOs have validation annotations
4. **Use transactions** - Service methods are transactional
5. **Log appropriately** - Structured logging with SLF4J
6. **Handle exceptions globally** - Consistent error responses
7. **Use dependency injection** - Constructor injection for testability
8. **Follow SOLID principles** - Each class has a single responsibility


## Future Enhancements

- Add caching (Redis) for frequently accessed users
- Implement pagination for list endpoints
- Add user roles and permissions
- Implement JWT authentication
- Add email verification flow
- Implement password reset functionality
- Add user profile pictures
- Implement audit logging

---

## Conclusion

This refactored implementation demonstrates enterprise-level code quality with proper use of design patterns, security best practices, and maintainable architecture. The code is production-ready and follows industry standards for Spring Boot applications.
