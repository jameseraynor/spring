# Spring Reactive Demo - Interview Study Guide

## Project Overview

This project demonstrates a Spring Boot application using reactive programming with Spring WebFlux and R2DBC. It implements a user management system with both traditional annotation-based REST controllers and functional endpoints.

## Key Technologies

- **Spring Boot 3.2.0**: Application framework
- **Spring WebFlux**: Reactive web framework
- **Spring Security**: Security framework with reactive support
- **Spring Data R2DBC**: Reactive database access
- **H2 Database**: In-memory database
- **JWT (JSON Web Tokens)**: Stateless authentication
- **BCrypt**: Password hashing
- **Project Reactor**: Reactive programming library (Mono and Flux)
- **Java 17**: Programming language

## Core Reactive Programming Concepts

### Reactive Streams

The project uses the Reactive Streams API through Project Reactor, which provides two main types:

1. **Mono<T>**: Represents 0 or 1 element
2. **Flux<T>**: Represents 0 to N elements

### Key Reactive Operators Demonstrated

- **map/flatMap**: Transform data
- **filter**: Filter elements
- **switchIfEmpty**: Handle empty results
- **onErrorResume**: Handle errors with fallbacks
- **doOnSuccess/doOnError**: Side effects
- **delayElements**: Introduce artificial delays
- **take**: Limit result size
- **distinct**: Remove duplicates
- **sort**: Order elements
- **then**: Complete when upstream completes
- **timeout**: Handle timeouts

## Project Structure

### Model Layer

The `User` class represents the core data entity with validation annotations:
- `@Table("users")`: Maps to the "users" database table
- `@Id`: Marks the primary key
- `@NotBlank`, `@Size`, `@Email`: Bean validation constraints

### Repository Layer

`UserRepository` extends `R2dbcRepository` for reactive database operations:
- Custom finder methods: `findByDepartment`, `findByEmail`
- Custom queries with `@Query` annotation
- Returns `Mono<T>` for single results and `Flux<T>` for collections

### Service Layer

`UserService` contains business logic:
- Constructor injection for dependencies
- CRUD operations returning reactive types
- Error handling with `switchIfEmpty` and `onErrorResume`
- Advanced operations like filtering, mapping, and limiting results

`NotificationService` demonstrates asynchronous operations:
- Simulates async notification sending
- Uses `delayElement` to mimic network delays
- Shows proper error handling

### Controller Layer

Two approaches to handling HTTP requests:

1. **Annotation-based (`UserController`)**:
   - Traditional `@RestController` with reactive return types
   - Maps HTTP methods to service calls
   - Proper error handling with `onErrorResume` and `defaultIfEmpty`
   - Returns appropriate HTTP status codes

2. **Functional endpoints (`UserHandler` + router function)**:
   - Functional approach using `RouterFunction`
   - Handler methods process requests and produce responses
   - Defined in `SpringReactiveDemoApplication` using route builder

## Security Module

The project includes a comprehensive security implementation with JWT-based authentication and role-based authorization:

### Security Models

1. **Role**: Represents user roles (ADMIN, USER)
2. **UserRole**: Junction table linking users to roles
3. **User**: Extended with password and enabled fields

### Authentication & Authorization

- **JWT (JSON Web Tokens)**: Stateless authentication
- **BCrypt**: Password hashing for secure storage
- **Role-based access control**: Different permissions for USER and ADMIN roles
- **Reactive Security**: Non-blocking security filters

### Security Configuration

- **SecurityConfig**: Configures security rules and JWT authentication
- **JwtUtil**: Handles JWT token generation and validation
- **JwtAuthenticationManager**: Manages JWT-based authentication
- **AuthService**: Handles login, registration, and user role management

### Protected Endpoints

- **Public**: `/api/auth/login`, `/api/auth/register`, `/api/public/**`
- **User Role**: `/api/users/**` (GET, PUT), `/api/functional/**`
- **Admin Role**: `/api/admin/**`, `/api/users/**` (DELETE, POST)

## Database Configuration

- Uses R2DBC with H2 in-memory database
- Schema defined in `schema.sql` (includes security tables)
- Initial data loaded from `data.sql` (includes roles and user-role mappings)
- Configuration in `application.yml` (includes JWT settings)

## Testing

The project includes comprehensive tests:
- `WebTestClient` for testing HTTP endpoints
- Mocking with Mockito
- Testing both success and error scenarios
- Verifying response status codes and body content

## Key Interview Topics

### 1. Reactive Programming Fundamentals

- **What is reactive programming?**
  - Programming paradigm focused on asynchronous data streams and propagation of change
  - Non-blocking, event-driven approach
  - Handles backpressure (when consumer processes data slower than producer generates it)

- **Benefits of reactive programming:**
  - Better resource utilization
  - Improved scalability with fewer threads
  - Non-blocking I/O operations
  - Built-in error handling
  - Backpressure management

- **Project Reactor basics:**
  - `Mono<T>`: Publisher of 0 or 1 element
  - `Flux<T>`: Publisher of 0 to N elements
  - Cold vs. Hot publishers
  - Subscription model

### 2. Spring WebFlux

- **Differences between Spring MVC and Spring WebFlux:**
  - Blocking vs. non-blocking
  - Servlet API vs. Reactive HTTP
  - Thread-per-request vs. event loop model

- **Annotation-based vs. Functional endpoints:**
  - When to use each approach
  - Benefits and trade-offs

- **WebClient vs. RestTemplate:**
  - Non-blocking vs. blocking HTTP client
  - Reactive support

### 3. R2DBC

- **What is R2DBC?**
  - Reactive Relational Database Connectivity
  - Non-blocking database access for relational databases

- **Differences between JPA/JDBC and R2DBC:**
  - Blocking vs. non-blocking
  - Connection pooling differences
  - Transaction management

- **R2DBC repository methods:**
  - Query methods
  - Custom queries
  - Reactive transactions

### 4. Error Handling in Reactive Applications

- **Error handling operators:**
  - `onErrorResume`
  - `onErrorReturn`
  - `onErrorMap`
  - `doOnError`

- **Error handling strategies:**
  - Fallback values
  - Retry mechanisms
  - Circuit breakers

### 5. Testing Reactive Applications

- **WebTestClient:**
  - Testing reactive endpoints
  - Verifying responses

- **StepVerifier:**
  - Testing reactive streams
  - Asserting element values and completion signals

- **Mocking in reactive applications:**
  - Creating Mono/Flux test data
  - Mocking reactive repositories and services

### 6. Performance Considerations

- **Reactive vs. Traditional approaches:**
  - When to use reactive programming
  - Performance benefits and trade-offs

- **Common pitfalls:**
  - Blocking operations in reactive streams
  - Improper error handling
  - Subscription management

## Sample Interview Questions

1. **What is the difference between `Mono` and `Flux` in Project Reactor?**

2. **How does backpressure work in reactive streams?**

3. **Explain the difference between `map` and `flatMap` operators.**

4. **How would you handle errors in a reactive stream?**

5. **What is the purpose of the `switchIfEmpty` operator?**

6. **How does Spring WebFlux differ from traditional Spring MVC?**

7. **Explain the difference between annotation-based controllers and functional endpoints in Spring WebFlux.**

8. **What is R2DBC and how does it differ from JDBC?**

9. **How would you test a reactive endpoint?**

10. **What happens if you perform a blocking operation in a reactive stream?**

11. **How would you implement pagination in a reactive application?**

12. **Explain how transactions work in R2DBC.**

13. **What is the purpose of the `subscribeOn` and `publishOn` operators?**

14. **How would you implement a timeout with fallback in a reactive service?**

15. **What are the benefits of using constructor injection over field injection?**

### 7. Spring Security with Reactive Applications

- **Reactive Security vs. Traditional Security:**
  - Non-blocking security filters
  - Reactive authentication and authorization
  - SecurityWebFilterChain vs. SecurityFilterChain

- **JWT in Reactive Applications:**
  - Stateless authentication
  - Token generation and validation
  - Custom authentication managers

- **Role-based Access Control:**
  - Method-level security
  - URL-based security rules
  - Authority vs. Role differences

## Security-Related Interview Questions

16. **How does Spring Security work in a reactive application?**

17. **What is JWT and why is it suitable for reactive applications?**

18. **Explain the difference between authentication and authorization.**

19. **How would you implement role-based access control in Spring WebFlux?**

20. **What is the purpose of SecurityWebFilterChain?**

21. **How do you handle password encoding in a reactive application?**

22. **What are the benefits of stateless authentication?**

23. **How would you implement logout in a JWT-based system?**

24. **What security considerations should you have for reactive endpoints?**

25. **How do you test secured reactive endpoints?**

## Code Examples to Study

### 1. Creating a Reactive Repository

```java
@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {
    Flux<User> findByDepartment(String department);
    Mono<User> findByEmail(String email);
    
    @Query("SELECT * FROM users WHERE name LIKE :pattern")
    Flux<User> findByNamePattern(String pattern);
}
```

### 2. Error Handling in Services

```java
public Mono<User> getUserById(Long id) {
    return userRepository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("User not found with id: " + id)));
}

public Flux<User> getAllUsersWithFallback() {
    return userRepository.findAll()
            .onErrorResume(error -> {
                System.err.println("Database error, returning fallback user: " + error.getMessage());
                User fallbackUser = new User("Fallback User", "fallback@example.com", "System");
                return Flux.just(fallbackUser);
            })
            .timeout(Duration.ofSeconds(5))
            .onErrorResume(error -> {
                System.err.println("Timeout error, returning fallback user");
                User fallbackUser = new User("Fallback User", "fallback@example.com", "System");
                return Flux.just(fallbackUser);
            });
}
```

### 3. Reactive Controller Methods

```java
@GetMapping("/{id}")
public Mono<ResponseEntity<User>> getUserById(@PathVariable Long id) {
    return userService.getUserById(id)
            .map(user -> ResponseEntity.ok(user))
            .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()))
            .defaultIfEmpty(ResponseEntity.notFound().build());
}
```

### 4. Functional Endpoints

```java
@Bean
public RouterFunction<ServerResponse> functionalRoutes(UserHandler userHandler) {
    return route(GET("/api/functional/users"), userHandler::getAllUsers)
            .andRoute(GET("/api/functional/users/{id}"), userHandler::getUserById)
            .andRoute(POST("/api/functional/users"), userHandler::createUser);
}
```

### 5. Testing Reactive Endpoints

```java
@Test
void getUserById_ShouldReturnUser_WhenUserExists() {
    // Given
    when(userService.getUserById(1L)).thenReturn(Mono.just(testUser));

    // When & Then
    webTestClient.get()
            .uri("/api/users/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.name").isEqualTo("John Doe");
}
```

### 6. Security Configuration

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                        .pathMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("USER", "ADMIN")
                        .anyExchange().authenticated()
                )
                .addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
```

### 7. JWT Token Generation and Validation

```java
@Component
public class JwtUtil {

    public String generateToken(String username, List<String> roles) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(expiration, ChronoUnit.SECONDS);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 8. Authentication Service

```java
@Service
public class AuthService {

    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        return userRepository.findByEmail(loginRequest.getEmail())
                .filter(User::isEnabled)
                .filter(user -> passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
                .flatMap(user -> 
                    roleRepository.findByUserId(user.getId())
                            .map(Role::getName)
                            .collectList()
                            .map(roles -> {
                                String token = jwtUtil.generateToken(user.getEmail(), roles);
                                return new LoginResponse(token, user.getName(), roles);
                            })
                )
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")));
    }
}
```

### 9. Role-Based Access Control

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/roles")
    public Flux<Role> getAllRoles() {
        return roleService.getAllRoles();
    }

    @PostMapping("/users/{userId}/roles/{roleId}")
    public Mono<ResponseEntity<UserRole>> assignRoleToUser(@PathVariable Long userId, @PathVariable Long roleId) {
        return roleService.assignRoleToUser(userId, roleId)
                .map(userRole -> ResponseEntity.status(HttpStatus.CREATED).body(userRole))
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().build()));
    }
}
```

### 10. Testing Secured Endpoints

```java
@Test
void login_ShouldReturnToken_WhenCredentialsAreValid() {
    // Given
    when(authService.login(any(LoginRequest.class))).thenReturn(Mono.just(loginResponse));

    // When & Then
    webTestClient.post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.token").isEqualTo("jwt-token")
            .jsonPath("$.username").isEqualTo("Test User")
            .jsonPath("$.roles[0]").isEqualTo("USER");
}
```

## Best Practices

1. **Use constructor injection** over field injection for better testability and immutability

2. **Handle empty results** with `switchIfEmpty` or `defaultIfEmpty`

3. **Implement proper error handling** with operators like `onErrorResume`

4. **Avoid blocking operations** in reactive streams

5. **Use appropriate operators** for transforming and combining reactive streams

6. **Test both success and error scenarios** in your reactive code

7. **Consider timeouts** for operations that might take too long

8. **Use validation annotations** to ensure data integrity

9. **Return appropriate HTTP status codes** from controllers

10. **Document your API** for better maintainability

## Conclusion

This Spring Reactive Demo project demonstrates key concepts of reactive programming with Spring WebFlux and R2DBC. Understanding these concepts and being able to explain them clearly will help you succeed in your interview. Focus on the reactive programming paradigm, how it differs from traditional approaches, and the benefits it provides for building scalable and responsive applications.