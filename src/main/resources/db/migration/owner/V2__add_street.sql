CREATE SCHEMA IF NOT EXISTS owner_schema;

ALTER TABLE owner_schema.owners
    ADD COLUMN street TEXT;
