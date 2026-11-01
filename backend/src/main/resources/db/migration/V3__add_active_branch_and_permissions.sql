ALTER TABLE app_user ADD COLUMN active_branch_id UUID;

UPDATE app_user u
SET active_branch_id = b.id
FROM branch b
WHERE b.company_id = u.company_id
  AND b.code = 'MATRIZ'
  AND u.active_branch_id IS NULL;

ALTER TABLE app_user
    ADD CONSTRAINT app_user_active_branch_fk
    FOREIGN KEY (active_branch_id) REFERENCES branch(id) ON DELETE RESTRICT;

CREATE TABLE permission (
    code VARCHAR(100) PRIMARY KEY,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE role_permission (
    role VARCHAR(30) NOT NULL,
    permission_code VARCHAR(100) NOT NULL REFERENCES permission(code) ON DELETE CASCADE,
    PRIMARY KEY (role, permission_code),
    CONSTRAINT role_permission_role_check CHECK (role IN ('ADMIN', 'MANAGER', 'SELLER', 'CASHIER', 'STOCK', 'USER'))
);

INSERT INTO permission (code, description) VALUES
    ('COMPANY_READ', 'Consultar empresa e filiais'),
    ('COMPANY_MANAGE', 'Administrar empresa e filiais'),
    ('USER_READ', 'Consultar usuários'),
    ('USER_MANAGE', 'Administrar usuários e permissões')
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permission (role, permission_code)
SELECT 'ADMIN', code FROM permission
ON CONFLICT DO NOTHING;

INSERT INTO role_permission (role, permission_code)
SELECT 'MANAGER', code FROM permission WHERE code IN ('COMPANY_READ', 'COMPANY_MANAGE', 'USER_READ')
ON CONFLICT DO NOTHING;
