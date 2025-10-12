-- V3 Password Reset Token Table
CREATE TABLE IF NOT EXISTS password_reset_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(128) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    used TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    request_ip VARCHAR(64) NULL,
    user_agent VARCHAR(255) NULL,
    consumed_at DATETIME NULL,
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE INDEX idx_prt_user ON password_reset_token(user_id);
CREATE INDEX idx_prt_expires ON password_reset_token(expires_at);
CREATE INDEX idx_prt_used ON password_reset_token(used);