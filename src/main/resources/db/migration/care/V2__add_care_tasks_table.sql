CREATE TABLE care_schema.care_tasks (
    id            UUID PRIMARY KEY,
    plant_id      UUID    NOT NULL,
    active        BOOLEAN NOT NULL,
    type          TEXT    NOT NULL,
    source        TEXT    NOT NULL,
    next_due_date DATE    NOT NULL,
    interval_days INT
);