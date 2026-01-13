CREATE TABLE receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE,
    state VARCHAR(50) NOT NULL,
    receipt_system receipt_system_enum,
    total DECIMAL(10, 2) NOT NULL,
    client_email VARCHAR(255) NOT NULL,
    client_phone VARCHAR(50),
    depersonalization BOOLEAN DEFAULT false,
    external_id UUID,
    date_send TIMESTAMP,
    date_create TIMESTAMP DEFAULT now(),
    date_update TIMESTAMP DEFAULT now()
);

CREATE TABLE receipt_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    receipt_id UUID REFERENCES receipts(id),
    payment_type payment_type_enum,
    sum DECIMAL(10, 2) NOT NULL
);

CREATE TABLE receipt_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    receipt_id UUID REFERENCES receipts(id),
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    sum DECIMAL(10, 2) NOT NULL,
    vat_type vat_type_enum,
    payment_method payment_method_enum,
    payment_object VARCHAR(255) NOT NULL
);

CREATE INDEX idx_receipts_state ON receipts(state);
CREATE INDEX idx_receipts_external_id ON receipts(external_id);