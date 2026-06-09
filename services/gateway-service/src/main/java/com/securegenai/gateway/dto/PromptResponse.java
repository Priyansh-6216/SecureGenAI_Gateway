package com.securegenai.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for a processed prompt.
 * Contains the full security evaluation result: risk score, policy decision,
 * detected entities, and the masked prompt that would be forwarded to the LLM.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptResponse {

    /** Unique request identifier for audit tracking */
    private String requestId;

    /** The original prompt submitted by the user */
    private String originalPrompt;

    /** The masked/sanitized prompt safe for LLM forwarding */
    private String maskedPrompt;

    /** Calculated risk score (0–100) */
    private int riskScore;

    /** Risk severity level: Safe, Medium, High, Critical */
    private String severity;

    /** Policy action taken: ALLOW, WARN, BLOCK */
    private String action;

    /** The specific policy rule that was triggered */
    private String policyTriggered;

    /** Human-readable explanation of the decision */
    private String reason;

    /** List of detected sensitive entity types (e.g., "SSN", "Email Address") */
    private List<String> detectedEntities;

    /** Processing time in milliseconds */
    private long processingTimeMs;

    /** Timestamp of processing */
    private Instant timestamp;
}
