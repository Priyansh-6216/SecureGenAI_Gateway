package com.securegenai.llmadapter.service.bedrock;

import com.securegenai.llmadapter.dto.LlmRequest;
import com.securegenai.llmadapter.dto.LlmResponse;
import com.securegenai.llmadapter.service.LlmProviderAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class BedrockAdapter implements LlmProviderAdapter {

    private final BedrockRuntimeClient bedrockRuntimeClient;

    @Override
    public boolean supports(String provider) {
        return "BEDROCK".equalsIgnoreCase(provider);
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        String modelId = request.getModel() != null ? request.getModel() : "anthropic.claude-v2";

        // Simple JSON payload for Claude v2
        String payload = String.format(
                "{\"prompt\":\"\\n\\nHuman: %s\\n\\nAssistant:\",\"max_tokens_to_sample\":300,\"temperature\":0.7}",
                escapeJson(request.getPrompt())
        );

        InvokeModelRequest invokeModelRequest = InvokeModelRequest.builder()
                .modelId(modelId)
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromString(payload, StandardCharsets.UTF_8))
                .build();

        InvokeModelResponse invokeModelResponse = bedrockRuntimeClient.invokeModel(invokeModelRequest);
        String responseBody = invokeModelResponse.body().asUtf8String();

        // Very naive parsing for demo. In real app, use ObjectMapper
        String extractedText = responseBody;
        if (responseBody.contains("\"completion\":\"")) {
            extractedText = responseBody.split("\"completion\":\"")[1].split("\"")[0];
        }

        LlmResponse llmResponse = new LlmResponse();
        llmResponse.setProvider("BEDROCK");
        llmResponse.setModelUsed(modelId);
        llmResponse.setContent(extractedText);

        return llmResponse;
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", "\\n");
    }
}
