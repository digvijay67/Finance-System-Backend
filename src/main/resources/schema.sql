-- ============================================================
--  Finance System — PostgreSQL Schema
--  Compatible with: PostgreSQL 14+
-- ============================================================

-- ── Extensions ───────────────────────────────────────────────

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ── Database ─────────────────────────────────────────────────

-- Run as superuser before applying the rest:
-- CREATE DATABASE finance_db;
-- \c finance_db

-- ── Types / Enums ─────────────────────────────────────────────

DO $$ BEGIN
    CREATE TYPE user_role   AS ENUM ('ADMIN', 'ANALYST', 'VIEWER');
    CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE');
    CREATE TYPE record_type AS ENUM ('INCOME', 'EXPENSE');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- ── Users Table ───────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(100)    NOT NULL,
    email       VARCHAR(150)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    role        VARCHAR(20)     NOT NULL CHECK (role IN ('ADMIN','ANALYST','VIEWER')),
    status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Users indexes
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email  ON users (email);
CREATE        INDEX IF NOT EXISTS idx_users_role   ON users (role);
CREATE        INDEX IF NOT EXISTS idx_users_status ON users (status);

-- ── Financial Records Table ───────────────────────────────────

CREATE TABLE IF NOT EXISTS financial_records (
    id          BIGSERIAL PRIMARY KEY,
    amount      NUMERIC(19, 2)  NOT NULL CHECK (amount > 0),
    type        VARCHAR(20)     NOT NULL CHECK (type IN ('INCOME','EXPENSE')),
    category    VARCHAR(100)    NOT NULL,
    record_date DATE            NOT NULL,
    description VARCHAR(500),
    user_id     BIGINT          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Financial records indexes
CREATE INDEX IF NOT EXISTS idx_records_user_id   ON financial_records (user_id);
CREATE INDEX IF NOT EXISTS idx_records_type      ON financial_records (type);
CREATE INDEX IF NOT EXISTS idx_records_category  ON financial_records (category);
CREATE INDEX IF NOT EXISTS idx_records_date      ON financial_records (record_date);
CREATE INDEX IF NOT EXISTS idx_records_user_type ON financial_records (user_id, type);
CREATE INDEX IF NOT EXISTS idx_records_user_date ON financial_records (user_id, record_date);

-- ── Auto-update updated_at trigger ───────────────────────────

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_users_updated_at            ON users;
DROP TRIGGER IF EXISTS trg_financial_records_updated_at ON financial_records;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_financial_records_updated_at
    BEFORE UPDATE ON financial_records
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ── Sample Data ───────────────────────────────────────────────
-- Default password for all users: Password1!
-- BCrypt hash (strength 12) generated offline

INSERT INTO users (full_name, email, password, role, status) VALUES
    ('Admin User',   'admin@finance.com', '$2a$12$Y5JfnXvzPmCw9eAgpYd1suc05TjQyS5F.h4fF3RmRJBMIrJMmBSMu', 'ADMIN',   'ACTIVE'),
    ('Alice Johnson','alice@finance.com', '$2a$12$Y5JfnXvzPmCw9eAgpYd1suc05TjQyS5F.h4fF3RmRJBMIrJMmBSMu', 'ANALYST', 'ACTIVE'),
    ('Bob Smith',    'bob@finance.com',   '$2a$12$Y5JfnXvzPmCw9eAgpYd1suc05TjQyS5F.h4fF3RmRJBMIrJMmBSMu', 'ANALYST', 'ACTIVE'),
    ('Carol White',  'carol@finance.com', '$2a$12$Y5JfnXvzPmCw9eAgpYd1suc05TjQyS5F.h4fF3RmRJBMIrJMmBSMu', 'VIEWER',  'ACTIVE'),
    ('David Brown',  'david@finance.com', '$2a$12$Y5JfnXvzPmCw9eAgpYd1suc05TjQyS5F.h4fF3RmRJBMIrJMmBSMu', 'VIEWER',  'INACTIVE')
ON CONFLICT (email) DO NOTHING;

INSERT INTO financial_records (amount, type, category, record_date, description, user_id)
SELECT v.amount, v.type::VARCHAR, v.category, v.record_date::DATE, v.description, u.id
FROM (VALUES
    (5000.00, 'INCOME',  'Salary',    '2024-01-31', 'January salary',            'alice@finance.com'),
    (5000.00, 'INCOME',  'Salary',    '2024-02-29', 'February salary',           'alice@finance.com'),
    ( 800.00, 'INCOME',  'Freelance', '2024-03-15', 'Website project',           'alice@finance.com'),
    (1200.00, 'EXPENSE', 'Rent',      '2024-01-05', 'Monthly rent',              'alice@finance.com'),
    ( 350.00, 'EXPENSE', 'Groceries', '2024-01-20', 'Supermarket',               'alice@finance.com'),
    ( 200.00, 'EXPENSE', 'Utilities', '2024-02-10', 'Electric + water',          'alice@finance.com'),
    (6500.00, 'INCOME',  'Salary',    '2024-01-31', 'January salary',            'bob@finance.com'),
    (1500.00, 'INCOME',  'Bonus',     '2024-03-01', 'Q1 performance bonus',      'bob@finance.com'),
    (2000.00, 'EXPENSE', 'Rent',      '2024-01-05', 'Monthly rent',              'bob@finance.com'),
    ( 500.00, 'EXPENSE', 'Travel',    '2024-02-18', 'Business trip flights',     'bob@finance.com'),
    ( 150.00, 'EXPENSE', 'Dining',    '2024-03-22', 'Team dinner',               'bob@finance.com')
) AS v(amount, type, category, record_date, description, email)
JOIN users u ON u.email = v.email;

-- ── Useful Views (optional) ───────────────────────────────────

CREATE OR REPLACE VIEW v_user_balance AS
SELECT
    u.id                                           AS user_id,
    u.full_name,
    u.email,
    COALESCE(SUM(CASE WHEN r.type = 'INCOME'  THEN r.amount END), 0) AS total_income,
    COALESCE(SUM(CASE WHEN r.type = 'EXPENSE' THEN r.amount END), 0) AS total_expense,
    COALESCE(SUM(CASE WHEN r.type = 'INCOME'  THEN r.amount
                      WHEN r.type = 'EXPENSE' THEN -r.amount END), 0) AS net_balance
FROM users u
LEFT JOIN financial_records r ON r.user_id = u.id
GROUP BY u.id, u.full_name, u.email;

CREATE OR REPLACE VIEW v_monthly_summary AS
SELECT
    u.id          AS user_id,
    u.email,
    DATE_TRUNC('month', r.record_date) AS month,
    r.type,
    SUM(r.amount)                       AS total
FROM financial_records r
JOIN users u ON u.id = r.user_id
GROUP BY u.id, u.email, month, r.type
ORDER BY u.id, month, r.type;
