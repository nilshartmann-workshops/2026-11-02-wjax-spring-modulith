CREATE TABLE billing_schema.invoices (
    id             UUID PRIMARY KEY,
    owner_id       UUID      NOT NULL,
    owner_name     TEXT      NOT NULL,
    billing_period TEXT      NOT NULL,
    amount         NUMERIC   NOT NULL,
    created_at     TIMESTAMP NOT NULL
);

CREATE TABLE billing_schema.invoice_items (
    invoice_id  UUID    NOT NULL REFERENCES billing_schema.invoices (id),
    description TEXT    NOT NULL,
    amount      NUMERIC NOT NULL
);
