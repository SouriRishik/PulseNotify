CREATE INDEX idx_notifications_pending_scheduled 
ON notifications (scheduled_at) 
WHERE status = 'PENDING' AND is_scheduled = TRUE;

CREATE INDEX idx_notifications_user_created 
ON notifications (user_id, created_at DESC);

CREATE INDEX idx_notifications_correlation_id 
ON notifications (correlation_id);

CREATE INDEX idx_delivery_events_notification_type 
ON delivery_events (notification_id, event_type);
