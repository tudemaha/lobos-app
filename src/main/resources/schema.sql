CREATE TABLE users
(
    id         VARCHAR(36)  NOT NULL DEFAULT (UUID()),
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(200) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT (CURRENT_TIMESTAMP),
    updated_at TIMESTAMP    NOT NULL DEFAULT (CURRENT_TIMESTAMP) ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_email (email)
);

CREATE TABLE collections
(
    id         VARCHAR(36)  NOT NULL DEFAULT (UUID()),
    name       VARCHAR(100) NOT NULL,
    color      VARCHAR(7)   NOT NULL DEFAULT '#ffffff',
    user_id    VARCHAR(36)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT (CURRENT_TIMESTAMP),
    updated_at TIMESTAMP    NOT NULL DEFAULT (CURRENT_TIMESTAMP) ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX idx_name (name)
);

CREATE TABLE grammars
(
    id            VARCHAR(36)  NOT NULL DEFAULT (UUID()),
    word          VARCHAR(100) NOT NULL,
    meaning       TEXT         NOT NULL,
    example       TEXT                  DEFAULT NULL,
    is_starred    BOOL         NOT NULL DEFAULT FALSE,
    collection_id VARCHAR(36)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT (CURRENT_TIMESTAMP),
    updated_at    TIMESTAMP    NOT NULL DEFAULT (CURRENT_TIMESTAMP) ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (collection_id) REFERENCES collections (id) ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX idx_word (word)
);