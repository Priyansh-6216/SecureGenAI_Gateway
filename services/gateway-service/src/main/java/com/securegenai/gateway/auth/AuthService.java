package com.securegenai.gateway.auth;

import com.securegenai.gateway.dto.auth.AuthResponse;
import com.securegenai.gateway.dto.auth.LoginRequest;
import com.securegenai.gateway.dto.auth.RefreshRequest;
import com.securegenai.gateway.exception.GatewayException;
import com.securegenai.gateway.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core authentication service for SecureGenAI Gateway.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Authenticate users via username/password (delegated to Spring's {@link AuthenticationManager}).</li>
 *   <li>Issue access + refresh JWT token pairs.</li>
 *   <li>Validate refresh tokens and issue new access tokens.</li>
 *   <li>Blacklist tokens on logout.</li>
 * </ul>
 *
 * <p><strong>Note:</strong> The refresh token store is in-memory for Day 3.
 * A Redis-backed store will be added in a later phase for scalability.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    /** In-memory store of valid refresh tokens (username → token). Resets on restart. */
    private final Set<String> activeRefreshTokens = ConcurrentHashMap.newKeySet();

    // ─── Login ────────────────────────────────────────────────────────────────

    /**
     * Authenticate a user with username and password.
     * Returns an {@link AuthResponse} containing both JWT tokens on success.
     *
     * @param request login credentials
     * @return {@link AuthResponse} with access + refresh tokens
     * @throws GatewayException.UnauthorizedException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            log.info("Login successful [user={}]", userDetails.getUsername());

            return buildAuthResponse(userDetails);

        } catch (BadCredentialsException e) {
            log.warn("Login failed for user: {}", request.getUsername());
            throw new GatewayException.UnauthorizedException("Invalid username or password");
        }
    }

    // ─── Refresh ──────────────────────────────────────────────────────────────

    /**
     * Exchange a valid refresh token for a new access + refresh token pair.
     * The old refresh token is immediately blacklisted (rotation strategy).
     *
     * @param request containing the refresh token
     * @return new {@link AuthResponse}
     * @throws GatewayException.UnauthorizedException if the refresh token is invalid or expired
     */
    public AuthResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            log.warn("Invalid or expired refresh token");
            throw new GatewayException.UnauthorizedException("Refresh token is invalid or expired");
        }

        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Blacklist old refresh token (rotation)
        jwtService.blacklistToken(refreshToken);
        activeRefreshTokens.remove(refreshToken);

        log.info("Token refreshed [user={}]", username);
        return buildAuthResponse(userDetails);
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    /**
     * Logout by blacklisting the provided refresh token.
     * Access tokens are short-lived and self-expire; only refresh tokens need blacklisting.
     *
     * @param refreshToken the refresh token to invalidate
     */
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                String username = jwtService.extractUsername(refreshToken);
                jwtService.blacklistToken(refreshToken);
                activeRefreshTokens.remove(refreshToken);
                log.info("Logout successful [user={}]", username);
            } catch (Exception e) {
                log.warn("Logout called with invalid token: {}", e.getMessage());
            }
        }
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(UserDetails userDetails) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Track active refresh token
        activeRefreshTokens.add(refreshToken);

        // Extract role for the response (strip ROLE_ prefix)
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("UNKNOWN");

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600)          // 1 hour — matches access-token-expiry in application.yml
                .username(userDetails.getUsername())
                .role(role)
                .issuedAt(Instant.now())
                .build();
    }
}
