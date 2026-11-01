CREATE TABLE customer (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES company(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branch(id) ON DELETE RESTRICT,
    customer_type VARCHAR(2) NOT NULL DEFAULT 'PJ',
    name VARCHAR(180) NOT NULL,
    document VARCHAR(32),
    email VARCHAR(254),
    phone VARCHAR(32),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT customer_type_check CHECK (customer_type IN ('PF', 'PJ')),
    CONSTRAINT customer_company_document_unique UNIQUE (company_id, document)
);

CREATE INDEX customer_company_idx ON customer(company_id);
CREATE INDEX customer_branch_idx ON customer(branch_id);
CREATE INDEX customer_name_idx ON customer(company_id, lower(name));
