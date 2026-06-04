package com.pulsenotify.modules.notification.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class NotificationSubmissionResponse {
    private UUID notificationId;
    private String correlationId;
    private String status;
    private String message;
}
