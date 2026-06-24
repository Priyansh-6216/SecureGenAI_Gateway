package com.securegenai.notification.controller;

import com.securegenai.notification.dto.NotificationRequest;
import com.securegenai.notification.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final AlertService alertService;

    @PostMapping("/alert")
    public ResponseEntity<String> sendAlert(@RequestBody NotificationRequest request) {
        alertService.processAlert(request);
        return ResponseEntity.ok("Alert dispatched to requested channels");
    }
}
