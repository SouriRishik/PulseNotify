package com.pulsenotify.modules.notification.repository;

import com.pulsenotify.modules.notification.entity.Notification;
import com.pulsenotify.modules.notification.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    boolean existsByIdempotencyKey(String idempotencyKey);
    
    @Query("SELECT n FROM Notification n WHERE n.isScheduled = true AND n.scheduledAt <= :now AND n.status = :status")
    List<Notification> findDueScheduledNotifications(@Param("now") Instant now, @Param("status") NotificationStatus status);
}
