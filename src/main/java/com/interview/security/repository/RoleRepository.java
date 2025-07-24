package com.interview.security.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.interview.security.model.Role;

@Repository
public interface RoleRepository extends R2dbcRepository<Role, Long> {
    
    Mono<Role> findByName(String name);
    
    @Query("SELECT r.* FROM roles r " +
           "JOIN user_roles ur ON r.id = ur.role_id " +
           "WHERE ur.user_id = :userId")
    Flux<Role> findByUserId(Long userId);
}