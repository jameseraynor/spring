package com.interview.security.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import com.interview.model.User;
import com.interview.repository.UserRepository;
import com.interview.security.dto.LoginRequest;
import com.interview.security.dto.LoginResponse;
import com.interview.security.jwt.JwtUtil;
import com.interview.security.model.Role;
import com.interview.security.model.UserRole;
import com.interview.security.repository.RoleRepository;
import com.interview.security.repository.UserRoleRepository;

import java.util.List;

@Service
/**
 * Coordinates authentication and registration workflows for the reactive API.
 *
 * Responsibilities:
 * - Authenticate users and issue JWTs containing role claims
 * - Register new users with a securely hashed password
 * - Assign a default USER role on registration
 * - Retrieve role names for a given user
 */
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                      RoleRepository roleRepository,
                      UserRoleRepository userRoleRepository,
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticates a user and returns a JWT-bearing response on success.
     *
     * Flow:
     * 1) Look up user by email
     * 2) Ensure the account is enabled
     * 3) Verify password using the configured {@link PasswordEncoder}
     * 4) Load role names and embed them as claims in the JWT
     * 5) Return {@link LoginResponse} with token, display name, and roles
     *
     * Emits a {@link RuntimeException} with message "Invalid credentials" when the
     * user is not found, disabled, or the password check fails.
     */
    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        return userRepository.findByEmail(loginRequest.getEmail())
                // Only allow login for enabled accounts
                .filter(User::isEnabled)
                // Verify provided password against the stored hash
                .filter(user -> passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
                .flatMap(user -> 
                    roleRepository.findByUserId(user.getId())
                            .map(Role::getName)
                            .collectList()
                            .map(roles -> {
                                // Include roles as claims so downstream authorization can rely on them
                                String token = jwtUtil.generateToken(user.getEmail(), roles);
                                return new LoginResponse(token, user.getName(), roles);
                            })
                )
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")));
    }

    /**
     * Registers a new user account.
     *
     * Behavior:
     * - Enforces unique email addresses
     * - Hashes the password using {@link PasswordEncoder}
     * - Persists the user and assigns the default USER role
     *
     * Returns the saved {@link User} on success; emits an error if the email
     * already exists.
     */
    public Mono<User> register(User user) {
        return userRepository.findByEmail(user.getEmail())
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("User already exists with email: " + user.getEmail()));
                    }
                    // Securely hash the password before persisting
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    
                    return userRepository.save(user)
                            .flatMap(savedUser -> 
                                // Assign default USER role for baseline access
                                roleRepository.findByName("USER")
                                        .flatMap(role -> {
                                            UserRole userRole = new UserRole(savedUser.getId(), role.getId());
                                            return userRoleRepository.save(userRole);
                                        })
                                        .then(Mono.just(savedUser))
                            );
                });
    }

    /**
     * Retrieves the list of role names for the user identified by the given email.
     * Returns an empty Mono if the user does not exist.
     */
    public Mono<List<String>> getUserRoles(String email) {
        return userRepository.findByEmail(email)
                .flatMap(user -> 
                    roleRepository.findByUserId(user.getId())
                            .map(Role::getName)
                            .collectList()
                );
    }
}