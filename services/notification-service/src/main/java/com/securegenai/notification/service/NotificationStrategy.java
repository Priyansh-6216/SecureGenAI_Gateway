package com.securegenai.notification.service;

import com.securegenai.notification.dto.NotificationRequest;

public interface NotificationStrategy {
    boolean supports(String channel);
    void send(NotificationRequest request);
}
