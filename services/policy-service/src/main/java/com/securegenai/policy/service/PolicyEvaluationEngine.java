package com.securegenai.policy.service;
import com.securegenai.policy.dto.PiiEntity;
import com.securegenai.policy.dto.PolicyDecision;
import com.securegenai.policy.entity.Policy;
import com.securegenai.policy.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyEvaluationEngine {

    private final PolicyRepository policyRepository;

    public PolicyDecision evaluate(List<PiiEntity> detectedEntities) {
        if (detectedEntities == null || detectedEntities.isEmpty()) {
            return new PolicyDecision("ALLOW", "No sensitive data detected.");
        }

        // Extract unique rule types from detected entities
        List<String> detectedTypes = detectedEntities.stream()
                .map(PiiEntity::getType)
                .distinct()
                .collect(Collectors.toList());

        // Fetch applicable policies
        List<Policy> applicablePolicies = policyRepository.findByRuleTypeIn(detectedTypes);

        if (applicablePolicies.isEmpty()) {
            return new PolicyDecision("ALLOW", "No matching policies found for detected data.");
        }

        // Determine highest severity action (BLOCK > WARN > ALLOW)
        boolean shouldBlock = applicablePolicies.stream().anyMatch(p -> "BLOCK".equalsIgnoreCase(p.getAction()));
        boolean shouldWarn = applicablePolicies.stream().anyMatch(p -> "WARN".equalsIgnoreCase(p.getAction()));

        if (shouldBlock) {
            String reason = "Blocked by policy for containing: " + getViolatingTypes(applicablePolicies, "BLOCK");
            return new PolicyDecision("BLOCK", reason);
        }

        if (shouldWarn) {
            String reason = "Warning policy triggered for containing: " + getViolatingTypes(applicablePolicies, "WARN");
            return new PolicyDecision("WARN", reason);
        }

        return new PolicyDecision("ALLOW", "Passed all policy checks.");
    }

    private String getViolatingTypes(List<Policy> policies, String action) {
        return policies.stream()
                .filter(p -> action.equalsIgnoreCase(p.getAction()))
                .map(Policy::getRuleType)
                .collect(Collectors.joining(", "));
    }
}
