package com.interview.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.interview.security.model.Role;
import com.interview.security.model.UserRole;
import com.interview.security.service.RoleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RoleService roleService;

    public AdminController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/roles")
    public Flux<Role> getAllRoles() {
        return roleService.getAllRoles();
    }

    @GetMapping("/roles/{id}")
    public Mono<ResponseEntity<Role>> getRoleById(@PathVariable Long id) {
        return roleService.getRoleById(id)
                .map(role -> ResponseEntity.ok(role))
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @PostMapping("/roles")
    public Mono<ResponseEntity<Role>> createRole(@Valid @RequestBody Role role) {
        return roleService.createRole(role)
                .map(createdRole -> ResponseEntity.status(HttpStatus.CREATED).body(createdRole))
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @GetMapping("/users/{userId}/roles")
    public Flux<Role> getUserRoles(@PathVariable Long userId) {
        return roleService.getUserRoles(userId);
    }

    @PostMapping("/users/{userId}/roles/{roleId}")
    public Mono<ResponseEntity<UserRole>> assignRoleToUser(@PathVariable Long userId, @PathVariable Long roleId) {
        return roleService.assignRoleToUser(userId, roleId)
                .map(userRole -> ResponseEntity.status(HttpStatus.CREATED).body(userRole))
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    public Mono<ResponseEntity<Void>> removeRoleFromUser(@PathVariable Long userId, @PathVariable Long roleId) {
        return roleService.removeRoleFromUser(userId, roleId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().build()));
    }
}