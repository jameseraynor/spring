package com.interview.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
/**
 * Configures Spring Security for a reactive WebFlux application.
 *
 * This configuration uses stateless JWT-based authentication:
 * - No HTTP session is created or used
 * - The client includes a Bearer token with each request
 * - Authorization is enforced via role-based rules per route
 */
public class SecurityConfig {

    private final JwtAuthenticationManager jwtAuthenticationManager;
    private final JwtServerAuthenticationConverter jwtServerAuthenticationConverter;

    public SecurityConfig(JwtAuthenticationManager jwtAuthenticationManager,
                         JwtServerAuthenticationConverter jwtServerAuthenticationConverter) {
        this.jwtAuthenticationManager = jwtAuthenticationManager;
        this.jwtServerAuthenticationConverter = jwtServerAuthenticationConverter;
    }

    @Bean
    /**
     * Builds the primary reactive security filter chain.
     *
     * Key points:
     * - Disables stateful/browser-centric mechanisms (CSRF, HTTP Basic, form login, server logout)
     * - Uses a no-op SecurityContextRepository to keep the app fully stateless
     * - Defines fine-grained route authorization with role checks
     * - Registers a custom JWT AuthenticationWebFilter at the AUTHENTICATION phase
     */
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // CSRF protection is primarily for browser cookie-based sessions; for
                // stateless APIs using tokens, it is typically disabled.
                .csrf(csrf -> csrf.disable())

                // Disable HTTP Basic auth; clients authenticate via JWT Bearer tokens instead.
                .httpBasic(httpBasic -> httpBasic.disable())

                // No server-rendered login page or form flow for APIs.
                .formLogin(formLogin -> formLogin.disable())

                // No server-side logout handling; clients drop their token to "log out".
                .logout(logout -> logout.disable())

                // Do not persist SecurityContext in a WebSession; each request supplies
                // credentials via the Authorization header (Bearer <token>).
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                // Route-level authorization rules.
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints
                        .pathMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                        // H2 console is typically used during local development only.
                        // Consider locking this down or disabling it in production.
                        .pathMatchers("/h2-console/**").permitAll()
                        
                        // Admin only endpoints
                        // Note: hasRole("ADMIN") expects authorities in the form "ROLE_ADMIN".
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
                // Register the JWT authentication filter at the AUTHENTICATION phase so that
                // downstream authorization checks can rely on an authenticated Principal.
                .addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    /**
     * Creates the authentication filter that:
     * - Delegates credential validation to {@link JwtAuthenticationManager}
     * - Extracts Bearer tokens from the request via {@link JwtServerAuthenticationConverter}
     */
    public AuthenticationWebFilter authenticationWebFilter() {
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
        authenticationWebFilter.setServerAuthenticationConverter(jwtServerAuthenticationConverter);
        return authenticationWebFilter;
    }

    @Bean
    /**
     * Password encoder for hashing user passwords using BCrypt.
     */
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}