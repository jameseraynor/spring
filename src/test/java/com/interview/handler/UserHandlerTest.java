package com.interview.handler;

import com.interview.model.User;
import com.interview.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserHandlerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserHandler userHandler;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john@example.com", "Engineering");
        testUser.setId(1L);
    }

    @Test
    void getAllUsers_ShouldReturnOkResponse() {
        // Given
        User user2 = new User("Jane Smith", "jane@example.com", "Marketing");
        user2.setId(2L);
        
        when(userService.getAllUsers()).thenReturn(Flux.just(testUser, user2));
        
        ServerRequest request = MockServerRequest.builder().build();

        // When & Then
        StepVerifier.create(userHandler.getAllUsers(request))
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();
    }

    @Test
    void getUserById_ShouldReturnOkResponse_WhenUserExists() {
        // Given
        when(userService.getUserById(1L)).thenReturn(Mono.just(testUser));
        
        ServerRequest request = MockServerRequest.builder()
                .pathVariable("id", "1")
                .build();

        // When & Then
        StepVerifier.create(userHandler.getUserById(request))
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserNotExists() {
        // Given
        when(userService.getUserById(anyLong())).thenReturn(Mono.empty());
        
        ServerRequest request = MockServerRequest.builder()
                .pathVariable("id", "999")
                .build();

        // When & Then
        StepVerifier.create(userHandler.getUserById(request))
                .expectNextMatches(response -> response.statusCode().is4xxClientError())
                .verifyComplete();
    }

    @Test
    void createUser_ShouldReturnOkResponse() {
        // Given
        User newUser = new User("Jane Doe", "jane@example.com", "Marketing");
        User savedUser = new User("Jane Doe", "jane@example.com", "Marketing");
        savedUser.setId(2L);

        when(userService.createUser(any(User.class))).thenReturn(Mono.just(savedUser));
        
        ServerRequest request = MockServerRequest.builder()
                .body(Mono.just(newUser));

        // When & Then
        StepVerifier.create(userHandler.createUser(request))
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();
    }
}