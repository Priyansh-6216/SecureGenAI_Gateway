package com.securegenai.risk.service;

import com.securegenai.risk.entity.RiskAssessment;
import com.securegenai.risk.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskCalculationEngine {

    private final RiskAssessmentRepository repository;

    public RiskAssessment calculateRisk(List<String> findings) {
        int score = 0;

        if (findings != null) {
            for (String finding : findings) {
                switch (finding.toUpperCase()) {
                    case "PII":
                    case "EMAIL":
                    case "SSN":
                    case "CREDIT_CARD":
                        score += 30;
                        break;
                    case "SECRET":
                    case "API_KEY":
                    case "CREDENTIALS":
                        score += 50;
                        break;
                    case "SOURCE_CODE":
                        score += 25;
                        break;
                    case "COMPLIANCE_VIOLATION":
                        score += 40;
                        break;
                    default:
                        // other generic findings could add a baseline score
                        score += 10;
                }
            }
        }

        // Cap at 100
        score = Math.min(score, 100);

        String severity;
        if (score <= 30) {
            severity = "LOW";
        } else if (score <= 60) {
            severity = "MEDIUM";
        } else if (score <= 80) {
            severity = "HIGH";
        } else {
            severity = "CRITICAL";
        }

        RiskAssessment assessment = new RiskAssessment();
        assessment.setRiskScore(score);
        assessment.setSeverity(severity);
        assessment.setCreatedAt(LocalDateTime.now());

        return repository.save(assessment);
    }
}
