CREATE TABLE billing_schema.usage_records (
    id          UUID PRIMARY KEY,
    owner_id    UUID                     NOT NULL,
    usage_type  TEXT                     NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    cost_cents  BIGINT                   NOT NULL
);