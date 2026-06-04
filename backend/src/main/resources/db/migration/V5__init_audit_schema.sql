CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    action VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    performed_by UUID REFERENCES users(id) ON DELETE SET NULL,
    details JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
