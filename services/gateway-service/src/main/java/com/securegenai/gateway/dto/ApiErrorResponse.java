package com.securegenai.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standardized error response DTO.
 * All exceptions return this format for consistent client error handling.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    /** Timestamp of the error */
    private Instant timestamp;

    /** HTTP status code */
    private int status;

    /** HTTP error name (e.g., "Bad Request") */
    private String error;

    /** Detailed error message */
    private String message;

    /** Request path that caused the error */
    private String path;

    /** Unique trace ID for debugging */
    private String traceId;
}
