package com.securegenai.notification.service.impl;

import com.securegenai.notification.dto.NotificationRequest;
import com.securegenai.notification.service.NotificationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailNotificationStrategy implements NotificationStrategy {

    @Override
    public boolean supports(String channel) {
        return "EMAIL".equalsIgnoreCase(channel);
    }

    @Override
    public void send(NotificationRequest request) {
        // In a real app, use JavaMailSender
        log.info("Sending EMAIL alert: [{}] {}", request.getSeverity(), request.getMessage());
    }
}
