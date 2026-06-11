package com.securegenai.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Service responsible for JWT lifecycle management.
 *
 * <p>Supports:
 * <ul>
 *   <li>Generating HMAC-SHA256 signed access tokens (short-lived)</li>
 *   <li>Generating HMAC-SHA256 signed refresh tokens (long-lived)</li>
 *   <li>Validating and parsing tokens</li>
 *   <li>In-memory refresh token blacklisting for logout support</li>
 * </ul>
 *
 * <p>All tokens embed a {@code role} claim and a {@code type} claim
 * ({@code "access"} or {@code "refresh"}) to distinguish token kinds.
 *
 * <p><strong>Note:</strong> The blacklist is in-memory and resets on restart.
 * A Redis-backed blacklist will replace this in a future phase.
 */
@Slf4j
@Service
public class JwtService {

    // ─── Configuration ────────────────────────────────────────────────────────

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${security.jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    /** In-memory blacklist for invalidated refresh tokens (logout). */
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    // ─── Token Generation ─────────────────────────────────────────────────────

    /**
     * Generate a short-lived access token for the authenticated user.
     *
     * @param userDetails the authenticated user
     * @return signed JWT access token string
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Extract the first granted authority as the role (strip "ROLE_" prefix)
        userDetails.getAuthorities().stream()
                .findFirst()
                .ifPresent(authority -> {
                    String role = authority.getAuthority().replace("ROLE_", "");
                    claims.put("role", role);
                });
        claims.put("type", "access");
        return buildToken(claims, userDetails.getUsername(), accessTokenExpiry);
    }

    /**
     * Generate a long-lived refresh token for the authenticated user.
     *
     * @param userDetails the authenticated user
     * @return signed JWT refresh token string
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return buildToken(claims, userDetails.getUsername(), refreshTokenExpiry);
    }

    // ─── Token Validation ─────────────────────────────────────────────────────

    /**
     * Validate an access token against the current user's details.
     *
     * @param token       the JWT string
     * @param userDetails the user to validate against
     * @return {@code true} if valid, not expired, and not blacklisted
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token)
                    && !blacklistedTokens.contains(token)
                    && "access".equals(extractClaim(token, c -> c.get("type", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate a refresh token (must be type=refresh and not blacklisted).
     *
     * @param token the JWT string
     * @return {@code true} if valid refresh token
     */
    public boolean isRefreshTokenValid(String token) {
        try {
            return !isTokenExpired(token)
                    && !blacklistedTokens.contains(token)
                    && "refresh".equals(extractClaim(token, c -> c.get("type", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // ─── Claim Extraction ─────────────────────────────────────────────────────

    /**
     * Extract the username (subject) from a token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract the role claim from a token (e.g., "ADMIN").
     */
    public String extractRole(String token) {
        return extractClaim(token, c -> c.get("role", String.class));
    }

    /**
     * Extract token expiry as an {@link Instant}.
     */
    public Instant extractExpiry(String token) {
        return extractClaim(token, Claims::getExpiration).toInstant();
    }

    /**
     * Generic claim extractor using a resolver function.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ─── Blacklist Management ─────────────────────────────────────────────────

    /**
     * Blacklist a token, preventing it from being used again.
     * Used during logout to immediately invalidate a refresh token.
     *
     * @param token the token to invalidate
     */
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
        log.debug("Token blacklisted [username={}]", extractUsername(token));
    }

    /**
     * Check whether a token has been blacklisted.
     */
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiryMs) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expiryMs)))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private SecretKey getSigningKey() {
        // Derive a 256-bit HMAC-SHA256 key from the configured secret string.
        // In production, use a proper key management service (AWS KMS / Secrets Manager).
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
