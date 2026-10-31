-- MaterialCRM — fundação de identidade, empresas e filiais.
-- Supabase Auth é a autoridade de credenciais; app_user guarda apenas o perfil de negócio.

CREATE TABLE public.application_metadata (
    key VARCHAR(100) PRIMARY KEY,
    value VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO public.application_metadata (key, value)
VALUES ('schema_version', '1');

CREATE TABLE public.company (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    legal_name VARCHAR(180) NOT NULL,
    trade_name VARCHAR(180) NOT NULL,
    tax_id VARCHAR(32) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE public.branch (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES public.company(id) ON DELETE RESTRICT,
    name VARCHAR(160) NOT NULL,
    code VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_branch_company_code UNIQUE (company_id, code),
    CONSTRAINT uq_branch_id_company UNIQUE (id, company_id)
);

-- auth.users pertence ao Supabase Auth. Nenhuma senha é duplicada nesta tabela.
CREATE TABLE public.app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth_user_id UUID NOT NULL UNIQUE REFERENCES auth.users(id) ON DELETE RESTRICT,
    company_id UUID NOT NULL REFERENCES public.company(id) ON DELETE RESTRICT,
    full_name VARCHAR(180) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_app_user_id_company UNIQUE (id, company_id)
);

CREATE TABLE public.role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES public.company(id) ON DELETE RESTRICT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_role_company_name UNIQUE (company_id, name),
    CONSTRAINT uq_role_id_company UNIQUE (id, company_id)
);

CREATE TABLE public.permission (
    code VARCHAR(100) PRIMARY KEY,
    module VARCHAR(60) NOT NULL,
    description VARCHAR(255) NOT NULL
);

-- A chave composta impede associar usuário e papel de empresas diferentes.
CREATE TABLE public.user_role (
    company_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id, company_id)
        REFERENCES public.app_user(id, company_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id, company_id)
        REFERENCES public.role(id, company_id) ON DELETE CASCADE
);

CREATE TABLE public.role_permission (
    role_id UUID NOT NULL REFERENCES public.role(id) ON DELETE CASCADE,
    permission_code VARCHAR(100) NOT NULL REFERENCES public.permission(code) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_code)
);

-- A chave composta impede conceder acesso a uma filial de outra empresa.
CREATE TABLE public.user_branch_access (
    company_id UUID NOT NULL,
    user_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    PRIMARY KEY (user_id, branch_id),
    CONSTRAINT fk_user_branch_user FOREIGN KEY (user_id, company_id)
        REFERENCES public.app_user(id, company_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_branch_branch FOREIGN KEY (branch_id, company_id)
        REFERENCES public.branch(id, company_id) ON DELETE CASCADE
);

CREATE TABLE public.audit_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID REFERENCES public.company(id) ON DELETE RESTRICT,
    branch_id UUID,
    actor_user_id UUID REFERENCES public.app_user(id) ON DELETE SET NULL,
    event_type VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id UUID,
    request_id VARCHAR(100),
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_audit_branch_company FOREIGN KEY (branch_id, company_id)
        REFERENCES public.branch(id, company_id) ON DELETE RESTRICT
);

CREATE INDEX idx_branch_company ON public.branch(company_id);
CREATE INDEX idx_app_user_company ON public.app_user(company_id);
CREATE INDEX idx_app_user_auth_user ON public.app_user(auth_user_id);
CREATE INDEX idx_user_role_company ON public.user_role(company_id);
CREATE INDEX idx_user_branch_company ON public.user_branch_access(company_id);
CREATE INDEX idx_audit_event_company_time ON public.audit_event(company_id, occurred_at DESC);
CREATE INDEX idx_audit_event_actor_time ON public.audit_event(actor_user_id, occurred_at DESC);

INSERT INTO public.permission (code, module, description) VALUES
    ('COMPANY_READ', 'ADMINISTRATION', 'Consultar empresas e filiais'),
    ('COMPANY_MANAGE', 'ADMINISTRATION', 'Administrar empresas e filiais'),
    ('USER_READ', 'ADMINISTRATION', 'Consultar usuários e perfis'),
    ('USER_MANAGE', 'ADMINISTRATION', 'Administrar usuários e perfis'),
    ('AUDIT_READ', 'ADMINISTRATION', 'Consultar eventos de auditoria');
