package com.securegenai.risk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskResponse {
    private UUID assessmentId;
    private int riskScore;
    private String severity;
}
