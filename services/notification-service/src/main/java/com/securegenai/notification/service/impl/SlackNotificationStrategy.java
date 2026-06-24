package com.securegenai.notification.service.impl;

import com.securegenai.notification.dto.NotificationRequest;
import com.securegenai.notification.service.NotificationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SlackNotificationStrategy implements NotificationStrategy {

    @Value("${notifications.slack.webhook-url}")
    private String webhookUrl;

    @Override
    public boolean supports(String channel) {
        return "SLACK".equalsIgnoreCase(channel);
    }

    @Override
    public void send(NotificationRequest request) {
        // In a real app, use RestTemplate to POST to webhookUrl
        log.info("Sending SLACK alert to webhook {}: [{}] {}", webhookUrl, request.getSeverity(), request.getMessage());
    }
}
