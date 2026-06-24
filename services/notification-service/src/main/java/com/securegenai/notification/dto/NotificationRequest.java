package com.securegenai.notification.dto;

import lombok.Data;
import java.util.List;

@Data
public class NotificationRequest {
    private String triggerType; // e.g., "Critical Risk", "Policy Violations"
    private String message;
    private String severity;
    private String userId;
    private List<String> channels; // ["SLACK", "EMAIL", "TEAMS"]
}
