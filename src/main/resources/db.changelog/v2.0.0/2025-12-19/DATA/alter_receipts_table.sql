DROP TABLE receipts CASCADE;

CREATE TABLE receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL,
    state VARCHAR(50) NOT NULL,
    receipt_type VARCHAR(50) NOT NULL,
    receipt_system receipt_system_enum,
    product VARCHAR(255),
    channel VARCHAR(255),
    order_id UUID,
    total DECIMAL(10, 2) NOT NULL,
    client_email VARCHAR(255) NOT NULL,
    client_phone VARCHAR(50),
    depersonalization BOOLEAN DEFAULT false,
    external_id UUID,
    date_send TIMESTAMP,
    date_create TIMESTAMP DEFAULT now(),
    date_update TIMESTAMP DEFAULT now()
);

CREATE UNIQUE INDEX ON receipts(payment_id);
CREATE INDEX ON receipts(state);
CREATE INDEX ON receipts(external_id);