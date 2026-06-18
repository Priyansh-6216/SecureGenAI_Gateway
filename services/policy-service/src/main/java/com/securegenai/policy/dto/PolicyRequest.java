package com.securegenai.policy.dto;
import lombok.Data;
import java.util.List;

@Data
public class PolicyRequest {
    private List<PiiEntity> detectedEntities;
}
