package com.securegenai.gateway.controller;

import com.securegenai.gateway.security.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Admin controller — platform administration endpoints.
 *
 * <p>All endpoints require the {@code ADMIN} role.
 * Method-level {@code @PreAuthorize} is an additional defense-in-depth layer
 * on top of the route-level rule in {@link com.securegenai.gateway.security.SecurityConfig}.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Platform administration endpoints (ADMIN role required)")
public class AdminController {

    // ─── Users List ───────────────────────────────────────────────────────────

    @Operation(
            summary = "List platform users",
            description = "Returns the registered demo users and their roles. "
                    + "In Day 4, this will query the database.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "User list returned")
    @ApiResponse(responseCode = "403", description = "ADMIN role required")
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> listUsers() {
        log.info("GET /api/v1/admin/users");

        List<Map<String, String>> users = List.of(
                Map.of("username", "admin",    "role", UserRole.ADMIN.name(),            "status", "ACTIVE"),
                Map.of("username", "analyst",  "role", UserRole.SECURITY_ANALYST.name(), "status", "ACTIVE"),
                Map.of("username", "employee", "role", UserRole.EMPLOYEE.name(),          "status", "ACTIVE")
        );

        return ResponseEntity.ok(Map.of(
                "users", users,
                "total", users.size(),
                "timestamp", Instant.now().toString()
        ));
    }

    // ─── Gateway Statistics ───────────────────────────────────────────────────

    @Operation(
            summary = "Gateway statistics",
            description = "Returns platform-level operational statistics. "
                    + "Placeholder implementation — full metrics wire-up comes in the observability phase.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Statistics returned")
    @ApiResponse(responseCode = "403", description = "ADMIN role required")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("GET /api/v1/admin/stats");

        long uptimeMs = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();

        return ResponseEntity.ok(Map.of(
                "service", "gateway-service",
                "version", "1.0.0",
                "day", "DAY-3",
                "phase", "AUTHENTICATION & AUTHORIZATION",
                "uptimeMs", uptimeMs,
                "roles", List.of(UserRole.values()),
                "securityMode", "JWT (local) / Cognito (profile: cognito)",
                "timestamp", Instant.now().toString()
        ));
    }
}
