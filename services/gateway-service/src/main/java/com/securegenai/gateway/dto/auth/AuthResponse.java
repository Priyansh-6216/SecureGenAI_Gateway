package com.securegenai.gateway.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response body for successful authentication (login or token refresh).
 *
 * <p>Contains both the short-lived access token and the long-lived refresh token.
 * Clients should store the refresh token securely (e.g., httpOnly cookie or
 * secure storage) and use it to obtain new access tokens when the current one expires.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing JWT tokens")
public class AuthResponse {

    @Schema(description = "JWT access token (short-lived)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "JWT refresh token (long-lived)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token validity in seconds", example = "3600")
    private long expiresIn;

    @Schema(description = "Authenticated username", example = "analyst")
    private String username;

    @Schema(description = "User's platform role", example = "SECURITY_ANALYST")
    private String role;

    @Schema(description = "Token issuance timestamp")
    private Instant issuedAt;
}
