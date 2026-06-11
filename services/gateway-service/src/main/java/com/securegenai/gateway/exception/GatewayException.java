package com.securegenai.gateway.exception;

/**
 * Base exception for all Gateway-specific errors.
 */
public class GatewayException extends RuntimeException {

    public GatewayException(String message) {
        super(message);
    }

    public GatewayException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Thrown when a prompt is blocked by a security policy (results in HTTP 403).
     */
    public static class PromptBlockedException extends GatewayException {
        private final String policyTriggered;

        public PromptBlockedException(String message, String policyTriggered) {
            super(message);
            this.policyTriggered = policyTriggered;
        }

        public String getPolicyTriggered() {
            return policyTriggered;
        }
    }

    /**
     * Thrown when a request is invalid beyond what Bean Validation catches (results in HTTP 400).
     */
    public static class InvalidRequestException extends GatewayException {
        public InvalidRequestException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when authentication fails — invalid credentials, missing or expired token (HTTP 401).
     */
    public static class UnauthorizedException extends GatewayException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when the authenticated user lacks the required role/permission (HTTP 403).
     */
    public static class ForbiddenException extends GatewayException {
        public ForbiddenException(String message) {
            super(message);
        }
    }
}
