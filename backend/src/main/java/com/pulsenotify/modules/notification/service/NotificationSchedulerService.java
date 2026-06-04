package com.pulsenotify.modules.notification.service;

import com.pulsenotify.modules.notification.entity.Notification;
import com.pulsenotify.modules.notification.entity.NotificationStatus;
import com.pulsenotify.modules.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSchedulerService {

    private final NotificationRepository notificationRepository;
    private final SqsProducerService sqsProducerService;

    // Run every minute (cron = "0 * * * * *")
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void processScheduledNotifications() {
        log.info("Polling for due scheduled notifications...");
        
        List<Notification> dueNotifications = notificationRepository.findDueScheduledNotifications(
                Instant.now(), 
                NotificationStatus.PENDING
        );
        
        if (dueNotifications.isEmpty()) {
            return;
        }
        
        log.info("Found {} scheduled notifications due for processing", dueNotifications.size());
        
        for (Notification notification : dueNotifications) {
            try {
                sqsProducerService.publishNotification(notification);
                notification.setStatus(NotificationStatus.QUEUED);
                notificationRepository.save(notification);
                log.info("Successfully queued scheduled notification {}", notification.getId());
            } catch (Exception e) {
                log.error("Failed to queue scheduled notification {}", notification.getId(), e);
                // Optionally handle retries or mark as FAILED
            }
        }
    }
}
