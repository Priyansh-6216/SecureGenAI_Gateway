package com.securegenai.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for submitting a prompt to the gateway.
 * The gateway will scan, mask, score, and evaluate policy before forwarding.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptRequest {

    @NotBlank(message = "Prompt cannot be blank")
    @Size(max = 50000, message = "Prompt cannot exceed 50,000 characters")
    private String prompt;

    /**
     * Target LLM provider (e.g., "openai", "bedrock", "claude", "gemini").
     * Defaults to "openai" if not specified.
     */
    @Builder.Default
    private String provider = "openai";

    /**
     * Optional user identifier for audit logging.
     */
    private String userId;
}
