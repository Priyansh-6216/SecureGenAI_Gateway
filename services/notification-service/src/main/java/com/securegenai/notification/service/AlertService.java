package com.securegenai.notification.service;

import com.securegenai.notification.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final List<NotificationStrategy> strategies;

    public void processAlert(NotificationRequest request) {
        if (request.getChannels() == null || request.getChannels().isEmpty()) {
            log.warn("No channels specified for alert: {}", request.getMessage());
            return;
        }

        for (String channel : request.getChannels()) {
            strategies.stream()
                    .filter(strategy -> strategy.supports(channel))
                    .findFirst()
                    .ifPresentOrElse(
                            strategy -> strategy.send(request),
                            () -> log.warn("No notification strategy found for channel: {}", channel)
                    );
        }
    }
}
