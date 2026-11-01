CREATE TABLE sale (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES company(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branch(id) ON DELETE RESTRICT,
    customer_id UUID REFERENCES customer(id) ON DELETE RESTRICT,
    seller_user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    total NUMERIC(14, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT sale_status_check CHECK (status IN ('COMPLETED', 'CANCELLED')),
    CONSTRAINT sale_total_check CHECK (total >= 0)
);

CREATE TABLE sale_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id UUID NOT NULL REFERENCES sale(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
    quantity NUMERIC(14, 3) NOT NULL,
    unit_price NUMERIC(14, 2) NOT NULL,
    total NUMERIC(14, 2) NOT NULL,
    CONSTRAINT sale_item_quantity_check CHECK (quantity > 0),
    CONSTRAINT sale_item_price_check CHECK (unit_price >= 0 AND total >= 0)
);

CREATE INDEX sale_company_branch_date_idx ON sale(company_id, branch_id, created_at DESC);
CREATE INDEX sale_item_sale_idx ON sale_item(sale_id);
