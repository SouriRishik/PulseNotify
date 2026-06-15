-- Drop old verification tokens table
DROP TABLE IF EXISTS verification_tokens;

-- Remove email verification column from users
ALTER TABLE users DROP COLUMN IF EXISTS is_email_verified;

-- Create pending users table
CREATE TABLE pending_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    roles VARCHAR(255),
    otp_code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
