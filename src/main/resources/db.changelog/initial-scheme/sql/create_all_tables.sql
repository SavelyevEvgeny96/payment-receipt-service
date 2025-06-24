--liquibase formatted sql
--changeset Диоп Шейх Тижан:create_all_tables
--preconditions onfail:mark_ran onerror:halt

-- Create table for Cash Registers
CREATE TABLE cash_registers (
    system_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    system_code VARCHAR(255) NOT NULL,
    system_name VARCHAR(255) NOT NULL,
    date_create TIMESTAMP DEFAULT now(),
    date_update TIMESTAMP DEFAULT now()
);

-- Create table for API Versions
CREATE TABLE api_versions (
    version_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version_code VARCHAR(50) NOT NULL,
    version_name VARCHAR(255) NOT NULL,
    date_create TIMESTAMP DEFAULT now(),
    date_update TIMESTAMP DEFAULT now()
);

-- Create table for Check Statuses
CREATE TABLE check_statuses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    state_id VARCHAR(50) NOT NULL,
    state_name VARCHAR(255) NOT NULL
);

-- Create table for Payment Types
CREATE TABLE payment_types (
    type_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type_id_code INT NOT NULL,
    type_id_name VARCHAR(255) NOT NULL,
    date_create TIMESTAMP DEFAULT now(),
    date_update TIMESTAMP DEFAULT now()
);

-- Create table for VAT Types
CREATE TABLE vat_types (
    vat_type_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vat_type_code VARCHAR(50) NOT NULL,
    vat_type_name VARCHAR(255) NOT NULL,
    date_create TIMESTAMP DEFAULT now(),
    date_update TIMESTAMP DEFAULT now()
);

-- Create table for Payment Objects
CREATE TABLE payment_objects (
    payment_object_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_object_id_code VARCHAR(50) NOT NULL,
    payment_object_id_name VARCHAR(255) NOT NULL,
    date_create TIMESTAMP DEFAULT now(),
    date_update TIMESTAMP DEFAULT now()
);

-- Create table for Payment Methods
CREATE TABLE payment_methods (
    payment_method_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_method_code VARCHAR(50) NOT NULL,
    payment_method_name VARCHAR(255) NOT NULL,
    date_create TIMESTAMP DEFAULT now(),
    date_update TIMESTAMP DEFAULT now()
);

-- Create table for Payment Documents
CREATE TABLE payment_documents (
    doc_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_user_id VARCHAR(255),
    client_email VARCHAR(255) NOT NULL,
    client_phone VARCHAR(50),
    client_name VARCHAR(255),
    total DOUBLE PRECISION NOT NULL,
    external_id VARCHAR(255),
    system_id UUID REFERENCES cash_registers(system_id),
    version_id UUID REFERENCES api_versions(version_id),
    state_id UUID REFERENCES check_statuses(id),
    date_send TIMESTAMP,
    date_create TIMESTAMP DEFAULT now(),
    date_update TIMESTAMP DEFAULT now()
);

-- Create table for Payment Receipts
CREATE TABLE payment_receipts (
    payment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type_id UUID REFERENCES payment_types(type_id),
    sum DOUBLE PRECISION NOT NULL,
    doc_id UUID REFERENCES payment_documents(doc_id),
    date_create TIMESTAMP DEFAULT now(),
    date_update TIMESTAMP DEFAULT now()
);

-- Create table for Payment Items
CREATE TABLE payment_items (
    item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    sum DOUBLE PRECISION NOT NULL,
    payment_method_id UUID REFERENCES payment_methods(payment_method_id),
    payment_object_id UUID REFERENCES payment_objects(payment_object_id),
    vat_type_id UUID REFERENCES vat_types(vat_type_id),
    doc_id UUID REFERENCES payment_documents(doc_id),
    date_create TIMESTAMP DEFAULT now(),
    date_update TIMESTAMP DEFAULT now()
);

-- Create table for Configuration Data
CREATE TABLE configuration_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    param_name VARCHAR(255) NOT NULL,
    param_value VARCHAR(255) NOT NULL
);