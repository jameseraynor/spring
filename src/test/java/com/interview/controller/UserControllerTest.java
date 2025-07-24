package com.interview.controller;

import com.interview.model.User;
import com.interview.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john@example.com", "Engineering");
        testUser.setId(1L);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        User user2 = new User("Jane Smith", "jane@example.com", "Marketing");
        user2.setId(2L);
        
        when(userService.getAllUsers()).thenReturn(Flux.just(testUser, user2));

        // When & Then
        webTestClient.get()
                .uri("/api/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(User.class)
                .hasSize(2);
    }

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
                .jsonPath("$.name").isEqualTo("John Doe")
                .jsonPath("$.email").isEqualTo("john@example.com")
                .jsonPath("$.department").isEqualTo("Engineering");
    }

    @Test
    void getUserById_ShouldReturn404_WhenUserNotFound() {
        // Given
        when(userService.getUserById(anyLong())).thenReturn(Mono.error(new RuntimeException("User not found")));

        // When & Then
        webTestClient.get()
                .uri("/api/users/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createUser_ShouldReturnCreatedUser() {
        // Given
        User newUser = new User("Jane Doe", "jane@example.com", "Marketing");
        User savedUser = new User("Jane Doe", "jane@example.com", "Marketing");
        savedUser.setId(2L);

        when(userService.createUser(any(User.class))).thenReturn(Mono.just(savedUser));

        // When & Then
        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newUser)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(2)
                .jsonPath("$.name").isEqualTo("Jane Doe")
                .jsonPath("$.email").isEqualTo("jane@example.com")
                .jsonPath("$.department").isEqualTo("Marketing");
    }

    @Test
    void createUser_ShouldReturn400_WhenValidationFails() {
        // Given
        User invalidUser = new User("", "invalid-email", "Marketing");
        when(userService.createUser(any(User.class))).thenReturn(Mono.error(new RuntimeException("Validation failed")));

        // When & Then
        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidUser)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() {
        // Given
        User updateData = new User("John Updated", "john.updated@example.com", "Marketing");
        User updatedUser = new User("John Updated", "john.updated@example.com", "Marketing");
        updatedUser.setId(1L);

        when(userService.updateUser(anyLong(), any(User.class))).thenReturn(Mono.just(updatedUser));

        // When & Then
        webTestClient.put()
                .uri("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateData)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("John Updated")
                .jsonPath("$.email").isEqualTo("john.updated@example.com")
                .jsonPath("$.department").isEqualTo("Marketing");
    }

    @Test
    void deleteUser_ShouldReturn204() {
        // Given
        when(userService.deleteUser(1L)).thenReturn(Mono.empty());

        // When & Then
        webTestClient.delete()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getUsersByDepartment_ShouldReturnFilteredUsers() {
        // Given
        when(userService.getUsersByDepartment("Engineering")).thenReturn(Flux.just(testUser));

        // When & Then
        webTestClient.get()
                .uri("/api/users/department/Engineering")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class)
                .hasSize(1);
    }

    @Test
    void searchUsers_ShouldReturnMatchingUsers() {
        // Given
        when(userService.searchUsersByName("John")).thenReturn(Flux.just(testUser));

        // When & Then
        webTestClient.get()
                .uri("/api/users/search?name=John")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class)
                .hasSize(1);
    }

    @Test
    void getUserCountByDepartment_ShouldReturnCount() {
        // Given
        when(userService.getUserCountByDepartment("Engineering")).thenReturn(Mono.just(5L));

        // When & Then
        webTestClient.get()
                .uri("/api/users/department/Engineering/count")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .isEqualTo(5L);
    }

    @Test
    void getUserEmailsByDepartment_ShouldReturnEmails() {
        // Given
        when(userService.getUserEmailsByDepartment("Engineering"))
                .thenReturn(Flux.just("alice@example.com"));

        // When & Then
        webTestClient.get()
                .uri("/api/users/department/Engineering/emails")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(String.class)
                .hasSize(1);
    }
}