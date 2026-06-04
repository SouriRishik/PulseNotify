package com.pulsenotify.modules.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsenotify.modules.notification.entity.*;
import com.pulsenotify.modules.notification.provider.NotificationProvider;
import com.pulsenotify.modules.notification.repository.DeliveryEventRepository;
import com.pulsenotify.modules.notification.repository.NotificationLogRepository;
import com.pulsenotify.modules.notification.repository.NotificationRepository;
import com.pulsenotify.modules.notification.repository.RetryRecordRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsConsumerService {

    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository logRepository;
    private final DeliveryEventRepository deliveryEventRepository;
    private final RetryRecordRepository retryRecordRepository;
    private final List<NotificationProvider> providers;

    private static final int MAX_RETRIES = 3;

    @SqsListener("${cloud.aws.sqs.queue-name}")
    @Transactional
    public void processMessage(Message<String> message) {
        String payload = message.getPayload();
        log.info("Received message from SQS: {}", payload);

        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            UUID notificationId = UUID.fromString(jsonNode.get("notificationId").asText());
            String channel = jsonNode.get("channel").asText();
            String compiledSubject = jsonNode.get("templateSubject").asText();
            String compiledBody = jsonNode.get("templateBody").asText();

            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification not found in DB: " + notificationId));

            // Update state to PROCESSING
            notification.setStatus(NotificationStatus.PROCESSING);
            notificationRepository.save(notification);

            // Find provider
            NotificationProvider provider = providers.stream()
                    .filter(p -> p.supports(channel))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No provider found for channel: " + channel));

            // Send notification
            String providerRef = provider.send(notification, compiledSubject, compiledBody);

            // Update State to SENT
            notification.setStatus(NotificationStatus.SENT);
            notificationRepository.save(notification);

            // Create Log
            logRepository.save(NotificationLog.builder()
                    .notification(notification)
                    .status("SENT")
                    .providerResponse("Provider Ref: " + providerRef)
                    .build());

            // Create Delivery Event
            deliveryEventRepository.save(DeliveryEvent.builder()
                    .notification(notification)
                    .eventType("SENT")
                    .providerReference(providerRef)
                    .build());

        } catch (Exception e) {
            log.error("Error processing SQS message", e);
            handleFailure(payload, e.getMessage());
            // Rethrow to let SQS handle DLQ routing if max retries exceeded on AWS side
            throw new RuntimeException("Failed to process message", e); 
        }
    }

    private void handleFailure(String payload, String errorReason) {
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            if (!jsonNode.has("notificationId")) return;
            
            UUID notificationId = UUID.fromString(jsonNode.get("notificationId").asText());
            Notification notification = notificationRepository.findById(notificationId).orElse(null);
            
            if (notification != null) {
                // Log the failure
                logRepository.save(NotificationLog.builder()
                        .notification(notification)
                        .status("FAILED")
                        .providerResponse(errorReason)
                        .build());

                // Update Retry Record
                RetryRecord retryRecord = retryRecordRepository.findByNotification(notification)
                        .orElse(RetryRecord.builder().notification(notification).retryCount(0).build());
                
                retryRecord.setRetryCount(retryRecord.getRetryCount() + 1);
                retryRecord.setErrorReason(errorReason);
                retryRecord.setLastRetry(Instant.now());
                retryRecordRepository.save(retryRecord);

                if (retryRecord.getRetryCount() >= MAX_RETRIES) {
                    notification.setStatus(NotificationStatus.DLQ);
                } else {
                    notification.setStatus(NotificationStatus.FAILED);
                }
                notificationRepository.save(notification);
            }
        } catch (Exception ex) {
            log.error("Failed to handle failure logic for payload {}", payload, ex);
        }
    }
}
