CREATE TABLE refresh_tokens
(
    id          VARCHAR(36)  NOT NULL DEFAULT (UUID()),
    user_id     VARCHAR(36)  NOT NULL,
    family_id   VARCHAR(36)  NOT NULL,
    token_hash  CHAR(64)     NOT NULL UNIQUE,
    device_name VARCHAR(100) NULL,
    expires_at  TIMESTAMP    NOT NULL,
    used_at     TIMESTAMP    NULL,
    revoked_at  TIMESTAMP    NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT (CURRENT_TIMESTAMP),
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_family (family_id)
);