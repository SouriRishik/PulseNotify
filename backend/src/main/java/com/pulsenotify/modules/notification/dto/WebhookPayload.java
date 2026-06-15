package com.pulsenotify.modules.notification.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class WebhookPayload {
    private UUID notificationId;
    private String eventType; // e.g. DELIVERED, OPENED, CLICKED, BOUNCED
    private String providerReference;
}
