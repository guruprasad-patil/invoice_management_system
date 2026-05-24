-- =============================================================================
-- V1: Complete initial schema for AIIMS
-- All tables created in dependency order.
-- =============================================================================

-- ── Users ─────────────────────────────────────────────────────────────────────
CREATE TABLE users
(
    id         BIGSERIAL    PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    role       VARCHAR(50)  NOT NULL DEFAULT 'VIEWER',
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role  ON users (role);

-- ── Refresh Tokens ────────────────────────────────────────────────────────────
CREATE TABLE refresh_tokens
(
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token       VARCHAR(512) NOT NULL UNIQUE,
    expiry_date TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_token   ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

-- ── Customers ─────────────────────────────────────────────────────────────────
CREATE TABLE customers
(
    id            BIGSERIAL    PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255),
    phone         VARCHAR(50),
    company       VARCHAR(255),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city          VARCHAR(100),
    state         VARCHAR(100),
    zip_code      VARCHAR(20),
    country       VARCHAR(100),
    tax_id        VARCHAR(100),
    notes         TEXT,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by    BIGINT       REFERENCES users (id),
    updated_by    BIGINT       REFERENCES users (id)
);

CREATE UNIQUE INDEX idx_customers_email_active ON customers (email)
    WHERE deleted = FALSE AND email IS NOT NULL;

CREATE INDEX idx_customers_name    ON customers (name);
CREATE INDEX idx_customers_deleted ON customers (deleted);
CREATE INDEX idx_customers_company ON customers (company);

-- ── Invoice Number Sequence ───────────────────────────────────────────────────
CREATE SEQUENCE invoice_number_seq
    START WITH 1000
    INCREMENT BY 1
    NO MAXVALUE
    CACHE 1;

-- ── Invoices ──────────────────────────────────────────────────────────────────
CREATE TABLE invoices
(
    id              BIGSERIAL      PRIMARY KEY,
    invoice_number  VARCHAR(50)    NOT NULL UNIQUE,
    customer_id     BIGINT         NOT NULL REFERENCES customers (id),
    status          VARCHAR(50)    NOT NULL DEFAULT 'DRAFT',
    issue_date      DATE           NOT NULL,
    due_date        DATE           NOT NULL,
    subtotal        DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    tax_rate        DECIMAL(5, 2)  NOT NULL DEFAULT 0.00,
    tax_amount      DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    discount_rate   DECIMAL(5, 2)  NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    total_amount    DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    paid_amount     DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    balance_due     DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    notes           TEXT,
    terms           TEXT,
    deleted         BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT         REFERENCES users (id),
    updated_by      BIGINT         REFERENCES users (id)
);

CREATE INDEX idx_invoices_invoice_number ON invoices (invoice_number);
CREATE INDEX idx_invoices_customer_id    ON invoices (customer_id);
CREATE INDEX idx_invoices_status         ON invoices (status);
CREATE INDEX idx_invoices_due_date       ON invoices (due_date);
CREATE INDEX idx_invoices_issue_date     ON invoices (issue_date);
CREATE INDEX idx_invoices_deleted        ON invoices (deleted);

COMMENT ON COLUMN invoices.balance_due IS 'Denormalized: total_amount - paid_amount. Kept for query performance.';

-- ── Invoice Items ─────────────────────────────────────────────────────────────
CREATE TABLE invoice_items
(
    id              BIGSERIAL      PRIMARY KEY,
    invoice_id      BIGINT         NOT NULL REFERENCES invoices (id) ON DELETE CASCADE,
    description     VARCHAR(500)   NOT NULL,
    quantity        DECIMAL(10, 2) NOT NULL DEFAULT 1.00,
    unit_price      DECIMAL(15, 2) NOT NULL,
    tax_rate        DECIMAL(5, 2)  NOT NULL DEFAULT 0.00,
    tax_amount      DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    discount_rate   DECIMAL(5, 2)  NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    total_amount    DECIMAL(15, 2) NOT NULL,
    sort_order      INT            NOT NULL DEFAULT 0
);

CREATE INDEX idx_invoice_items_invoice_id ON invoice_items (invoice_id);

-- ── Payments ──────────────────────────────────────────────────────────────────
CREATE TABLE payments
(
    id               BIGSERIAL      PRIMARY KEY,
    invoice_id       BIGINT         NOT NULL REFERENCES invoices (id),
    amount           DECIMAL(15, 2) NOT NULL,
    payment_date     DATE           NOT NULL,
    payment_method   VARCHAR(50),
    reference_number VARCHAR(100),
    notes            TEXT,
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       BIGINT         REFERENCES users (id)
);

CREATE INDEX idx_payments_invoice_id   ON payments (invoice_id);
CREATE INDEX idx_payments_payment_date ON payments (payment_date);

-- ── Audit Logs ────────────────────────────────────────────────────────────────
CREATE TABLE audit_logs
(
    id          BIGSERIAL    PRIMARY KEY,
    action      VARCHAR(50)  NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id   VARCHAR(100),
    user_id     BIGINT       REFERENCES users (id) ON DELETE SET NULL,
    user_email  VARCHAR(255),
    old_value   TEXT,
    new_value   TEXT,
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_action      ON audit_logs (action);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs (entity_type);
CREATE INDEX idx_audit_logs_entity_id   ON audit_logs (entity_id);
CREATE INDEX idx_audit_logs_user_id     ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_created_at  ON audit_logs (created_at);

-- ── Seed: Default Admin User ──────────────────────────────────────────────────
-- Password: Admin@123 (BCrypt strength 12)
-- CHANGE THIS PASSWORD IMMEDIATELY IN PRODUCTION
INSERT INTO users (email, password, first_name, last_name, role, enabled)
VALUES ('admin@aiims.com',
        '$2a$12$mJgUMSJXKEp7SvhQ1.FXCOc8oBY8fvtJq0CW0eJC.bXGqvQI3dT2.',
        'System',
        'Administrator',
        'ADMIN',
        TRUE);
