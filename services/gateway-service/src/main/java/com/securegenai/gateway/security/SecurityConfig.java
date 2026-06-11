package com.securegenai.gateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration for the SecureGenAI Gateway.
 *
 * <p><strong>Security model:</strong>
 * <ul>
 *   <li>Stateless — no HTTP sessions; every request is authenticated via JWT.</li>
 *   <li>CSRF disabled — appropriate for a stateless REST API consumed by SPAs and services.</li>
 *   <li>CORS is configured centrally here (replacing the standalone {@code CorsConfig} bean
 *       to avoid filter-chain ordering conflicts).</li>
 * </ul>
 *
 * <p><strong>Route access rules:</strong>
 * <pre>
 * Public (no token required):
 *   GET  /api/v1/health
 *   POST /api/v1/auth/login
 *   POST /api/v1/auth/refresh
 *   GET  /swagger-ui/**
 *   GET  /v3/api-docs/**
 *   GET  /actuator/health
 *
 * EMPLOYEE + above:
 *   POST /api/v1/prompts
 *
 * SECURITY_ANALYST + above:
 *   POST /api/v1/validate
 *
 * ADMIN only:
 *   GET/POST /api/v1/admin/**
 *
 * Authenticated (any role):
 *   GET  /api/v1/auth/me
 *   POST /api/v1/auth/logout
 *   Everything else
 * </pre>
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity           // enables @PreAuthorize / @PostAuthorize on methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    // ─── Security Filter Chain ────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — stateless REST API
            .csrf(AbstractHttpConfigurer::disable)

            // CORS — use the bean defined in this class
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Route-level authorization rules
            .authorizeHttpRequests(auth -> auth

                // ── Public endpoints ──────────────────────────────────────
                .requestMatchers(
                        "/api/v1/health",
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/actuator/health",
                        "/actuator/info"
                ).permitAll()

                // ── Admin only ────────────────────────────────────────────
                .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")

                // ── Security Analyst + Admin ──────────────────────────────
                .requestMatchers("/api/v1/validate")
                    .hasAnyRole("ADMIN", "SECURITY_ANALYST")

                // ── All authenticated users (any role) ────────────────────
                .requestMatchers("/api/v1/prompts")
                    .hasAnyRole("ADMIN", "SECURITY_ANALYST", "EMPLOYEE")

                // ── Everything else requires authentication ────────────────
                .anyRequest().authenticated()
            )

            // Stateless session — no server-side sessions
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Wire our JWT filter before the standard username/password filter
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Security filter chain configured");
        return http.build();
    }

    // ─── CORS ─────────────────────────────────────────────────────────────────

    /**
     * Centralized CORS configuration (replaces the standalone CorsConfig bean).
     * Allows the React frontend at localhost:5173 / localhost:3000.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    // ─── Auth Provider & Beans ────────────────────────────────────────────────

    /**
     * DAO-based authentication provider using BCrypt password hashing.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * BCrypt password encoder (strength 12).
     * Used for password verification during login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Exposes the {@link AuthenticationManager} for use in {@link com.securegenai.gateway.auth.AuthService}.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
