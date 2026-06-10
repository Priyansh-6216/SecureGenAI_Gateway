package com.securegenai.gateway.service;

import com.securegenai.gateway.dto.PromptResponse;
import com.securegenai.gateway.dto.ValidationResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PromptSecurityServiceTest {

    private final PromptSecurityService securityService = new PromptSecurityService();

    @Test
    void testProcessPrompt_Safe() {
        String prompt = "Hello, this is a clean prompt with no PII.";
        PromptResponse response = securityService.processPrompt(prompt, "openai", "user123");

        assertNotNull(response);
        assertEquals("ALLOW", response.getAction());
        assertEquals("Rule-000 (Generic Allow)", response.getPolicyTriggered());
        assertEquals(0, response.getRiskScore());
        assertEquals("Safe", response.getSeverity());
        assertEquals(prompt, response.getMaskedPrompt());
        assertTrue(response.getDetectedEntities().isEmpty());
    }

    @Test
    void testProcessPrompt_BlockedBySSN() {
        String prompt = "Please update my account, my SSN is 000-12-3456.";
        PromptResponse response = securityService.processPrompt(prompt, "openai", "user123");

        assertNotNull(response);
        assertEquals("BLOCK", response.getAction());
        assertEquals("Rule-001 (Block SSN)", response.getPolicyTriggered());
        assertEquals("[REQUEST BLOCKED BY GATEWAY SECURITY POLICY]", response.getMaskedPrompt());
        assertTrue(response.getDetectedEntities().contains("SSN (Social Security Number)"));
        assertEquals("Safe", response.getSeverity()); // Weight of SSN is 30, so <= 30 is Safe
    }

    @Test
    void testProcessPrompt_MaskedEmailAndCreditCard() {
        String prompt = "Email is user@example.com and CC is 1234-5678-9012-3456.";
        PromptResponse response = securityService.processPrompt(prompt, "openai", "user123");

        assertNotNull(response);
        assertEquals("ALLOW", response.getAction());
        assertTrue(response.getDetectedEntities().contains("Email Address"));
        assertTrue(response.getDetectedEntities().contains("Credit Card Number"));
        
        // Risk score CC (35) + Email (10) = 45 -> Medium
        assertEquals(45, response.getRiskScore());
        assertEquals("Medium", response.getSeverity());
        
        // Check masking
        assertTrue(response.getMaskedPrompt().contains("u***@example.com"));
        assertTrue(response.getMaskedPrompt().contains("****-****-****-****"));
    }

    @Test
    void testProcessPrompt_MaskedSecrets() {
        String prompt = "Deploying key. secret_key: abc123xyz key and sk-live-987654321012";
        PromptResponse response = securityService.processPrompt(prompt, "openai", "user123");

        assertNotNull(response);
        assertEquals("WARN", response.getAction()); // Warn on API Keys
        assertEquals("Rule-002 (Mask API Key)", response.getPolicyTriggered());
        assertTrue(response.getDetectedEntities().contains("API Credentials / Secrets"));
        assertEquals(50, response.getRiskScore());
        assertEquals("Medium", response.getSeverity());
        assertTrue(response.getMaskedPrompt().contains("secret_key: ****************"));
        assertTrue(response.getMaskedPrompt().contains("sk-live-****************"));
    }

    @Test
    void testProcessPrompt_BlockHighRisk() {
        // SSN (30) + Secret (50) + Credit Card (35) = 115 -> clamped to 100
        String prompt = "SSN: 000-12-3456, Key: sk-live-1234, CC: 1111-2222-3333-4444";
        PromptResponse response = securityService.processPrompt(prompt, "openai", "user123");

        assertNotNull(response);
        assertEquals("BLOCK", response.getAction());
        assertEquals("Rule-003 (Deny High Risk)", response.getPolicyTriggered());
        assertEquals(100, response.getRiskScore());
        assertEquals("Critical", response.getSeverity());
    }

    @Test
    void testProcessPrompt_SourceCodeWarning() {
        String prompt = "public class Hello { public static void main(String[] args) { System.out.println(\"Hi\"); } }";
        PromptResponse response = securityService.processPrompt(prompt, "openai", "user123");

        assertNotNull(response);
        assertEquals("ALLOW", response.getAction());
        assertTrue(response.getDetectedEntities().contains("Source Code Block"));
        assertEquals(25, response.getRiskScore());
        assertEquals("Safe", response.getSeverity());
    }

    @Test
    void testValidatePrompt_Safe() {
        String prompt = "Safe query.";
        ValidationResponse response = securityService.validatePrompt(prompt);

        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals("ALLOW", response.getPolicyAction());
        assertEquals(0, response.getRiskScore());
    }

    @Test
    void testValidatePrompt_Blocked() {
        String prompt = "My SSN is 000-12-3456.";
        ValidationResponse response = securityService.validatePrompt(prompt);

        assertNotNull(response);
        assertFalse(response.isValid());
        assertEquals("BLOCK", response.getPolicyAction());
        assertEquals("Rule-001 (Block SSN)", response.getPolicyTriggered());
    }
}
