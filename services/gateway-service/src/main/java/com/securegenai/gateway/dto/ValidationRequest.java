package com.securegenai.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for prompt validation only (without LLM forwarding).
 * Returns risk assessment and policy decision without processing the prompt further.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRequest {

    @NotBlank(message = "Prompt cannot be blank")
    @Size(max = 50000, message = "Prompt cannot exceed 50,000 characters")
    private String prompt;
}
