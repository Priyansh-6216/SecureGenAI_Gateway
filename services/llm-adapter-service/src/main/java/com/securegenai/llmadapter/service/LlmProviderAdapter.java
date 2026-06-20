package com.securegenai.llmadapter.service;

import com.securegenai.llmadapter.dto.LlmRequest;
import com.securegenai.llmadapter.dto.LlmResponse;

public interface LlmProviderAdapter {
    boolean supports(String provider);
    LlmResponse generate(LlmRequest request);
}
