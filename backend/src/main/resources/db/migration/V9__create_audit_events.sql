CREATE TABLE audit_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES company(id) ON DELETE RESTRICT,
    actor_user_id UUID REFERENCES app_user(id) ON DELETE SET NULL,
    event_type VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id UUID,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX audit_event_company_occurred_idx ON audit_event (company_id, occurred_at DESC);
CREATE INDEX audit_event_actor_occurred_idx ON audit_event (actor_user_id, occurred_at DESC);
