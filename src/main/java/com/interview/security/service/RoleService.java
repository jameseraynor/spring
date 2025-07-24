package com.interview.security.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.interview.security.model.Role;
import com.interview.security.model.UserRole;
import com.interview.security.repository.RoleRepository;
import com.interview.security.repository.UserRoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public RoleService(RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public Flux<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Mono<Role> getRoleById(Long id) {
        return roleRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Role not found with id: " + id)));
    }

    public Mono<Role> getRoleByName(String name) {
        return roleRepository.findByName(name)
                .switchIfEmpty(Mono.error(new RuntimeException("Role not found with name: " + name)));
    }

    public Flux<Role> getUserRoles(Long userId) {
        return roleRepository.findByUserId(userId);
    }

    public Mono<UserRole> assignRoleToUser(Long userId, Long roleId) {
        return userRoleRepository.findByUserIdAndRoleId(userId, roleId)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("User already has this role"));
                    }
                    UserRole userRole = new UserRole(userId, roleId);
                    return userRoleRepository.save(userRole);
                });
    }

    public Mono<Void> removeRoleFromUser(Long userId, Long roleId) {
        return userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
    }

    public Mono<Role> createRole(Role role) {
        return roleRepository.findByName(role.getName())
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("Role already exists with name: " + role.getName()));
                    }
                    return roleRepository.save(role);
                });
    }
}