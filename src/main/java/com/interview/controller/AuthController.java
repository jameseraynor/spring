package com.interview.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import com.interview.model.User;
import com.interview.security.dto.LoginRequest;
import com.interview.security.dto.LoginResponse;
import com.interview.security.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(error -> 
                    Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new LoginResponse(null, null, null)))
                );
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<User>> register(@Valid @RequestBody User user) {
        return authService.register(user)
                .map(registeredUser -> {
                    // Don't return password in response
                    registeredUser.setPassword(null);
                    return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
                })
                .onErrorResume(error -> 
                    Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build())
                );
    }
}