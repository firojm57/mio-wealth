-- ==========================================================
-- Mio Wealth Database Schema (Approach 3: Unified Categories + Tags)
-- ==========================================================

-- 1. Unified Master Financial Categories Table (Natural PK)
CREATE TABLE IF NOT EXISTS financial_category (
    code VARCHAR(50) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    domain VARCHAR(20) NOT NULL, -- 'INVESTMENT', 'SAVING', 'EXPENSE', 'LIABILITY'
    description VARCHAR(255)
);

-- Seed Canonical Categories Across All 4 Domains
INSERT INTO financial_category (code, name, domain, description) VALUES
-- Investment Categories
('STOCKS', 'Stocks & Equities', 'INVESTMENT', 'Publicly traded equity shares and stocks'),
('MUTUAL_FUNDS', 'Mutual Funds & ETFs', 'INVESTMENT', 'Index funds, active mutual funds, and ETFs'),
('REAL_ESTATE', 'Real Estate', 'INVESTMENT', 'Residential, commercial properties, and REITs'),
('GOLD', 'Gold & Commodities', 'INVESTMENT', 'Physical gold, sovereign gold bonds (SGB), and commodities'),
('CRYPTO', 'Cryptocurrency', 'INVESTMENT', 'Digital crypto assets and tokens'),

-- Savings & Government Schemes Categories
('PF', 'Employees Provident Fund (EPF)', 'SAVING', 'Government backed retirement saving scheme for salaried employees'),
('PPF', 'Public Provident Fund (PPF)', 'SAVING', 'Long-term government savings scheme with tax benefits'),
('SSY', 'Sukanya Samriddhi Yojana (SSY)', 'SAVING', 'Government backed savings scheme for girl child education and marriage'),
('NPS', 'National Pension System (NPS)', 'SAVING', 'Voluntary defined contribution retirement savings scheme'),
('FD', 'Fixed Deposit (FD)', 'SAVING', 'Bank term deposits with guaranteed interest returns'),
('RD', 'Recurring Deposit (RD)', 'SAVING', 'Monthly recurring deposit scheme'),
('SAVINGS_ACCOUNT', 'Savings Bank Account', 'SAVING', 'Liquid savings bank deposits'),
('CASH', 'Cash Reserves', 'SAVING', 'Physical emergency cash reserves'),

-- Expense Categories
('FOOD_DINING', 'Food & Dining', 'EXPENSE', 'Groceries, restaurants, and food delivery'),
('HOUSING', 'Housing & Rent', 'EXPENSE', 'Rent, property maintenance, and repairs'),
('UTILITIES', 'Bills & Utilities', 'EXPENSE', 'Electricity, water, gas, internet, and mobile recharges'),
('TRANSPORTATION', 'Transportation & Fuel', 'EXPENSE', 'Fuel, public transit, vehicle maintenance, and cab fares'),
('HEALTHCARE', 'Healthcare & Medical', 'EXPENSE', 'Doctor consultations, medicines, and health insurance'),
('ENTERTAINMENT', 'Entertainment & Leisure', 'EXPENSE', 'Movies, streaming subscriptions, outings, and hobbies'),
('SHOPPING', 'Shopping & Lifestyle', 'EXPENSE', 'Clothing, electronics, personal care, and home goods'),
('EDUCATION', 'Education & Self-Improvement', 'EXPENSE', 'Tuition fees, books, courses, and certifications'),
('TRAVEL', 'Travel & Vacation', 'EXPENSE', 'Flight bookings, hotels, and holiday trips'),
('MISCELLANEOUS', 'Miscellaneous Expenses', 'EXPENSE', 'Other unclassified discretionary expenses'),

-- Liability & Debt Categories
('HOME_LOAN', 'Home Loan / Mortgage', 'LIABILITY', 'Long-term loan secured for residential or commercial property'),
('AUTO_LOAN', 'Vehicle / Auto Loan', 'LIABILITY', 'Loan taken for purchasing cars or two-wheelers'),
('PERSONAL_LOAN', 'Personal Loan', 'LIABILITY', 'Unsecured personal bank loan'),
('EDUCATION_LOAN', 'Education Loan', 'LIABILITY', 'Student loan for higher education'),
('CREDIT_CARD', 'Credit Card Outstanding', 'LIABILITY', 'Revolving credit card balance and short-term debt');

-- 2. User Credentials (UUID PK)
CREATE TABLE IF NOT EXISTS user_login (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- 3. User Profile (UUID PK)
CREATE TABLE IF NOT EXISTS user_profile (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    email_id VARCHAR(150) NOT NULL,
    mobile_number VARCHAR(30),
    profile_picture VARCHAR(255),
    user_fk VARCHAR(36) NOT NULL UNIQUE,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_fk) REFERENCES user_login (id) ON DELETE CASCADE
);

-- 4. User Addresses (UUID PK)
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

-- 5. User Investments (UUID PK + Category FK + Tags)
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
    user_fk VARCHAR(36) NOT NULL,
    CONSTRAINT fk_investment_category FOREIGN KEY (category_code) REFERENCES financial_category (code),
    CONSTRAINT fk_investment_user FOREIGN KEY (user_fk) REFERENCES user_login (id) ON DELETE CASCADE
);

-- 6. User Savings & Schemes (UUID PK + Category FK + Tags)
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
    user_fk VARCHAR(36) NOT NULL,
    CONSTRAINT fk_saving_category FOREIGN KEY (category_code) REFERENCES financial_category (code),
    CONSTRAINT fk_saving_user FOREIGN KEY (user_fk) REFERENCES user_login (id) ON DELETE CASCADE
);

-- 7. User Liabilities & Debts (UUID PK + Category FK + Tags)
CREATE TABLE IF NOT EXISTS liability (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    amount DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    interest_rate DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks VARCHAR(255),
    tags VARCHAR(255),
    category_code VARCHAR(50) NOT NULL,
    user_fk VARCHAR(36) NOT NULL,
    CONSTRAINT fk_liability_category FOREIGN KEY (category_code) REFERENCES financial_category (code),
    CONSTRAINT fk_liability_user FOREIGN KEY (user_fk) REFERENCES user_login (id) ON DELETE CASCADE
);

-- 8. User Expenses (UUID PK + Category FK + Tags)
CREATE TABLE IF NOT EXISTS expense (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    amount DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    expense_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'UPI',
    remarks VARCHAR(255),
    tags VARCHAR(255),
    category_code VARCHAR(50) NOT NULL,
    user_fk VARCHAR(36) NOT NULL,
    CONSTRAINT fk_expense_category FOREIGN KEY (category_code) REFERENCES financial_category (code),
    CONSTRAINT fk_expense_user FOREIGN KEY (user_fk) REFERENCES user_login (id) ON DELETE CASCADE
);
