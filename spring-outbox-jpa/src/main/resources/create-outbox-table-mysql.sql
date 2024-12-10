CREATE TABLE IF NOT EXISTS outbox
(
    id         BINARY(16) NOT NULL,
    type       VARCHAR(255) NOT NULL,
    payload    BLOB,
    related_to BINARY(16),
    metadata   JSON,
    PRIMARY KEY (id),
    INDEX      idx_type (type),
    INDEX      idx_related_to (related_to)
);
