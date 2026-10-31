-- Índices de apoio às chaves estrangeiras compostas e consultas de relacionamento.
CREATE INDEX IF NOT EXISTS idx_user_role_user_company
    ON public.user_role (user_id, company_id);
CREATE INDEX IF NOT EXISTS idx_user_role_role_company
    ON public.user_role (role_id, company_id);
CREATE INDEX IF NOT EXISTS idx_user_branch_user_company
    ON public.user_branch_access (user_id, company_id);
CREATE INDEX IF NOT EXISTS idx_user_branch_branch_company
    ON public.user_branch_access (branch_id, company_id);
CREATE INDEX IF NOT EXISTS idx_role_permission_permission
    ON public.role_permission (permission_code);
CREATE INDEX IF NOT EXISTS idx_audit_event_branch_company
    ON public.audit_event (branch_id, company_id);
