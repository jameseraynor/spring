package com.interview.service;

import com.interview.model.User;
import com.interview.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john@example.com", "Engineering");
        testUser.setId(1L);
    }

    @Test
    void createUser_ShouldReturnSavedUser() {
        // Given
        User newUser = new User("Jane Doe", "jane@example.com", "Marketing");
        User savedUser = new User("Jane Doe", "jane@example.com", "Marketing");
        savedUser.setId(2L);

        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));
        when(notificationService.sendWelcomeNotification(anyString())).thenReturn(Mono.just("Notification sent"));

        // When & Then
        StepVerifier.create(userService.createUser(newUser))
                .expectNext(savedUser)
                .verifyComplete();

        verify(userRepository).save(newUser);
        verify(notificationService).sendWelcomeNotification("jane@example.com");
    }

    @Test
    void createUser_ShouldHandleError() {
        // Given
        User newUser = new User("Jane Doe", "jane@example.com", "Marketing");
        when(userRepository.save(any(User.class))).thenReturn(Mono.error(new RuntimeException("Database error")));

        // When & Then
        StepVerifier.create(userService.createUser(newUser))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));

        // When & Then
        StepVerifier.create(userService.getUserById(1L))
                .expectNext(testUser)
                .verifyComplete();

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_ShouldReturnError_WhenUserNotFound() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(userService.getUserById(999L))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("User not found with id: 999"))
                .verify();
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() {
        // Given
        User updateData = new User("John Updated", "john.updated@example.com", "Marketing");
        User updatedUser = new User("John Updated", "john.updated@example.com", "Marketing");
        updatedUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));

        // When & Then
        StepVerifier.create(userService.updateUser(1L, updateData))
                .expectNext(updatedUser)
                .verifyComplete();

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldCompleteSuccessfully() {
        // Given
        when(userRepository.deleteById(1L)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(userService.deleteUser(1L))
                .verifyComplete();

        verify(userRepository).deleteById(1L);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        User user2 = new User("Jane Smith", "jane@example.com", "Marketing");
        user2.setId(2L);
        
        when(userRepository.findAll()).thenReturn(Flux.just(testUser, user2));

        // When & Then
        StepVerifier.create(userService.getAllUsers())
                .expectNext(testUser)
                .expectNext(user2)
                .verifyComplete();

        verify(userRepository).findAll();
    }

    @Test
    void getUsersByDepartment_ShouldReturnFilteredUsers() {
        // Given
        User engineeringUser = new User("Bob Johnson", "bob@example.com", "Engineering");
        engineeringUser.setId(3L);
        
        when(userRepository.findByDepartment("Engineering"))
                .thenReturn(Flux.just(testUser, engineeringUser));

        // When & Then
        StepVerifier.create(userService.getUsersByDepartment("Engineering"))
                .expectNext(testUser)
                .expectNext(engineeringUser)
                .verifyComplete();

        verify(userRepository).findByDepartment("Engineering");
    }

    @Test
    void searchUsersByName_ShouldReturnMatchingUsers() {
        // Given
        when(userRepository.findByNamePattern("%John%")).thenReturn(Flux.just(testUser));

        // When & Then
        StepVerifier.create(userService.searchUsersByName("John"))
                .expectNext(testUser)
                .verifyComplete();

        verify(userRepository).findByNamePattern("%John%");
    }

    @Test
    void getUserEmailsByDepartment_ShouldReturnSortedEmails() {
        // Given
        User user2 = new User("Alice Brown", "alice@example.com", "Engineering");
        user2.setId(2L);
        
        when(userRepository.findByDepartment("Engineering"))
                .thenReturn(Flux.just(testUser, user2));

        // When & Then
        StepVerifier.create(userService.getUserEmailsByDepartment("Engineering"))
                .expectNext("alice@example.com")
                .expectNext("john@example.com")
                .verifyComplete();
    }

    @Test
    void getUserCountByDepartment_ShouldReturnCount() {
        // Given
        when(userRepository.countByDepartment("Engineering")).thenReturn(Mono.just(5L));

        // When & Then
        StepVerifier.create(userService.getUserCountByDepartment("Engineering"))
                .expectNext(5L)
                .verifyComplete();

        verify(userRepository).countByDepartment("Engineering");
    }

    @Test
    void getAllUsersWithFallback_ShouldReturnFallbackOnError() {
        // Given
        when(userRepository.findAll()).thenReturn(Flux.error(new RuntimeException("Database error")));

        // When & Then
        StepVerifier.create(userService.getAllUsersWithFallback())
                .expectNextMatches(user -> 
                    user.getName().equals("Fallback User") && 
                    user.getEmail().equals("fallback@example.com") && 
                    user.getDepartment().equals("System")
                )
                .verifyComplete();
    }
}