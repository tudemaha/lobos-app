CREATE TABLE collection_quota_interactions
(
    id         VARCHAR(36) NOT NULL DEFAULT (UUID()),
    user_id    VARCHAR(36) NOT NULL,
    action     VARCHAR(50) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT (CURRENT_TIMESTAMP),
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX idx_quota_user_action (user_id, action)
);
