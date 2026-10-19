ALTER TABLE users ADD COLUMN email_verified_at TIMESTAMP NULL DEFAULT NULL;
ALTER TABLE users ADD COLUMN pending_email VARCHAR(200) NULL DEFAULT NULL;

CREATE TABLE email_verification_tokens
(
    id         VARCHAR(36) NOT NULL DEFAULT (UUID()),
    user_id    VARCHAR(36) NOT NULL,
    token_hash CHAR(64)    NOT NULL UNIQUE,
    purpose    VARCHAR(20) NOT NULL DEFAULT 'REGISTER',
    expires_at TIMESTAMP   NOT NULL,
    used_at    TIMESTAMP   NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT (CURRENT_TIMESTAMP),
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);