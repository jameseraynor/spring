package com.interview.repository;

import com.interview.model.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {
    
    // Custom query methods demonstrating reactive queries
    Flux<User> findByDepartment(String department);
    
    Mono<User> findByEmail(String email);
    
    @Query("SELECT * FROM users WHERE name LIKE :pattern")
    Flux<User> findByNamePattern(String pattern);
    
    @Query("SELECT COUNT(*) FROM users WHERE department = :department")
    Mono<Long> countByDepartment(String department);
}