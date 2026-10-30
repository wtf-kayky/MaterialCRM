CREATE TABLE company (
    id UUID PRIMARY KEY,
    legal_name VARCHAR(180) NOT NULL,
    trade_name VARCHAR(180) NOT NULL,
    tax_id VARCHAR(32) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE branch (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES company(id),
    name VARCHAR(160) NOT NULL,
    code VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_branch_company_code UNIQUE (company_id, code)
);

CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES company(id),
    full_name VARCHAR(180) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_app_user_company_email UNIQUE (company_id, email)
);

CREATE TABLE role (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES company(id),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_role_company_name UNIQUE (company_id, name)
);

CREATE TABLE permission (
    code VARCHAR(100) PRIMARY KEY,
    module VARCHAR(60) NOT NULL,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE user_role (
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE role_permission (
    role_id UUID NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    permission_code VARCHAR(100) NOT NULL REFERENCES permission(code),
    PRIMARY KEY (role_id, permission_code)
);

CREATE TABLE user_branch_access (
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    branch_id UUID NOT NULL REFERENCES branch(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, branch_id)
);

CREATE TABLE audit_event (
    id UUID PRIMARY KEY,
    company_id UUID REFERENCES company(id),
    branch_id UUID REFERENCES branch(id),
    actor_user_id UUID REFERENCES app_user(id),
    event_type VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id UUID,
    request_id VARCHAR(100),
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_branch_company ON branch(company_id);
CREATE INDEX idx_app_user_company ON app_user(company_id);
CREATE INDEX idx_audit_event_company_time ON audit_event(company_id, occurred_at DESC);
CREATE INDEX idx_audit_event_actor_time ON audit_event(actor_user_id, occurred_at DESC);

INSERT INTO permission (code, module, description) VALUES
    ('COMPANY_READ', 'ADMINISTRATION', 'Consultar empresas e filiais'),
    ('COMPANY_MANAGE', 'ADMINISTRATION', 'Administrar empresas e filiais'),
    ('USER_READ', 'ADMINISTRATION', 'Consultar usuários e perfis'),
    ('USER_MANAGE', 'ADMINISTRATION', 'Administrar usuários e perfis'),
    ('AUDIT_READ', 'ADMINISTRATION', 'Consultar eventos de auditoria');

