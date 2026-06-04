package com.pulsenotify.modules.notification.service;

import com.pulsenotify.common.entity.AuditLog;
import com.pulsenotify.common.repository.AuditLogRepository;
import com.pulsenotify.modules.auth.entity.User;
import com.pulsenotify.modules.auth.repository.UserRepository;
import com.pulsenotify.modules.notification.dto.NotificationSubmissionResponse;
import com.pulsenotify.modules.notification.dto.SendNotificationRequest;
import com.pulsenotify.modules.notification.entity.Notification;
import com.pulsenotify.modules.notification.entity.NotificationChannel;
import com.pulsenotify.modules.notification.entity.NotificationPriority;
import com.pulsenotify.modules.notification.entity.NotificationStatus;
import com.pulsenotify.modules.notification.repository.NotificationChannelRepository;
import com.pulsenotify.modules.notification.repository.NotificationRepository;
import com.pulsenotify.modules.template.entity.NotificationTemplate;
import com.pulsenotify.modules.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationChannelRepository channelRepository;
    private final TemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final SqsProducerService sqsProducerService;

    @Transactional
    public NotificationSubmissionResponse submitNotification(SendNotificationRequest request, UUID userId) {
        
        // 1. Idempotency Check
        if (notificationRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            throw new RuntimeException("Duplicate processing attempt. Idempotency key already exists.");
        }

        // 2. Fetch Relationships
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        NotificationTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Template not found"));

        if (!template.getIsActive()) {
            throw new RuntimeException("Cannot send notification using an inactive template");
        }

        NotificationChannel channel = channelRepository.findByChannelName(request.getChannelName().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        // 3. Create Notification Entity
        String correlationId = UUID.randomUUID().toString();
        
        Notification notification = Notification.builder()
                .user(user)
                .recipientAddress(request.getRecipientAddress())
                .template(template)
                .channel(channel)
                .status(NotificationStatus.PENDING)
                .priority(request.getPriority() != null ? request.getPriority() : NotificationPriority.NORMAL)
                .idempotencyKey(request.getIdempotencyKey())
                .correlationId(correlationId)
                .payload(request.getPayload())
                .isScheduled(request.getIsScheduled() != null ? request.getIsScheduled() : false)
                .scheduledAt(request.getScheduledAt())
                .build();

        Notification saved = notificationRepository.save(notification);

        // 4. Audit Logging
        AuditLog auditLog = AuditLog.builder()
                .action("SUBMIT_NOTIFICATION")
                .entityId(saved.getId())
                .entityType("NOTIFICATION")
                .performedBy(user)
                .details(Map.of("idempotency_key", request.getIdempotencyKey(), "channel", channel.getChannelName()))
                .build();
        auditLogRepository.save(auditLog);

        if (!notification.getIsScheduled()) {
            sqsProducerService.publishNotification(saved);
            saved.setStatus(NotificationStatus.QUEUED);
            notificationRepository.save(saved);
        }

        return NotificationSubmissionResponse.builder()
                .notificationId(saved.getId())
                .correlationId(correlationId)
                .status(saved.getStatus().name())
                .message(notification.getIsScheduled() ? "Notification scheduled successfully" : "Notification queued successfully")
                .build();
    }
}
