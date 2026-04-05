-- Flyway migration: V1__init_schema.sql
-- This file is equivalent to schema.sql but structured for Flyway.
-- Place in: src/main/resources/db/migration/

-- ── Users ─────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_role   ON users (role);
CREATE INDEX IF NOT EXISTS idx_users_status ON users (status);

-- ── Financial Records ─────────────────────────────────────────

CREATE TABLE IF NOT EXISTS financial_records (
    id          BIGSERIAL PRIMARY KEY,
    amount      NUMERIC(19,2) NOT NULL,
    type        VARCHAR(20)   NOT NULL,
    category    VARCHAR(100)  NOT NULL,
    record_date DATE          NOT NULL,
    description VARCHAR(500),
    user_id     BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_records_user_id   ON financial_records (user_id);
CREATE INDEX IF NOT EXISTS idx_records_type      ON financial_records (type);
CREATE INDEX IF NOT EXISTS idx_records_category  ON financial_records (category);
CREATE INDEX IF NOT EXISTS idx_records_date      ON financial_records (record_date);
CREATE INDEX IF NOT EXISTS idx_records_user_type ON financial_records (user_id, type);
CREATE INDEX IF NOT EXISTS idx_records_user_date ON financial_records (user_id, record_date);
