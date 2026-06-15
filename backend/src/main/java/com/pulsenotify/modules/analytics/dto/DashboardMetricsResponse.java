package com.pulsenotify.modules.analytics.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class DashboardMetricsResponse {
    private long totalNotifications;
    private Map<String, Long> statusBreakdown;
    private Map<String, Long> channelBreakdown;
    private double successRate; // percentage
    private double openRate; // percentage
}
