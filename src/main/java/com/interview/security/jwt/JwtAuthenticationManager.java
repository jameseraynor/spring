package com.interview.security.jwt;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationManager(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        
        return Mono.just(authToken)
                .filter(token -> !token.isEmpty())
                .filter(jwtUtil::isTokenValid)
                .map(token -> {
                    String username = jwtUtil.getUsernameFromToken(token);
                    List<String> roles = jwtUtil.getRolesFromToken(token);
                    
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());
                    
                    return new UsernamePasswordAuthenticationToken(username, null, authorities);
                })
                .cast(Authentication.class);
    }
}