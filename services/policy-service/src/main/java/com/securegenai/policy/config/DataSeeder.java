package com.securegenai.policy.config;

import com.securegenai.policy.entity.Policy;
import com.securegenai.policy.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final PolicyRepository policyRepository;

    @Override
    public void run(String... args) {
        if (policyRepository.count() == 0) {
            policyRepository.saveAll(List.of(
                new Policy(null, "Block SSN", "Blocks any request containing a Social Security Number", "BLOCK", "SSN", LocalDateTime.now()),
                new Policy(null, "Block API Keys", "Blocks any request containing an API Key", "BLOCK", "API_KEY", LocalDateTime.now()),
                new Policy(null, "Warn Source Code", "Warns when source code is detected", "WARN", "SOURCE_CODE", LocalDateTime.now()),
                new Policy(null, "Block Credit Cards", "Blocks any request containing a Credit Card Number", "BLOCK", "CREDIT_CARD", LocalDateTime.now())
            ));
        }
    }
}
