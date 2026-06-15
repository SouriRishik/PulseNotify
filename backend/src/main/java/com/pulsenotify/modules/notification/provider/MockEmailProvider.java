package com.pulsenotify.modules.notification.provider;

import com.pulsenotify.modules.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Slf4j
public class MockEmailProvider implements NotificationProvider {

    @Override
    public boolean supports(String channelName) {
        return "EMAIL".equalsIgnoreCase(channelName);
    }

    @Override
    public String send(Notification notification, String compiledSubject, String compiledBody) {
        log.info("Simulating sending EMAIL to {} with subject '{}'", notification.getRecipientAddress(), compiledSubject);
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return "EMAIL_MSG_ID_" + UUID.randomUUID().toString();
    }
}
