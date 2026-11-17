CREATE TABLE company (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    legal_name VARCHAR(180) NOT NULL,
    trade_name VARCHAR(180) NOT NULL,
    tax_id VARCHAR(32) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE branch (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES company(id) ON DELETE RESTRICT,
    name VARCHAR(160) NOT NULL,
    code VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT branch_company_code_unique UNIQUE (company_id, code)
);

ALTER TABLE app_user ADD COLUMN company_id UUID;

INSERT INTO company (legal_name, trade_name, tax_id)
VALUES ('MaterialCRM Local', 'MaterialCRM Local', 'LOCAL-DEFAULT')
ON CONFLICT (tax_id) DO NOTHING;

INSERT INTO branch (company_id, name, code)
SELECT id, 'Matriz', 'MATRIZ'
FROM company
WHERE tax_id = 'LOCAL-DEFAULT'
  AND NOT EXISTS (
      SELECT 1 FROM branch b
      WHERE b.company_id = company.id AND b.code = 'MATRIZ'
  );

UPDATE app_user
SET company_id = (SELECT id FROM company WHERE tax_id = 'LOCAL-DEFAULT')
WHERE company_id IS NULL;

ALTER TABLE app_user
    ALTER COLUMN company_id SET NOT NULL,
    ADD CONSTRAINT app_user_company_fk FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE RESTRICT;

CREATE INDEX app_user_company_idx ON app_user(company_id);
CREATE INDEX branch_company_idx ON branch(company_id);
