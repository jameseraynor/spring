package com.interview.security.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.interview.security.model.UserRole;

@Repository
public interface UserRoleRepository extends R2dbcRepository<UserRole, Long> {
    
    Flux<UserRole> findByUserId(Long userId);
    
    Flux<UserRole> findByRoleId(Long roleId);
    
    Mono<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);
    
    Mono<Void> deleteByUserId(Long userId);
    
    Mono<Void> deleteByUserIdAndRoleId(Long userId, Long roleId);
}