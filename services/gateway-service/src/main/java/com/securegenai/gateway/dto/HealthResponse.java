package com.securegenai.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for the custom health check endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponse {

    /** Service status: "UP" or "DOWN" */
    private String status;

    /** Service name */
    private String service;

    /** Application version */
    private String version;

    /** Uptime in human-readable format */
    private String uptime;

    /** Current timestamp */
    private Instant timestamp;
}
