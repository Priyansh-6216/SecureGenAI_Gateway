package com.securegenai.notification.service.impl;

import com.securegenai.notification.dto.NotificationRequest;
import com.securegenai.notification.service.NotificationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TeamsNotificationStrategy implements NotificationStrategy {

    @Value("${notifications.teams.webhook-url}")
    private String webhookUrl;

    @Override
    public boolean supports(String channel) {
        return "TEAMS".equalsIgnoreCase(channel);
    }

    @Override
    public void send(NotificationRequest request) {
        // In a real app, use RestTemplate to POST to Teams Webhook
        log.info("Sending TEAMS alert to webhook {}: [{}] {}", webhookUrl, request.getSeverity(), request.getMessage());
    }
}
