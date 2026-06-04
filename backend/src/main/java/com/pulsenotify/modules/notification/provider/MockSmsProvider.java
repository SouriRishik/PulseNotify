package com.pulsenotify.modules.notification.provider;

import com.pulsenotify.modules.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@Slf4j
public class MockSmsProvider implements NotificationProvider {

    @Override
    public boolean supports(String channelName) {
        return "SMS".equalsIgnoreCase(channelName);
    }

    @Override
    public String send(Notification notification, String compiledSubject, String compiledBody) {
        log.info("Simulating sending SMS to {} with body '{}'", notification.getRecipientAddress(), compiledBody);
        try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return "SMS_MSG_ID_" + UUID.randomUUID().toString();
    }
}
