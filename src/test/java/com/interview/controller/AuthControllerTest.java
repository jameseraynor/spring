package com.interview.controller;

import com.interview.model.User;
import com.interview.security.dto.LoginRequest;
import com.interview.security.dto.LoginResponse;
import com.interview.security.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthService authService;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private User testUser;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("test@example.com", "password123");
        loginResponse = new LoginResponse("jwt-token", "Test User", List.of("USER"));
        testUser = new User("Test User", "test@example.com", "password123", "Engineering");
        testUser.setId(1L);
    }

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
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.token").isEqualTo("jwt-token")
                .jsonPath("$.username").isEqualTo("Test User")
                .jsonPath("$.roles[0]").isEqualTo("USER");
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsAreInvalid() {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Invalid credentials")));

        // When & Then
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void register_ShouldReturnCreatedUser_WhenUserIsValid() {
        // Given
        User registeredUser = new User("Test User", "test@example.com", "Engineering");
        registeredUser.setId(1L);
        when(authService.register(any(User.class))).thenReturn(Mono.just(registeredUser));

        // When & Then
        webTestClient.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testUser)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Test User")
                .jsonPath("$.email").isEqualTo("test@example.com")
                .jsonPath("$.password").doesNotExist(); // Password should not be returned
    }

    @Test
    void register_ShouldReturnBadRequest_WhenUserAlreadyExists() {
        // Given
        when(authService.register(any(User.class)))
                .thenReturn(Mono.error(new RuntimeException("User already exists")));

        // When & Then
        webTestClient.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testUser)
                .exchange()
                .expectStatus().isBadRequest();
    }
}