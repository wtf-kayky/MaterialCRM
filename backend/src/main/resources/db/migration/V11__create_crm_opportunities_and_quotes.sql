CREATE TABLE crm_opportunity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES company(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branch(id) ON DELETE RESTRICT,
    customer_id UUID REFERENCES customer(id) ON DELETE RESTRICT,
    owner_user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
    title VARCHAR(180) NOT NULL,
    stage VARCHAR(20) NOT NULL DEFAULT 'NEW',
    expected_amount NUMERIC(14, 2),
    closed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT crm_opportunity_stage_check CHECK (stage IN ('NEW', 'QUALIFIED', 'PROPOSAL', 'WON', 'LOST')),
    CONSTRAINT crm_opportunity_amount_check CHECK (expected_amount IS NULL OR expected_amount >= 0),
    CONSTRAINT crm_opportunity_closed_check CHECK (
        (stage IN ('WON', 'LOST') AND closed_at IS NOT NULL)
        OR (stage NOT IN ('WON', 'LOST') AND closed_at IS NULL)
    )
);

CREATE TABLE quote (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES company(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branch(id) ON DELETE RESTRICT,
    customer_id UUID NOT NULL REFERENCES customer(id) ON DELETE RESTRICT,
    opportunity_id UUID REFERENCES crm_opportunity(id) ON DELETE RESTRICT,
    created_by_user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    total NUMERIC(14, 2) NOT NULL DEFAULT 0,
    valid_until DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT quote_status_check CHECK (status IN ('DRAFT', 'SENT', 'ACCEPTED', 'REJECTED', 'EXPIRED')),
    CONSTRAINT quote_total_check CHECK (total >= 0)
);

CREATE TABLE quote_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quote_id UUID NOT NULL REFERENCES quote(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
    quantity NUMERIC(14, 3) NOT NULL,
    unit_price NUMERIC(14, 2) NOT NULL,
    total NUMERIC(14, 2) NOT NULL,
    CONSTRAINT quote_item_quantity_check CHECK (quantity > 0),
    CONSTRAINT quote_item_amount_check CHECK (unit_price >= 0 AND total >= 0)
);

CREATE INDEX crm_opportunity_company_branch_stage_idx ON crm_opportunity (company_id, branch_id, stage, updated_at DESC);
CREATE INDEX quote_company_branch_status_idx ON quote (company_id, branch_id, status, created_at DESC);
CREATE INDEX quote_item_quote_idx ON quote_item (quote_id);
