-- ==========================================================
-- Mio Wealth Master/Shared Database Schema (public schema)
-- ==========================================================

-- 1. Master Financial Categories (Canonical Catalog across all 4 domains)
CREATE TABLE IF NOT EXISTS financial_category (
    code VARCHAR(50) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    domain VARCHAR(20) NOT NULL, -- 'INVESTMENT', 'SAVING', 'EXPENSE', 'LIABILITY'
    description VARCHAR(255)
);

-- Seed Canonical Categories
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
('CREDIT_CARD', 'Credit Card Outstanding', 'LIABILITY', 'Revolving credit card balance and short-term debt')
ON CONFLICT (code) DO NOTHING;

-- 2. Master User Identity & Tenant Mapping
CREATE TABLE IF NOT EXISTS user_login (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    tenant_schema VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
