package com.securegenai.gateway.exception;

import com.securegenai.gateway.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for the Gateway Service.
 * Converts all exceptions into a consistent {@link ApiErrorResponse} JSON format.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles Bean Validation errors (e.g., @NotBlank, @Size failures).
     * Returns HTTP 400.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        String traceId = UUID.randomUUID().toString();
        log.warn("Validation error [traceId={}] [path={}] {}", traceId, request.getRequestURI(), message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Bad Request")
                        .message(message)
                        .path(request.getRequestURI())
                        .traceId(traceId)
                        .build());
    }

    /**
     * Handles prompts blocked by security policy.
     * Returns HTTP 403.
     */
    @ExceptionHandler(GatewayException.PromptBlockedException.class)
    public ResponseEntity<ApiErrorResponse> handlePromptBlocked(
            GatewayException.PromptBlockedException ex, HttpServletRequest request) {

        String traceId = UUID.randomUUID().toString();
        log.warn("Prompt blocked [traceId={}] [policy={}] {}", traceId, ex.getPolicyTriggered(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.FORBIDDEN.value())
                        .error("Forbidden")
                        .message(ex.getMessage() + " [Policy: " + ex.getPolicyTriggered() + "]")
                        .path(request.getRequestURI())
                        .traceId(traceId)
                        .build());
    }

    /**
     * Handles invalid request data beyond validation annotations.
     * Returns HTTP 400.
     */
    @ExceptionHandler(GatewayException.InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(
            GatewayException.InvalidRequestException ex, HttpServletRequest request) {

        String traceId = UUID.randomUUID().toString();
        log.warn("Invalid request [traceId={}] {}", traceId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Bad Request")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .traceId(traceId)
                        .build());
    }

    /**
     * Handles authentication failures — invalid credentials, missing or expired JWT.
     * Returns HTTP 401.
     */
    @ExceptionHandler(GatewayException.UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            GatewayException.UnauthorizedException ex, HttpServletRequest request) {

        String traceId = UUID.randomUUID().toString();
        log.warn("Unauthorized [traceId={}] [path={}] {}", traceId, request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .error("Unauthorized")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .traceId(traceId)
                        .build());
    }

    /**
     * Handles Spring Security AuthenticationException (e.g., bad credentials from auth manager).
     * Returns HTTP 401.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        String traceId = UUID.randomUUID().toString();
        log.warn("Authentication failed [traceId={}] [path={}] {}", traceId, request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .error("Unauthorized")
                        .message("Authentication failed: " + ex.getMessage())
                        .path(request.getRequestURI())
                        .traceId(traceId)
                        .build());
    }

    /**
     * Handles insufficient permissions — authenticated user lacks required role.
     * Returns HTTP 403.
     */
    @ExceptionHandler({GatewayException.ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ApiErrorResponse> handleForbidden(
            RuntimeException ex, HttpServletRequest request) {

        String traceId = UUID.randomUUID().toString();
        log.warn("Access denied [traceId={}] [path={}] {}", traceId, request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.FORBIDDEN.value())
                        .error("Forbidden")
                        .message("Access denied: insufficient permissions to access this resource")
                        .path(request.getRequestURI())
                        .traceId(traceId)
                        .build());
    }

    /**
     * Catch-all for any unexpected exceptions.
     * Returns HTTP 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        String traceId = UUID.randomUUID().toString();
        log.error("Unexpected error [traceId={}] [path={}]", traceId, request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("Internal Server Error")
                        .message("An unexpected error occurred. Please contact support with traceId: " + traceId)
                        .path(request.getRequestURI())
                        .traceId(traceId)
                        .build());
    }
}
