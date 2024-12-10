CREATE TABLE IF NOT EXISTS outbox (
    id         UUID NOT NULL,
    type       VARCHAR(255) NOT NULL,
    payload    BLOB,
    related_to UUID,
    metadata   JSON,
    PRIMARY KEY (id)
);

CREATE INDEX idx_type ON outbox (type);
CREATE INDEX idx_related_to ON outbox (related_to);
