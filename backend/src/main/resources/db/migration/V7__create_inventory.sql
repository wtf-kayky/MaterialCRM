CREATE TABLE stock (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES company(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branch(id) ON DELETE RESTRICT,
    product_id UUID NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
    quantity NUMERIC(14, 3) NOT NULL DEFAULT 0,
    minimum_quantity NUMERIC(14, 3) NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT stock_branch_product_unique UNIQUE (branch_id, product_id),
    CONSTRAINT stock_quantity_check CHECK (quantity >= 0 AND minimum_quantity >= 0)
);

CREATE TABLE stock_movement (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES company(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branch(id) ON DELETE RESTRICT,
    product_id UUID NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
    movement_type VARCHAR(10) NOT NULL,
    quantity NUMERIC(14, 3) NOT NULL,
    note VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT stock_movement_type_check CHECK (movement_type IN ('IN', 'OUT', 'ADJUST')),
    CONSTRAINT stock_movement_quantity_check CHECK (quantity > 0)
);

CREATE INDEX stock_company_branch_idx ON stock(company_id, branch_id);
CREATE INDEX stock_movement_product_idx ON stock_movement(product_id, created_at DESC);
