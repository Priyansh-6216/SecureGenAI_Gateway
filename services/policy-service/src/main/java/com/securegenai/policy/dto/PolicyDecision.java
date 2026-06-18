package com.securegenai.policy.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PolicyDecision {
    private String action; // ALLOW, WARN, BLOCK
    private String reason;
}
