package com.securegenai.gateway.config;

/**
 * CORS configuration has been moved to
 * {@link com.securegenai.gateway.security.SecurityConfig#corsConfigurationSource()}.
 *
 * <p>In Spring Security 6, CORS must be configured through the security filter chain
 * via {@code http.cors(cors -> cors.configurationSource(...))} to ensure correct
 * filter ordering. A standalone {@code CorsFilter} bean registered outside the
 * security chain causes ordering conflicts and may silently bypass security rules.
 *
 * <p>This class is intentionally left as a documentation marker for Day 3.
 * It will be removed in a cleanup commit.
 *
 * @see com.securegenai.gateway.security.SecurityConfig
 */
public class CorsConfig {
    // Intentionally empty — CORS is configured in SecurityConfig.
}
