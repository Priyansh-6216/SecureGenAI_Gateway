package com.securegenai.risk.dto;

import lombok.Data;
import java.util.List;

@Data
public class RiskRequest {
    private List<String> findings; // e.g., ["SSN", "API_KEY", "SOURCE_CODE", "COMPLIANCE_VIOLATION"]
}
