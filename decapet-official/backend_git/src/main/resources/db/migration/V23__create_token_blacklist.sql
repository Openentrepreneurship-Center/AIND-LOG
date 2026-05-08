CREATE TABLE token_blacklist (
    id VARCHAR(26) PRIMARY KEY,
    jti VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_token_blacklist_jti ON token_blacklist (jti);
CREATE INDEX idx_token_blacklist_expires_at ON token_blacklist (expires_at);
