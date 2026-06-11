package com.securegenai.gateway.auth;

import com.securegenai.gateway.dto.auth.AuthResponse;
import com.securegenai.gateway.dto.auth.LoginRequest;
import com.securegenai.gateway.dto.auth.RefreshRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * Authentication controller — manages login, token refresh, logout, and current user info.
 *
 * <p>All endpoints under {@code /api/v1/auth} except {@code /me} and {@code /logout}
 * are publicly accessible (no JWT required).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "JWT-based authentication endpoints")
public class AuthController {

    private final AuthService authService;

    // ─── Login ────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Login",
            description = "Authenticate with username and password. Returns a JWT access token and refresh token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login [user={}]", request.getUsername());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // ─── Refresh ──────────────────────────────────────────────────────────────

    @Operation(
            summary = "Refresh access token",
            description = "Exchange a valid refresh token for a new access + refresh token pair. "
                    + "The old refresh token is immediately invalidated (rotation)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token invalid or expired")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        log.info("POST /api/v1/auth/refresh");
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    @Operation(
            summary = "Logout",
            description = "Invalidate the provided refresh token. Access tokens are short-lived and self-expire.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestBody(required = false) RefreshRequest request,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "unknown";
        log.info("POST /api/v1/auth/logout [user={}]", username);

        if (request != null && request.getRefreshToken() != null) {
            authService.logout(request.getRefreshToken());
        }

        return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully",
                "timestamp", Instant.now().toString()
        ));
    }

    // ─── Current User ─────────────────────────────────────────────────────────

    @Operation(
            summary = "Get current user info",
            description = "Returns the authenticated user's details extracted from the JWT token.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current user info"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.debug("GET /api/v1/auth/me [user={}]", userDetails.getUsername());

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .orElse("UNKNOWN");

        return ResponseEntity.ok(Map.of(
                "username", userDetails.getUsername(),
                "role", role,
                "authorities", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList(),
                "timestamp", Instant.now().toString()
        ));
    }
}
