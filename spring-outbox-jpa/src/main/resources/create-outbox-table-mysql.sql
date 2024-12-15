CREATE TABLE IF NOT EXISTS outbox
(
    id         BINARY(16) NOT NULL,
    type       VARCHAR(255) NOT NULL,
    payload    BLOB,
    metadata   JSON,
    PRIMARY KEY (id),
    INDEX      idx_type (type)
);
