package com.securegenai.audit.dto;

import lombok.Data;

@Data
public class AuditLogRequest {
    private String userId;
    private String prompt;
    private String response;
    private Integer riskScore;
}
