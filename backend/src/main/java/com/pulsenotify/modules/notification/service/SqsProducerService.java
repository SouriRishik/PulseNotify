package com.pulsenotify.modules.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsenotify.modules.notification.entity.Notification;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsProducerService {

    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.sqs.queue-name}")
    private String queueName;

    public void publishNotification(Notification notification) {
        try {
            Map<String, Object> messagePayload = new HashMap<>();
            messagePayload.put("notificationId", notification.getId().toString());
            messagePayload.put("recipient", notification.getRecipientAddress());
            messagePayload.put("channel", notification.getChannel().getChannelName());
            messagePayload.put("correlationId", notification.getCorrelationId());
            messagePayload.put("templateSubject", notification.getTemplate().getSubject());
            messagePayload.put("templateBody", notification.getTemplate().getBody());
            messagePayload.put("dataPayload", notification.getPayload());

            String jsonPayload = objectMapper.writeValueAsString(messagePayload);

            Message<String> message = MessageBuilder.withPayload(jsonPayload)
                    .setHeader("notificationId", notification.getId().toString())
                    .setHeader("correlationId", notification.getCorrelationId())
                    .build();

            sqsTemplate.send(queueName, message);
            log.info("Published notification {} to SQS queue {}", notification.getId(), queueName);
            
        } catch (Exception e) {
            log.error("Failed to publish notification {} to SQS", notification.getId(), e);
            throw new RuntimeException("SQS Publish Failed", e);
        }
    }
}
