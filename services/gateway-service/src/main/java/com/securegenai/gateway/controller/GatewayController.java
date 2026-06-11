package com.securegenai.gateway.controller;

import com.securegenai.gateway.dto.*;
import com.securegenai.gateway.service.PromptSecurityService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;

/**
 * Gateway Controller — the centralized entry point for all AI requests.
 *
 * Exposes:
 * - POST /api/v1/prompts   — Submit a prompt for full security processing
 * - POST /api/v1/validate  — Validate a prompt without LLM forwarding
 * - GET  /api/v1/health    — Custom health check
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Gateway", description = "Centralized AI Security Gateway endpoints")
public class GatewayController {

    private final PromptSecurityService promptSecurityService;

    /**
     * Submit a prompt for full security processing.
     * The gateway will:
     * 1. Detect PII entities
     * 2. Mask sensitive data
     * 3. Calculate risk score
     * 4. Evaluate security policies
     * 5. Return the processed result
     */
    @Operation(
            summary = "Process a prompt",
            description = "Submit a prompt through the full security pipeline: PII detection, "
                    + "data masking, risk scoring, and policy enforcement. Returns the security "
                    + "evaluation result including the masked prompt.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prompt processed successfully",
                    content = @Content(schema = @Schema(implementation = PromptResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Prompt blocked by security policy or insufficient role",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/prompts")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECURITY_ANALYST', 'EMPLOYEE')")
    public ResponseEntity<PromptResponse> processPrompt(
            @Valid @RequestBody PromptRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        // Auto-populate userId from JWT when not explicitly provided in the body
        String effectiveUserId = (request.getUserId() != null && !request.getUserId().isBlank())
                ? request.getUserId()
                : (principal != null ? principal.getUsername() : "anonymous");

        log.info("POST /api/v1/prompts [provider={}] [userId={}]",
                request.getProvider(), effectiveUserId);

        PromptResponse response = promptSecurityService.processPrompt(
                request.getPrompt(),
                request.getProvider(),
                effectiveUserId
        );

        if ("BLOCK".equals(response.getAction())) {
            throw new com.securegenai.gateway.exception.GatewayException.PromptBlockedException(
                    response.getReason(),
                    response.getPolicyTriggered()
            );
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Validate a prompt without forwarding to an LLM provider.
     * Useful for pre-checking prompts before submission.
     */
    @Operation(
            summary = "Validate a prompt",
            description = "Check a prompt against security policies without forwarding to any LLM. "
                    + "Returns risk assessment, detected entities, and the policy decision.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validation result",
                    content = @Content(schema = @Schema(implementation = ValidationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "SECURITY_ANALYST or ADMIN role required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECURITY_ANALYST')")
    public ResponseEntity<ValidationResponse> validatePrompt(@Valid @RequestBody ValidationRequest request) {
        log.info("POST /api/v1/validate [length={}]", request.getPrompt().length());

        ValidationResponse response = promptSecurityService.validatePrompt(request.getPrompt());

        return ResponseEntity.ok(response);
    }

    /**
     * Custom health check endpoint.
     * Returns service status, version, and uptime.
     */
    @Operation(
            summary = "Health check",
            description = "Returns service status, version, and uptime information."
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy",
            content = @Content(schema = @Schema(implementation = HealthResponse.class)))
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        Duration uptime = Duration.ofMillis(uptimeMs);

        String uptimeFormatted = String.format("%dd %dh %dm %ds",
                uptime.toDaysPart(),
                uptime.toHoursPart(),
                uptime.toMinutesPart(),
                uptime.toSecondsPart());

        HealthResponse response = HealthResponse.builder()
                .status("UP")
                .service("gateway-service")
                .version("1.0.0")
                .uptime(uptimeFormatted)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
