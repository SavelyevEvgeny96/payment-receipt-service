CREATE TYPE payment_method_enum as ENUM (
    'FULL_PREPAYMENT',
    'PREPAYMENT',
    'ADVANCE',
    'FULL_PAYMENT',
    'PARTIAL_PAYMENT',
    'CREDIT',
    'CREDIT_PAYMENT'
);

CREATE CAST (varchar AS payment_method_enum) WITH INOUT AS IMPLICIT;

CREATE TYPE payment_type_enum as ENUM (
    'CASH',
    'NON_CASH',
    'ADVANCE_PAYMENT',
    'POST_PAYMENT',
    'OTHER_FORM_OF_PAYMENT'
);

CREATE CAST (varchar AS payment_type_enum) WITH INOUT AS IMPLICIT;

CREATE TYPE receipt_system_enum as ENUM (
    'ATOL'
);

CREATE CAST (varchar AS receipt_system_enum) WITH INOUT AS IMPLICIT;

CREATE TYPE vat_type_enum as ENUM (
    'NONE',
    'VAT0',
    'VAT10',
    'VAT18',
    'VAT110',
    'VAT118',
    'VAT20',
    'VAT120'
);

CREATE CAST (varchar AS vat_type_enum) WITH INOUT AS IMPLICIT;


CREATE TYPE receipt_state_enum as ENUM (
    'NEW',
    'WAIT',
    'DONE',
    'FAIL'
);

CREATE CAST (varchar AS receipt_state_enum) WITH INOUT AS IMPLICIT;
