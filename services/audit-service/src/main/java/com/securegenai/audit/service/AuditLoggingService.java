package com.securegenai.audit.service;

import com.securegenai.audit.dto.AuditLogRequest;
import com.securegenai.audit.entity.AuditLog;
import com.securegenai.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLoggingService {

    private final AuditLogRepository auditLogRepository;

    public AuditLog logEvent(AuditLogRequest request) {
        AuditLog auditLog = AuditLog.builder()
                .userId(request.getUserId())
                .prompt(request.getPrompt())
                .response(request.getResponse())
                .riskScore(request.getRiskScore())
                .timestamp(LocalDateTime.now())
                .build();

        return auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }
}
