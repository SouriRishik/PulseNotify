package com.pulsenotify.modules.analytics.service;

import com.pulsenotify.modules.analytics.dto.DashboardMetricsResponse;
import com.pulsenotify.modules.auth.entity.User;
import com.pulsenotify.modules.auth.repository.UserRepository;
import com.pulsenotify.modules.notification.entity.NotificationStatus;
import com.pulsenotify.modules.notification.repository.NotificationRepository;
import com.pulsenotify.modules.notification.repository.DeliveryEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final DeliveryEventRepository deliveryEventRepository;

    public DashboardMetricsResponse getDashboardMetrics(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long total = notificationRepository.countByUser(user);
        
        List<Object[]> statusData = notificationRepository.countNotificationsByStatusForUser(user);
        Map<String, Long> statusBreakdown = new HashMap<>();
        long successfulCount = 0;
        
        for (Object[] row : statusData) {
            NotificationStatus status = (NotificationStatus) row[0];
            long count = (Long) row[1];
            statusBreakdown.put(status.name(), count);
            if (status == NotificationStatus.DELIVERED || status == NotificationStatus.SENT) {
                successfulCount += count;
            }
        }

        List<Object[]> channelData = notificationRepository.countNotificationsByChannelForUser(user);
        Map<String, Long> channelBreakdown = new HashMap<>();
        for (Object[] row : channelData) {
            channelBreakdown.put((String) row[0], (Long) row[1]);
        }

        double successRate = total > 0 ? ((double) successfulCount / total) * 100.0 : 0.0;

        long openedCount = deliveryEventRepository.countByUserAndEventType(user, "OPENED");
        double openRate = successfulCount > 0 ? ((double) openedCount / successfulCount) * 100.0 : 0.0;

        return DashboardMetricsResponse.builder()
                .totalNotifications(total)
                .statusBreakdown(statusBreakdown)
                .channelBreakdown(channelBreakdown)
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .openRate(Math.round(openRate * 100.0) / 100.0)
                .build();
    }
}
