package com.securegenai.llmadapter.service.openai;

import com.securegenai.llmadapter.dto.LlmRequest;
import com.securegenai.llmadapter.dto.LlmResponse;
import com.securegenai.llmadapter.dto.OpenAiRequest;
import com.securegenai.llmadapter.dto.OpenAiResponse;
import com.securegenai.llmadapter.service.LlmProviderAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class OpenAiAdapter implements LlmProviderAdapter {

    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Override
    public boolean supports(String provider) {
        return "OPENAI".equalsIgnoreCase(provider);
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        OpenAiRequest.Message userMessage = new OpenAiRequest.Message("user", request.getPrompt());
        OpenAiRequest openAiRequest = new OpenAiRequest(
                request.getModel() != null ? request.getModel() : "gpt-3.5-turbo",
                Collections.singletonList(userMessage)
        );

        HttpEntity<OpenAiRequest> entity = new HttpEntity<>(openAiRequest, headers);

        OpenAiResponse response = restTemplate.postForObject(apiUrl, entity, OpenAiResponse.class);

        LlmResponse llmResponse = new LlmResponse();
        llmResponse.setProvider("OPENAI");
        if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
            llmResponse.setContent(response.getChoices().get(0).getMessage().getContent());
            llmResponse.setModelUsed(response.getModel());
        }

        return llmResponse;
    }
}
