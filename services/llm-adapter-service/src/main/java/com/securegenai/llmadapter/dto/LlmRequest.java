package com.securegenai.llmadapter.dto;

import lombok.Data;

@Data
public class LlmRequest {
    private String provider; // e.g., "OPENAI"
    private String prompt;
    private String model;
}
