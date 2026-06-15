package com.pulsenotify.modules.notification.controller;

import com.pulsenotify.modules.notification.dto.WebhookPayload;
import com.pulsenotify.modules.notification.entity.DeliveryEvent;
import com.pulsenotify.modules.notification.entity.Notification;
import com.pulsenotify.modules.notification.entity.NotificationStatus;
import com.pulsenotify.modules.notification.repository.DeliveryEventRepository;
import com.pulsenotify.modules.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final NotificationRepository notificationRepository;
    private final DeliveryEventRepository deliveryEventRepository;

    @PostMapping("/email")
    @Transactional
    public ResponseEntity<String> handleEmailWebhook(@RequestBody WebhookPayload payload) {
        log.info("Received webhook payload: {}", payload);

        Optional<Notification> notificationOpt = notificationRepository.findById(payload.getNotificationId());
        
        if (notificationOpt.isEmpty()) {
            log.warn("Webhook received for unknown notification ID: {}", payload.getNotificationId());
            return ResponseEntity.notFound().build();
        }

        Notification notification = notificationOpt.get();

        DeliveryEvent event = DeliveryEvent.builder()
                .notification(notification)
                .eventType(payload.getEventType().toUpperCase())
                .providerReference(payload.getProviderReference())
                .build();
                
        deliveryEventRepository.save(event);

        if ("DELIVERED".equalsIgnoreCase(payload.getEventType())) {
            notification.setStatus(NotificationStatus.DELIVERED);
        } else if ("BOUNCED".equalsIgnoreCase(payload.getEventType()) || "FAILED".equalsIgnoreCase(payload.getEventType())) {
            notification.setStatus(NotificationStatus.FAILED);
        } else if ("OPENED".equalsIgnoreCase(payload.getEventType())) {
            // Usually we keep status DELIVERED but just log the OPENED event
            notification.setStatus(NotificationStatus.DELIVERED); 
        }

        notificationRepository.save(notification);

        return ResponseEntity.ok("Webhook processed successfully");
    }
}
