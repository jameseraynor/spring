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

    public Mono<User> register(User user) {
        return userRepository.findByEmail(user.getEmail())
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("User already exists with email: " + user.getEmail()));
                    }
                    
                    // Encode password
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    
                    return userRepository.save(user)
                            .flatMap(savedUser -> 
                                // Assign default USER role
                                roleRepository.findByName("USER")
                                        .flatMap(role -> {
                                            UserRole userRole = new UserRole(savedUser.getId(), role.getId());
                                            return userRoleRepository.save(userRole);
                                        })
                                        .then(Mono.just(savedUser))
                            );
                });
    }

    public Mono<List<String>> getUserRoles(String email) {
        return userRepository.findByEmail(email)
                .flatMap(user -> 
                    roleRepository.findByUserId(user.getId())
                            .map(Role::getName)
                            .collectList()
                );
    }
}