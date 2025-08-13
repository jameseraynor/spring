package com.interview.security.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.interview.security.model.Role;
import com.interview.security.model.UserRole;
import com.interview.security.repository.RoleRepository;
import com.interview.security.repository.UserRoleRepository;

@Service
/**
 * Provides reactive role management and user-role assignment operations.
 *
 * Responsibilities:
 * - Query roles by id and name
 * - List all roles and roles for a given user
 * - Assign and remove roles from users with duplicate checks
 * - Create roles while preventing duplicates by name
 */
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public RoleService(RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * Returns all available roles.
     */
    public Flux<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * Retrieves a role by its identifier.
     * Emits a {@link RuntimeException} if the role does not exist.
     */
    public Mono<Role> getRoleById(Long id) {
        return roleRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Role not found with id: " + id)));
    }

    /**
     * Retrieves a role by its name.
     * Emits a {@link RuntimeException} if the role does not exist.
     */
    public Mono<Role> getRoleByName(String name) {
        return roleRepository.findByName(name)
                .switchIfEmpty(Mono.error(new RuntimeException("Role not found with name: " + name)));
    }

    /**
     * Lists all roles assigned to the specified user.
     */
    public Flux<Role> getUserRoles(Long userId) {
        return roleRepository.findByUserId(userId);
    }

    /**
     * Assigns a role to a user if not already assigned.
     * Emits a {@link RuntimeException} if the mapping already exists.
     */
    public Mono<UserRole> assignRoleToUser(Long userId, Long roleId) {
        return userRoleRepository.findByUserIdAndRoleId(userId, roleId)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("User already has this role"));
                    }
                    // Create and persist the user-role link when no duplicate exists
                    UserRole userRole = new UserRole(userId, roleId);
                    return userRoleRepository.save(userRole);
                });
    }

    /**
     * Removes a role assignment from a user. No-op if the mapping does not exist.
     */
    public Mono<Void> removeRoleFromUser(Long userId, Long roleId) {
        return userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
    }

    /**
     * Creates a new role, ensuring uniqueness by role name.
     * Emits a {@link RuntimeException} if a role with the same name already exists.
     */
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