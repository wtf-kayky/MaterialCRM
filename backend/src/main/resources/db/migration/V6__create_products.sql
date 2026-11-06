CREATE TABLE product (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES company(id) ON DELETE RESTRICT,
    sku VARCHAR(60) NOT NULL,
    name VARCHAR(180) NOT NULL,
    category VARCHAR(100),
    unit VARCHAR(20) NOT NULL DEFAULT 'UN',
    cost_price NUMERIC(14, 2) NOT NULL DEFAULT 0,
    sale_price NUMERIC(14, 2) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT product_company_sku_unique UNIQUE (company_id, sku),
    CONSTRAINT product_prices_check CHECK (cost_price >= 0 AND sale_price >= 0)
);

CREATE INDEX product_company_idx ON product(company_id);
CREATE INDEX product_name_idx ON product(company_id, lower(name));
