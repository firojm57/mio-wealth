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

-- 3. Tenant Categories (Dynamic Catalog per Tenant)
CREATE TABLE IF NOT EXISTS category (
    code VARCHAR(50) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    domain VARCHAR(20) NOT NULL, -- 'INVESTMENT', 'SAVING', 'EXPENSE', 'LIABILITY'
    description VARCHAR(255),
    is_custom BOOLEAN NOT NULL DEFAULT false
);

-- Seed Canonical Categories for the Tenant
INSERT INTO category (code, name, domain, description, is_custom) VALUES
-- Investment Categories
('STOCKS', 'Stocks & Equities', 'INVESTMENT', 'Publicly traded equity shares and stocks', false),
('MUTUAL_FUNDS', 'Mutual Funds & ETFs', 'INVESTMENT', 'Index funds, active mutual funds, and ETFs', false),
('REAL_ESTATE', 'Real Estate', 'INVESTMENT', 'Residential, commercial properties, and REITs', false),
('GOLD', 'Gold & Commodities', 'INVESTMENT', 'Physical gold, sovereign gold bonds (SGB), and commodities', false),
('CRYPTO', 'Cryptocurrency', 'INVESTMENT', 'Digital crypto assets and tokens', false),

-- Savings & Government Schemes Categories
('PF', 'Employees Provident Fund (EPF)', 'SAVING', 'Government backed retirement saving scheme for salaried employees', false),
('PPF', 'Public Provident Fund (PPF)', 'SAVING', 'Long-term government savings scheme with tax benefits', false),
('SSY', 'Sukanya Samriddhi Yojana (SSY)', 'SAVING', 'Government backed savings scheme for girl child education and marriage', false),
('NPS', 'National Pension System (NPS)', 'SAVING', 'Voluntary defined contribution retirement savings scheme', false),
('FD', 'Fixed Deposit (FD)', 'SAVING', 'Bank term deposits with guaranteed interest returns', false),
('RD', 'Recurring Deposit (RD)', 'SAVING', 'Monthly recurring deposit scheme', false),
('SAVINGS_ACCOUNT', 'Savings Bank Account', 'SAVING', 'Liquid savings bank deposits', false),
('CASH', 'Cash Reserves', 'SAVING', 'Physical emergency cash reserves', false),

-- Expense Categories
('FOOD_DINING', 'Food & Dining', 'EXPENSE', 'Groceries, restaurants, and food delivery', false),
('HOUSING', 'Housing & Rent', 'EXPENSE', 'Rent, property maintenance, and repairs', false),
('UTILITIES', 'Bills & Utilities', 'EXPENSE', 'Electricity, water, gas, internet, and mobile recharges', false),
('TRANSPORTATION', 'Transportation & Fuel', 'EXPENSE', 'Fuel, public transit, vehicle maintenance, and cab fares', false),
('HEALTHCARE', 'Healthcare & Medical', 'EXPENSE', 'Doctor consultations, medicines, and health insurance', false),
('ENTERTAINMENT', 'Entertainment & Leisure', 'EXPENSE', 'Movies, streaming subscriptions, outings, and hobbies', false),
('SHOPPING', 'Shopping & Lifestyle', 'EXPENSE', 'Clothing, electronics, personal care, and home goods', false),
('EDUCATION', 'Education & Self-Improvement', 'EXPENSE', 'Tuition fees, books, courses, and certifications', false),
('TRAVEL', 'Travel & Vacation', 'EXPENSE', 'Flight bookings, hotels, and holiday trips', false),
('MISCELLANEOUS', 'Miscellaneous Expenses', 'EXPENSE', 'Other unclassified discretionary expenses', false),

-- Liability & Debt Categories
('HOME_LOAN', 'Home Loan / Mortgage', 'LIABILITY', 'Long-term loan secured for residential or commercial property', false),
('AUTO_LOAN', 'Vehicle / Auto Loan', 'LIABILITY', 'Loan taken for purchasing cars or two-wheelers', false),
('PERSONAL_LOAN', 'Personal Loan', 'LIABILITY', 'Unsecured personal bank loan', false),
('EDUCATION_LOAN', 'Education Loan', 'LIABILITY', 'Student loan for higher education', false),
('CREDIT_CARD', 'Credit Card Outstanding', 'LIABILITY', 'Revolving credit card balance and short-term debt', false)
ON CONFLICT (code) DO NOTHING;

-- 4. Tenant Tag Catalog
CREATE TABLE IF NOT EXISTS tag (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    domain VARCHAR(30) NOT NULL DEFAULT 'INVESTMENT',
    is_system BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed standard investment tags
INSERT INTO tag (id, name, domain, is_system) VALUES
('tag-long-term', 'Long-term', 'INVESTMENT', true),
('tag-high-growth', 'High-growth', 'INVESTMENT', true),
('tag-dividend', 'Dividend', 'INVESTMENT', true),
('tag-defensive', 'Defensive', 'INVESTMENT', true),
('tag-tax-saving', 'Tax-saving', 'INVESTMENT', true),
('tag-speculative', 'Speculative', 'INVESTMENT', true)
ON CONFLICT (name) DO NOTHING;

-- 5. Tenant Investments
CREATE TABLE IF NOT EXISTS investment (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    asset_name VARCHAR(150) NOT NULL,
    category_code VARCHAR(50) NOT NULL,
    buying_price DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    investment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_sold BOOLEAN NOT NULL DEFAULT false,
    selling_price DOUBLE PRECISION,
    sold_date TIMESTAMP,
    current_percentage_change DOUBLE PRECISION DEFAULT 0.0,
    remarks VARCHAR(255),
    tags VARCHAR(255),
    CONSTRAINT fk_investment_category FOREIGN KEY (category_code) REFERENCES category (code)
);

-- 6. Tenant Savings & Schemes
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
    CONSTRAINT fk_saving_category FOREIGN KEY (category_code) REFERENCES category (code)
);

-- 7. Tenant Liabilities & Debts
CREATE TABLE IF NOT EXISTS liability (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    amount DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    interest_rate DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks VARCHAR(255),
    tags VARCHAR(255),
    category_code VARCHAR(50) NOT NULL,
    CONSTRAINT fk_liability_category FOREIGN KEY (category_code) REFERENCES category (code)
);

-- 8. Tenant Expenses
CREATE TABLE IF NOT EXISTS expense (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    amount DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    expense_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'UPI',
    remarks VARCHAR(255),
    tags VARCHAR(255),
    category_code VARCHAR(50) NOT NULL,
    CONSTRAINT fk_expense_category FOREIGN KEY (category_code) REFERENCES category (code)
);

-- Indexes for lightning fast queries within tenant schema
CREATE INDEX IF NOT EXISTS idx_category_domain ON category(domain);
CREATE INDEX IF NOT EXISTS idx_tag_domain ON tag(domain);
CREATE INDEX IF NOT EXISTS idx_investment_category ON investment(category_code);
CREATE INDEX IF NOT EXISTS idx_investment_is_sold ON investment(is_sold);
CREATE INDEX IF NOT EXISTS idx_saving_category ON saving(category_code);
CREATE INDEX IF NOT EXISTS idx_liability_category ON liability(category_code);
CREATE INDEX IF NOT EXISTS idx_expense_category ON expense(category_code);
