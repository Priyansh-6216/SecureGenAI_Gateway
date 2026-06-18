package com.securegenai.policy.controller;
import com.securegenai.policy.dto.PolicyRequest;
import com.securegenai.policy.dto.PolicyDecision;
import com.securegenai.policy.entity.Policy;
import com.securegenai.policy.repository.PolicyRepository;
import com.securegenai.policy.service.PolicyEvaluationEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/policy")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyEvaluationEngine policyEvaluationEngine;
    private final PolicyRepository policyRepository;

    @PostMapping("/evaluate")
    public ResponseEntity<PolicyDecision> evaluatePolicy(@RequestBody PolicyRequest request) {
        PolicyDecision decision = policyEvaluationEngine.evaluate(request.getDetectedEntities());
        return ResponseEntity.ok(decision);
    }

    @GetMapping
    public ResponseEntity<List<Policy>> getAllPolicies() {
        return ResponseEntity.ok(policyRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Policy> createPolicy(@RequestBody Policy policy) {
        return ResponseEntity.ok(policyRepository.save(policy));
    }
}
