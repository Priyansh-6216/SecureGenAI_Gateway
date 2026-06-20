package com.securegenai.llmadapter.controller;

import com.securegenai.llmadapter.dto.LlmRequest;
import com.securegenai.llmadapter.dto.LlmResponse;
import com.securegenai.llmadapter.service.LlmProviderAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/llm")
@RequiredArgsConstructor
public class LlmController {

    private final List<LlmProviderAdapter> adapters;

    @PostMapping("/generate")
    public ResponseEntity<LlmResponse> generate(@RequestBody LlmRequest request) {
        LlmProviderAdapter adapter = adapters.stream()
                .filter(a -> a.supports(request.getProvider()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported provider: " + request.getProvider()));

        LlmResponse response = adapter.generate(request);
        return ResponseEntity.ok(response);
    }
}
