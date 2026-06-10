package com.securegenai.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securegenai.gateway.dto.*;
import com.securegenai.gateway.exception.GatewayException;
import com.securegenai.gateway.service.PromptSecurityService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GatewayController.class)
class GatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PromptSecurityService promptSecurityService;

    @Test
    void testProcessPrompt_Success() throws Exception {
        PromptRequest request = PromptRequest.builder()
                .prompt("Hello LLM, tell me a joke.")
                .provider("openai")
                .userId("user-01")
                .build();

        PromptResponse response = PromptResponse.builder()
                .requestId("req-123")
                .originalPrompt("Hello LLM, tell me a joke.")
                .maskedPrompt("Hello LLM, tell me a joke.")
                .riskScore(0)
                .severity("Safe")
                .action("ALLOW")
                .policyTriggered("Rule-000")
                .reason("Safe")
                .detectedEntities(List.of())
                .processingTimeMs(15)
                .timestamp(Instant.now())
                .build();

        when(promptSecurityService.processPrompt(anyString(), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("req-123"))
                .andExpect(jsonPath("$.action").value("ALLOW"))
                .andExpect(jsonPath("$.maskedPrompt").value("Hello LLM, tell me a joke."));
    }

    @Test
    void testProcessPrompt_Blocked() throws Exception {
        PromptRequest request = PromptRequest.builder()
                .prompt("Blocked prompt.")
                .provider("openai")
                .userId("user-01")
                .build();

        PromptResponse response = PromptResponse.builder()
                .requestId("req-123")
                .originalPrompt("Blocked prompt.")
                .maskedPrompt("[REQUEST BLOCKED BY GATEWAY SECURITY POLICY]")
                .riskScore(95)
                .severity("Critical")
                .action("BLOCK")
                .policyTriggered("Rule-003")
                .reason("Prompt blocked due to high risk")
                .detectedEntities(List.of("SSN"))
                .processingTimeMs(10)
                .timestamp(Instant.now())
                .build();

        when(promptSecurityService.processPrompt(anyString(), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Prompt blocked due to high risk [Policy: Rule-003]"));
    }

    @Test
    void testProcessPrompt_ValidationFailure() throws Exception {
        // Blank prompt
        PromptRequest request = PromptRequest.builder()
                .prompt(" ")
                .provider("openai")
                .userId("user-01")
                .build();

        mockMvc.perform(post("/api/v1/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("prompt: Prompt cannot be blank"));
    }

    @Test
    void testValidatePrompt_Success() throws Exception {
        ValidationRequest request = ValidationRequest.builder()
                .prompt("Clean prompt")
                .build();

        ValidationResponse response = ValidationResponse.builder()
                .valid(true)
                .riskScore(0)
                .severity("Safe")
                .detectedEntities(List.of())
                .policyAction("ALLOW")
                .policyTriggered("Rule-000")
                .reason("Allowed")
                .build();

        when(promptSecurityService.validatePrompt(anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.policyAction").value("ALLOW"));
    }

    @Test
    void testHealthCheck_Success() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("gateway-service"));
    }
}
