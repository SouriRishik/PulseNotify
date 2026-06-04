package com.pulsenotify.modules.notification.repository;

import com.pulsenotify.modules.notification.entity.Notification;
import com.pulsenotify.modules.notification.entity.RetryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RetryRecordRepository extends JpaRepository<RetryRecord, UUID> {
    Optional<RetryRecord> findByNotification(Notification notification);
}
