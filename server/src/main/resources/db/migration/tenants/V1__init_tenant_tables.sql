-- ==========================================================
-- Mio Wealth Tenant Database Schema (Isolated per tenant)
-- ==========================================================

-- 1. Tenant User Profile
CREATE TABLE IF NOT EXISTS user_profile (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    email_id VARCHAR(150) NOT NULL,
    mobile_number VARCHAR(30),
    profile_picture VARCHAR(255),
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 2. Tenant Addresses
CREATE TABLE IF NOT EXISTS user_address (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    line1 VARCHAR(255),
    line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    user_profile_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_user_address_profile FOREIGN KEY (user_profile_id) REFERENCES user_profile (id) ON DELETE CASCADE
);

-- 3. Tenant Investments
CREATE TABLE IF NOT EXISTS investment (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    symbol VARCHAR(30) NOT NULL,
    asset_name VARCHAR(150) NOT NULL,
    amount DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    quantity INT NOT NULL DEFAULT 0,
    unit_price DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    investment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks VARCHAR(255),
    tags VARCHAR(255),
    action VARCHAR(50) NOT NULL DEFAULT 'BUY',
    category_code VARCHAR(50) NOT NULL,
    CONSTRAINT fk_investment_category FOREIGN KEY (category_code) REFERENCES financial_category (code)
);

-- 4. Tenant Savings & Schemes
CREATE TABLE IF NOT EXISTS saving (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    institution_name VARCHAR(150) NOT NULL,
    account_number VARCHAR(100),
    amount DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    saving_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks VARCHAR(255),
    tags VARCHAR(255),
    action VARCHAR(50) NOT NULL DEFAULT 'DEPOSIT',
    category_code VARCHAR(50) NOT NULL,
    CONSTRAINT fk_saving_category FOREIGN KEY (category_code) REFERENCES financial_category (code)
);

-- 5. Tenant Liabilities & Debts
CREATE TABLE IF NOT EXISTS liability (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    amount DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    interest_rate DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks VARCHAR(255),
    tags VARCHAR(255),
    category_code VARCHAR(50) NOT NULL,
    CONSTRAINT fk_liability_category FOREIGN KEY (category_code) REFERENCES financial_category (code)
);

-- 6. Tenant Expenses
CREATE TABLE IF NOT EXISTS expense (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    amount DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    expense_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'UPI',
    remarks VARCHAR(255),
    tags VARCHAR(255),
    category_code VARCHAR(50) NOT NULL,
    CONSTRAINT fk_expense_category FOREIGN KEY (category_code) REFERENCES financial_category (code)
);

-- Indexes for lightning fast queries within tenant schema
CREATE INDEX IF NOT EXISTS idx_investment_category ON investment(category_code);
CREATE INDEX IF NOT EXISTS idx_saving_category ON saving(category_code);
CREATE INDEX IF NOT EXISTS idx_liability_category ON liability(category_code);
CREATE INDEX IF NOT EXISTS idx_expense_category ON expense(category_code);
