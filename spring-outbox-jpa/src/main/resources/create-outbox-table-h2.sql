CREATE TABLE IF NOT EXISTS outbox (
    id         UUID NOT NULL,
    type       VARCHAR(255) NOT NULL,
    payload    BLOB,
    metadata   JSON,
    PRIMARY KEY (id)
);

CREATE INDEX idx_type ON outbox (type);
