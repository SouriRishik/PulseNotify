CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    recipient_address VARCHAR(255) NOT NULL,
    template_id UUID REFERENCES notification_templates(id) ON DELETE SET NULL,
    channel_id INT REFERENCES notification_channels(id) ON DELETE RESTRICT,
    
    status VARCHAR(50) DEFAULT 'PENDING',
    priority VARCHAR(50) DEFAULT 'NORMAL',
    
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    correlation_id VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    
    is_scheduled BOOLEAN DEFAULT FALSE,
    scheduled_at TIMESTAMP WITH TIME ZONE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
