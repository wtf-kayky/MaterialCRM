CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    role VARCHAR(30) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT app_user_email_unique UNIQUE (email),
    CONSTRAINT app_user_role_check CHECK (role IN ('ADMIN', 'MANAGER', 'SELLER', 'CASHIER', 'STOCK', 'USER'))
);

CREATE UNIQUE INDEX app_user_email_lower_idx ON app_user (lower(email));
