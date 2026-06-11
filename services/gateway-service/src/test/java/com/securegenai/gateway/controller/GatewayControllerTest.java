package com.securegenai.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securegenai.gateway.dto.*;
import com.securegenai.gateway.security.JwtAuthenticationFilter;
import com.securegenai.gateway.security.JwtService;
import com.securegenai.gateway.service.PromptSecurityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice tests for {@link GatewayController}.
 *
 * Uses {@code @WebMvcTest} (controller slice) with mocked services.
 * {@code @WithMockUser} injects a mock authenticated principal so Spring
 * Security's filter chain allows requests to reach the controller under test.
 */
@WebMvcTest(GatewayController.class)
@DisplayName("GatewayController Tests")
class GatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PromptSecurityService promptSecurityService;

    // These are mocked to satisfy the @WebMvcTest slice with Spring Security
    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    // ─── POST /api/v1/prompts ─────────────────────────────────────────────────

    @Test
    @DisplayName("Process prompt returns 200 for authorized SECURITY_ANALYST user")
    @WithMockUser(username = "analyst", roles = "SECURITY_ANALYST")
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
    @DisplayName("Blocked prompt returns 403 with policy info")
    @WithMockUser(username = "analyst", roles = "SECURITY_ANALYST")
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
    @DisplayName("Blank prompt returns 400 validation error")
    @WithMockUser(username = "analyst", roles = "SECURITY_ANALYST")
    void testProcessPrompt_ValidationFailure() throws Exception {
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
    @DisplayName("Process prompt without JWT returns 401")
    void testProcessPrompt_NoAuth_Returns401() throws Exception {
        PromptRequest request = PromptRequest.builder()
                .prompt("Hello")
                .provider("openai")
                .build();

        mockMvc.perform(post("/api/v1/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ─── POST /api/v1/validate ────────────────────────────────────────────────

    @Test
    @DisplayName("Validate prompt returns 200 for SECURITY_ANALYST")
    @WithMockUser(username = "analyst", roles = "SECURITY_ANALYST")
    void testValidatePrompt_Success() throws Exception {
        com.securegenai.gateway.dto.ValidationRequest request =
                com.securegenai.gateway.dto.ValidationRequest.builder()
                        .prompt("Clean prompt")
                        .build();

        com.securegenai.gateway.dto.ValidationResponse response =
                com.securegenai.gateway.dto.ValidationResponse.builder()
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
    @DisplayName("Validate prompt is forbidden for EMPLOYEE role")
    @WithMockUser(username = "employee", roles = "EMPLOYEE")
    void testValidatePrompt_ForbiddenForEmployee() throws Exception {
        com.securegenai.gateway.dto.ValidationRequest request =
                com.securegenai.gateway.dto.ValidationRequest.builder()
                        .prompt("Test prompt")
                        .build();

        mockMvc.perform(post("/api/v1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ─── GET /api/v1/health ───────────────────────────────────────────────────

    @Test
    @DisplayName("Health check is publicly accessible (no auth required)")
    void testHealthCheck_IsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("gateway-service"));
    }
}
