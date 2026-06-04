package com.pulsenotify.modules.notification.provider;

import com.pulsenotify.modules.notification.entity.Notification;

public interface NotificationProvider {
    boolean supports(String channelName);
    String send(Notification notification, String compiledSubject, String compiledBody);
}
