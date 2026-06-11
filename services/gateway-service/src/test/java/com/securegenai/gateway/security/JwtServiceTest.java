package com.securegenai.gateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JwtService}.
 *
 * Tests cover:
 * - Access token generation and claim extraction
 * - Refresh token generation and validation
 * - Expired token rejection
 * - Token blacklisting for logout
 * - Type-based token validation (access vs refresh)
 */
@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;

    // 64-character hex string = 32 bytes = 256-bit key
    private static final String TEST_SECRET =
            "3d6f2a9b1e4c8f7d5a2b9c4e1f8d3a6c2b5e9f1a4d7c0b3e6a9d2f5c8b1e4a7d";

    private UserDetails adminUser;
    private UserDetails analystUser;
    private UserDetails employeeUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Inject config values via ReflectionTestUtils (simulates @Value injection)
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiry", 3600000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiry", 86400000L);

        adminUser = buildUser("admin", UserRole.ADMIN);
        analystUser = buildUser("analyst", UserRole.SECURITY_ANALYST);
        employeeUser = buildUser("employee", UserRole.EMPLOYEE);
    }

    // ─── Access Token Generation ──────────────────────────────────────────────

    @Test
    @DisplayName("Should generate a non-null access token")
    void shouldGenerateAccessToken() {
        String token = jwtService.generateAccessToken(adminUser);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("Should embed correct username in access token")
    void shouldEmbedUsernameInAccessToken() {
        String token = jwtService.generateAccessToken(analystUser);
        assertThat(jwtService.extractUsername(token)).isEqualTo("analyst");
    }

    @Test
    @DisplayName("Should embed correct role in access token")
    void shouldEmbedRoleInAccessToken() {
        String token = jwtService.generateAccessToken(adminUser);
        assertThat(jwtService.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Should embed SECURITY_ANALYST role correctly")
    void shouldEmbedSecurityAnalystRole() {
        String token = jwtService.generateAccessToken(analystUser);
        assertThat(jwtService.extractRole(token)).isEqualTo("SECURITY_ANALYST");
    }

    @Test
    @DisplayName("Should embed EMPLOYEE role correctly")
    void shouldEmbedEmployeeRole() {
        String token = jwtService.generateAccessToken(employeeUser);
        assertThat(jwtService.extractRole(token)).isEqualTo("EMPLOYEE");
    }

    // ─── Access Token Validation ──────────────────────────────────────────────

    @Test
    @DisplayName("Should validate a fresh access token as valid")
    void shouldValidateFreshAccessToken() {
        String token = jwtService.generateAccessToken(adminUser);
        assertThat(jwtService.isTokenValid(token, adminUser)).isTrue();
    }

    @Test
    @DisplayName("Should reject access token for wrong user")
    void shouldRejectTokenForWrongUser() {
        String token = jwtService.generateAccessToken(adminUser);
        assertThat(jwtService.isTokenValid(token, analystUser)).isFalse();
    }

    @Test
    @DisplayName("Should reject expired access token")
    void shouldRejectExpiredAccessToken() {
        // Create a service with 0ms expiry to immediately expire
        JwtService expiredService = new JwtService();
        ReflectionTestUtils.setField(expiredService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(expiredService, "accessTokenExpiry", 0L);
        ReflectionTestUtils.setField(expiredService, "refreshTokenExpiry", 86400000L);

        String token = expiredService.generateAccessToken(adminUser);
        assertThat(expiredService.isTokenValid(token, adminUser)).isFalse();
    }

    @Test
    @DisplayName("Should reject refresh token when used as access token")
    void shouldRejectRefreshTokenAsAccessToken() {
        String refreshToken = jwtService.generateRefreshToken(adminUser);
        // Refresh tokens have type="refresh", so isTokenValid (which checks type="access") should return false
        assertThat(jwtService.isTokenValid(refreshToken, adminUser)).isFalse();
    }

    // ─── Refresh Token ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should generate a non-null refresh token")
    void shouldGenerateRefreshToken() {
        String token = jwtService.generateRefreshToken(adminUser);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("Should validate a fresh refresh token")
    void shouldValidateFreshRefreshToken() {
        String token = jwtService.generateRefreshToken(adminUser);
        assertThat(jwtService.isRefreshTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("Should reject access token when used as refresh token")
    void shouldRejectAccessTokenAsRefreshToken() {
        String accessToken = jwtService.generateAccessToken(adminUser);
        assertThat(jwtService.isRefreshTokenValid(accessToken)).isFalse();
    }

    // ─── Blacklisting ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should blacklist token and reject it on next use")
    void shouldBlacklistToken() {
        String refreshToken = jwtService.generateRefreshToken(adminUser);

        assertThat(jwtService.isRefreshTokenValid(refreshToken)).isTrue();

        jwtService.blacklistToken(refreshToken);

        assertThat(jwtService.isBlacklisted(refreshToken)).isTrue();
        assertThat(jwtService.isRefreshTokenValid(refreshToken)).isFalse();
    }

    @Test
    @DisplayName("Blacklisted access token should also fail isTokenValid check")
    void shouldRejectBlacklistedAccessToken() {
        String accessToken = jwtService.generateAccessToken(adminUser);

        assertThat(jwtService.isTokenValid(accessToken, adminUser)).isTrue();

        jwtService.blacklistToken(accessToken);

        assertThat(jwtService.isTokenValid(accessToken, adminUser)).isFalse();
    }

    // ─── Malformed Token ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return false for completely malformed token")
    void shouldReturnFalseForMalformedToken() {
        assertThat(jwtService.isRefreshTokenValid("this.is.not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when extracting claims from malformed token")
    void shouldThrowForMalformedTokenExtraction() {
        assertThatThrownBy(() -> jwtService.extractUsername("not-a-jwt"))
                .isInstanceOf(Exception.class);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private static UserDetails buildUser(String username, UserRole role) {
        return User.builder()
                .username(username)
                .password("password") // irrelevant for JWT tests
                .authorities(List.of(new SimpleGrantedAuthority(role.toAuthority())))
                .build();
    }
}
