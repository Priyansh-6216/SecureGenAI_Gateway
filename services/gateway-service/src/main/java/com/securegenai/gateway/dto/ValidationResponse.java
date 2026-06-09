package com.securegenai.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for validation-only requests.
 * Returns the risk assessment and policy decision without including the masked prompt.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponse {

    /** Whether the prompt passes all security checks */
    private boolean valid;

    /** Calculated risk score (0–100) */
    private int riskScore;

    /** Risk severity level: Safe, Medium, High, Critical */
    private String severity;

    /** List of detected sensitive entity types */
    private List<String> detectedEntities;

    /** Policy action: ALLOW, WARN, BLOCK */
    private String policyAction;

    /** The specific policy rule that was triggered */
    private String policyTriggered;

    /** Human-readable explanation */
    private String reason;
}
