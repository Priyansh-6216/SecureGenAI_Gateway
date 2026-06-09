package com.securegenai.gateway.service;

import com.securegenai.gateway.dto.PromptResponse;
import com.securegenai.gateway.dto.ValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Core security service for the Gateway.
 *
 * Performs:
 * 1. PII Detection   — SSN, email, API keys/secrets, source code blocks
 * 2. Data Masking     — Replaces detected PII with masked equivalents
 * 3. Risk Scoring     — Calculates a 0–100 threat score based on detected entities
 * 4. Policy Evaluation — Applies security rules (Rule-001 through Rule-004)
 *
 * This is the server-side version of the logic that was previously
 * simulated client-side in the React frontend.
 */
@Slf4j
@Service
public class PromptSecurityService {

    // ─── Regex Patterns ──────────────────────────────────────────────
    private static final Pattern SSN_PATTERN =
            Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("\\b[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}\\b");

    private static final Pattern SECRET_PATTERN =
            Pattern.compile("(?:secret|key|api|token|password|sk-[a-zA-Z0-9]{12,})", Pattern.CASE_INSENSITIVE);

    private static final Pattern SK_LIVE_PATTERN =
            Pattern.compile("(sk-live-[a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern SECRET_KEY_VALUE_PATTERN =
            Pattern.compile("(secret_key:\\s*[a-zA-Z0-9_\\-]+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern CREDIT_CARD_PATTERN =
            Pattern.compile("\\b(?:\\d{4}[\\s-]?){3}\\d{4}\\b");

    private static final Pattern SOURCE_CODE_PATTERN =
            Pattern.compile("(?:function|import|class|const\\s+\\w+\\s*=\\s*|def\\s+|public\\s+|private\\s+|protected\\s+)");

    // ─── Risk Score Weights ──────────────────────────────────────────
    private static final int RISK_SSN = 30;
    private static final int RISK_SECRET = 50;
    private static final int RISK_SOURCE_CODE = 25;
    private static final int RISK_EMAIL = 10;
    private static final int RISK_CREDIT_CARD = 35;

    /**
     * Process a prompt through the full security pipeline.
     * Returns a complete PromptResponse with risk score, policy decision, and masked prompt.
     */
    public PromptResponse processPrompt(String prompt, String provider, String userId) {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        log.info("Processing prompt [requestId={}] [provider={}] [userId={}] [length={}]",
                requestId, provider, userId, prompt.length());

        // 1. Detect PII entities
        List<String> detectedEntities = detectEntities(prompt);

        // 2. Mask the prompt
        String maskedPrompt = maskPrompt(prompt, detectedEntities);

        // 3. Calculate risk score
        int riskScore = calculateRiskScore(prompt, detectedEntities);

        // 4. Determine severity
        String severity = determineSeverity(riskScore);

        // 5. Evaluate policy
        PolicyDecision policy = evaluatePolicy(prompt, riskScore, detectedEntities);

        long processingTimeMs = System.currentTimeMillis() - startTime;

        log.info("Prompt processed [requestId={}] [risk={}] [severity={}] [action={}] [policy={}] [time={}ms]",
                requestId, riskScore, severity, policy.action, policy.policyTriggered, processingTimeMs);

        return PromptResponse.builder()
                .requestId(requestId)
                .originalPrompt(prompt)
                .maskedPrompt(policy.action.equals("BLOCK") ? "[REQUEST BLOCKED BY GATEWAY SECURITY POLICY]" : maskedPrompt)
                .riskScore(riskScore)
                .severity(severity)
                .action(policy.action)
                .policyTriggered(policy.policyTriggered)
                .reason(policy.reason)
                .detectedEntities(detectedEntities)
                .processingTimeMs(processingTimeMs)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Validate a prompt without full processing.
     * Returns a lightweight validation result.
     */
    public ValidationResponse validatePrompt(String prompt) {
        log.info("Validating prompt [length={}]", prompt.length());

        List<String> detectedEntities = detectEntities(prompt);
        int riskScore = calculateRiskScore(prompt, detectedEntities);
        String severity = determineSeverity(riskScore);
        PolicyDecision policy = evaluatePolicy(prompt, riskScore, detectedEntities);

        boolean isValid = policy.action.equals("ALLOW");

        log.info("Validation complete [valid={}] [risk={}] [action={}]",
                isValid, riskScore, policy.action);

        return ValidationResponse.builder()
                .valid(isValid)
                .riskScore(riskScore)
                .severity(severity)
                .detectedEntities(detectedEntities)
                .policyAction(policy.action)
                .policyTriggered(policy.policyTriggered)
                .reason(policy.reason)
                .build();
    }

    // ─── Private Helpers ─────────────────────────────────────────────

    private List<String> detectEntities(String prompt) {
        List<String> entities = new ArrayList<>();

        if (SSN_PATTERN.matcher(prompt).find()) {
            entities.add("SSN (Social Security Number)");
        }
        if (EMAIL_PATTERN.matcher(prompt).find()) {
            entities.add("Email Address");
        }
        if (SECRET_PATTERN.matcher(prompt).find()) {
            entities.add("API Credentials / Secrets");
        }
        if (CREDIT_CARD_PATTERN.matcher(prompt).find()) {
            entities.add("Credit Card Number");
        }

        int lineCount = prompt.split("\n").length;
        if (lineCount > 10 || SOURCE_CODE_PATTERN.matcher(prompt).find()) {
            entities.add("Source Code Block");
        }

        return entities;
    }

    private String maskPrompt(String prompt, List<String> entities) {
        String masked = prompt;

        // Mask SSNs
        if (entities.contains("SSN (Social Security Number)")) {
            masked = SSN_PATTERN.matcher(masked).replaceAll("***-**-****");
        }

        // Mask emails (keep first char + domain)
        if (entities.contains("Email Address")) {
            Matcher emailMatcher = EMAIL_PATTERN.matcher(masked);
            StringBuilder sb = new StringBuilder();
            while (emailMatcher.find()) {
                String localPart = emailMatcher.group().split("@")[0];
                String domain = emailMatcher.group().split("@")[1];
                String replacement = localPart.charAt(0) + "***@" + domain;
                emailMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            emailMatcher.appendTail(sb);
            masked = sb.toString();
        }

        // Mask API secrets
        if (entities.contains("API Credentials / Secrets")) {
            masked = SK_LIVE_PATTERN.matcher(masked).replaceAll("sk-live-****************");
            masked = SECRET_KEY_VALUE_PATTERN.matcher(masked).replaceAll("secret_key: ****************");
        }

        // Mask credit cards
        if (entities.contains("Credit Card Number")) {
            masked = CREDIT_CARD_PATTERN.matcher(masked).replaceAll("****-****-****-****");
        }

        return masked;
    }

    private int calculateRiskScore(String prompt, List<String> entities) {
        int score = 0;

        if (entities.contains("SSN (Social Security Number)")) score += RISK_SSN;
        if (entities.contains("API Credentials / Secrets")) score += RISK_SECRET;
        if (entities.contains("Source Code Block")) score += RISK_SOURCE_CODE;
        if (entities.contains("Email Address")) score += RISK_EMAIL;
        if (entities.contains("Credit Card Number")) score += RISK_CREDIT_CARD;

        return Math.min(score, 100);
    }

    private String determineSeverity(int riskScore) {
        if (riskScore > 80) return "Critical";
        if (riskScore > 60) return "High";
        if (riskScore > 30) return "Medium";
        return "Safe";
    }

    private PolicyDecision evaluatePolicy(String prompt, int riskScore, List<String> entities) {
        // Rule-001: Block SSN
        if (entities.contains("SSN (Social Security Number)")) {
            return new PolicyDecision("BLOCK", "Rule-001 (Block SSN)",
                    "Prompt blocked: SSN detected in user input.");
        }

        // Rule-003: Deny high risk
        if (riskScore > 90) {
            return new PolicyDecision("BLOCK", "Rule-003 (Deny High Risk)",
                    "Prompt blocked: Risk Score (" + riskScore + ") exceeds threshold of 90.");
        }

        // Rule-002: Warn and mask API keys
        if (entities.contains("API Credentials / Secrets")) {
            return new PolicyDecision("WARN", "Rule-002 (Mask API Key)",
                    "API credentials detected and masked automatically before forwarding.");
        }

        // Rule-004: Source code review
        int lineCount = prompt.split("\n").length;
        if (lineCount > 200) {
            return new PolicyDecision("WARN", "Rule-004 (Source Code Review)",
                    "Source code block > 200 lines requires manual security approval.");
        }

        return new PolicyDecision("ALLOW", "Rule-000 (Generic Allow)",
                "All security checks passed. Prompt is safe.");
    }

    /**
     * Internal record for policy evaluation results.
     */
    private record PolicyDecision(String action, String policyTriggered, String reason) {
    }
}
