CREATE TABLE checkout_mappings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product VARCHAR(255) NOT NULL,
    channel VARCHAR(255) NOT NULL,
    login VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX idx_checkout_mappings_product_channel ON checkout_mappings(product, channel);

INSERT INTO checkout_mappings(product, channel, login, password)VALUES('ALL','ALL', 'ATOL_LOGIN', 'ATOL_PASSWORD');