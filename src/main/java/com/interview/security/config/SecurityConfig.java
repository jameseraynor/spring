package com.interview.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import com.interview.security.jwt.JwtAuthenticationManager;
import com.interview.security.jwt.JwtServerAuthenticationConverter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthenticationManager jwtAuthenticationManager;
    private final JwtServerAuthenticationConverter jwtServerAuthenticationConverter;

    public SecurityConfig(JwtAuthenticationManager jwtAuthenticationManager,
                         JwtServerAuthenticationConverter jwtServerAuthenticationConverter) {
        this.jwtAuthenticationManager = jwtAuthenticationManager;
        this.jwtServerAuthenticationConverter = jwtServerAuthenticationConverter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints
                        .pathMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                        .pathMatchers("/h2-console/**").permitAll()
                        
                        // Admin only endpoints
                        .pathMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/admin/**").hasRole("ADMIN")
                        
                        // User and Admin endpoints
                        .pathMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/users/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                        
                        // Functional endpoints
                        .pathMatchers("/api/functional/**").hasAnyRole("USER", "ADMIN")
                        
                        // All other requests need authentication
                        .anyExchange().authenticated()
                )
                .addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public AuthenticationWebFilter authenticationWebFilter() {
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
        authenticationWebFilter.setServerAuthenticationConverter(jwtServerAuthenticationConverter);
        return authenticationWebFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}