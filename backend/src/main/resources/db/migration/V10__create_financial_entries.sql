CREATE TABLE financial_entry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES company(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branch(id) ON DELETE RESTRICT,
    entry_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    description VARCHAR(250) NOT NULL,
    counterparty VARCHAR(150),
    amount NUMERIC(14, 2) NOT NULL,
    due_date DATE,
    settled_at TIMESTAMPTZ,
    created_by_user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
    settled_by_user_id UUID REFERENCES app_user(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT financial_entry_type_check CHECK (entry_type IN ('RECEIVABLE', 'PAYABLE')),
    CONSTRAINT financial_entry_status_check CHECK (status IN ('OPEN', 'SETTLED', 'CANCELLED')),
    CONSTRAINT financial_entry_amount_check CHECK (amount > 0),
    CONSTRAINT financial_entry_settlement_check CHECK (
        (status = 'SETTLED' AND settled_at IS NOT NULL AND settled_by_user_id IS NOT NULL)
        OR (status <> 'SETTLED' AND settled_at IS NULL AND settled_by_user_id IS NULL)
    )
);

CREATE INDEX financial_entry_company_branch_status_due_idx
    ON financial_entry (company_id, branch_id, status, due_date);
CREATE INDEX financial_entry_company_created_idx
    ON financial_entry (company_id, created_at DESC);
