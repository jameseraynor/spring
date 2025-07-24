package com.interview.controller;

import com.interview.model.User;
import com.interview.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    public Flux<User> getAllUsers() {
        return userService.getAllUsers()
                .map(this::sanitizeUser);
    }
    
    @GetMapping("/{id}")
    public Mono<ResponseEntity<User>> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(sanitizeUser(user)))
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public Mono<ResponseEntity<User>> createUser(@Valid @RequestBody User user) {
        return userService.createUser(user)
                .map(savedUser -> ResponseEntity.status(HttpStatus.CREATED).body(sanitizeUser(savedUser)))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
    
    @PutMapping("/{id}")
    public Mono<ResponseEntity<User>> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        return userService.updateUser(id, user)
                .map(updatedUser -> ResponseEntity.ok(sanitizeUser(updatedUser)))
                .onErrorReturn(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorReturn(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/department/{department}")
    public Flux<User> getUsersByDepartment(@PathVariable String department) {
        return userService.getUsersByDepartment(department)
                .map(this::sanitizeUser);
    }
    
    @GetMapping("/search")
    public Flux<User> searchUsers(@RequestParam String name) {
        return userService.searchUsersByName(name)
                .map(this::sanitizeUser);
    }
    
    @GetMapping("/department/{department}/count")
    public Mono<Long> getUserCountByDepartment(@PathVariable String department) {
        return userService.getUserCountByDepartment(department);
    }
    
    @GetMapping("/department/{department}/emails")
    public Flux<String> getUserEmailsByDepartment(@PathVariable String department) {
        return userService.getUserEmailsByDepartment(department);
    }
    
    // Helper method to remove password from response
    private User sanitizeUser(User user) {
        User sanitizedUser = new User();
        sanitizedUser.setId(user.getId());
        sanitizedUser.setName(user.getName());
        sanitizedUser.setEmail(user.getEmail());
        sanitizedUser.setDepartment(user.getDepartment());
        sanitizedUser.setEnabled(user.isEnabled());
        // Password is intentionally not set
        return sanitizedUser;
    }
}