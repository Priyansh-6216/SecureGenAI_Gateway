package com.securegenai.llmadapter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponse {
    private String content;
    private String modelUsed;
    private String provider;
}
