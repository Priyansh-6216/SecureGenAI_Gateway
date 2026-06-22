package com.securegenai.audit.controller;

import com.securegenai.audit.dto.AuditLogRequest;
import com.securegenai.audit.entity.AuditLog;
import com.securegenai.audit.service.AuditLoggingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLoggingService auditLoggingService;

    @PostMapping("/log")
    public ResponseEntity<AuditLog> logEvent(@RequestBody AuditLogRequest request) {
        AuditLog savedLog = auditLoggingService.logEvent(request);
        return ResponseEntity.ok(savedLog);
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditLoggingService.getAllLogs());
    }
}
