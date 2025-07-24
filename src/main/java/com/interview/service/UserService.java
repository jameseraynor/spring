package com.interview.service;

import com.interview.model.User;
import com.interview.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    // Constructor injection (preferred over field injection)
    @Autowired
    public UserService(UserRepository userRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }
    
    // Mono examples - single value operations
    public Mono<User> createUser(User user) {
        return userRepository.save(user)
                .doOnSuccess(savedUser -> 
                    notificationService.sendWelcomeNotification(savedUser.getEmail())
                        .subscribe() // Fire and forget
                )
                .doOnError(error -> 
                    System.err.println("Error creating user: " + error.getMessage())
                );
    }
    
    public Mono<User> getUserById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found with id: " + id)));
    }
    
    public Mono<User> updateUser(Long id, User user) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .map(existingUser -> {
                    existingUser.setName(user.getName());
                    existingUser.setEmail(user.getEmail());
                    existingUser.setDepartment(user.getDepartment());
                    return existingUser;
                })
                .flatMap(userRepository::save);
    }
    
    public Mono<Void> deleteUser(Long id) {
        return userRepository.deleteById(id);
    }
    
    // Flux examples - multiple value operations
    public Flux<User> getAllUsers() {
        return userRepository.findAll()
                .delayElements(Duration.ofMillis(100)); // Simulate processing delay
    }
    
    public Flux<User> getUsersByDepartment(String department) {
        return userRepository.findByDepartment(department)
                .filter(user -> user.getName() != null && !user.getName().isEmpty());
    }
    
    public Flux<User> searchUsersByName(String namePattern) {
        return userRepository.findByNamePattern("%" + namePattern + "%")
                .take(10); // Limit results
    }
    
    // Advanced reactive operations
    public Flux<String> getUserEmailsByDepartment(String department) {
        return userRepository.findByDepartment(department)
                .map(User::getEmail)
                .distinct()
                .sort();
    }
    
    public Mono<Long> getUserCountByDepartment(String department) {
        return userRepository.countByDepartment(department);
    }
    
    // Demonstrating error handling and fallbacks
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
}