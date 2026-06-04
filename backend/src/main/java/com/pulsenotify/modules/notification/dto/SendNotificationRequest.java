package com.pulsenotify.modules.notification.dto;

import com.pulsenotify.modules.notification.entity.NotificationPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
public class SendNotificationRequest {
    @NotBlank
    private String recipientAddress;

    @NotNull
    private UUID templateId;

    @NotBlank
    private String channelName; // EMAIL, SMS, PUSH

    @NotBlank
    private String idempotencyKey;

    private NotificationPriority priority;

    @NotNull
    private Map<String, Object> payload;

    private Boolean isScheduled = false;
    
    private Instant scheduledAt;
}
