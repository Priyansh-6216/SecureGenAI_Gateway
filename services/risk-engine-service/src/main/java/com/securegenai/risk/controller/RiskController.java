package com.securegenai.risk.controller;

import com.securegenai.risk.dto.RiskRequest;
import com.securegenai.risk.dto.RiskResponse;
import com.securegenai.risk.entity.RiskAssessment;
import com.securegenai.risk.service.RiskCalculationEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/risk")
@RequiredArgsConstructor
public class RiskController {

    private final RiskCalculationEngine riskCalculationEngine;

    @PostMapping("/calculate")
    public ResponseEntity<RiskResponse> calculateRisk(@RequestBody RiskRequest request) {
        RiskAssessment assessment = riskCalculationEngine.calculateRisk(request.getFindings());
        RiskResponse response = new RiskResponse(
                assessment.getId(),
                assessment.getRiskScore(),
                assessment.getSeverity()
        );
        return ResponseEntity.ok(response);
    }
}
