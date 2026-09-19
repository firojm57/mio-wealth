# Database Schema & Persistence Specification

This document provides a low-level, field-by-field specification of the relational schema, entity relationships, constraints, cascade rules, JPA auditing lifecycle, and database migration architecture for **Mio Wealth**.

---

## 1. Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    user_login ||--o| user_profile : "has profile (1:1)"
    user_login ||--o{ investment : "owns investments (1:N)"
    user_login ||--o{ saving : "owns savings (1:N)"
    user_profile ||--o{ user_address : "has addresses (1:N)"
    investment ||--o{ investment_type : "categorized by (1:N)"

    user_login {
        varchar(100) user_id PK "Unique Account ID"
        varchar(255) password "BCrypt Encrypted Hash"
    }

    user_profile {
        bigint user_profile_id PK "Identity Auto-increment"
        varchar(100) first_name "Required First Name"
        varchar(100) middle_name "Optional Middle Name"
        varchar(100) last_name "Required Last Name"
        varchar(150) email_id "Email Address"
        varchar(30) mobile_number "Phone Number"
        varchar(255) profile_picture "Avatar URL / Path"
        varchar(100) user_id FK "Unique Foreign Key to user_login"
        timestamp created_on "Audit Creation Timestamp"
        timestamp updated_on "Audit Last Modified Timestamp"
        timestamp last_login "Audit Last Login Timestamp"
    }

    user_address {
        bigint address_id PK "Identity Auto-increment"
        varchar(255) line1 "Street Address Line 1"
        varchar(255) line2 "Apartment / Suite"
        varchar(100) city "City"
        varchar(100) state "State / Province"
        varchar(100) country "Country"
        varchar(20) postal_code "ZIP / Postal Code"
        bigint user_profile_id FK "Foreign Key to user_profile"
    }

    investment {
        bigint investment_id PK "Identity Auto-increment"
        double_precision amount "Total Investment Value ($)"
        int quantity "Number of Units / Shares"
        timestamp investment_date "Date of Investment"
        varchar(255) remarks "Notes or Asset Name"
        varchar(50) action "BUY / SELL / HOLD"
        varchar(100) user_id FK "Foreign Key to user_login"
    }

    investment_type {
        bigint type_id PK "Identity Auto-increment"
        varchar(50) type "Category Code (e.g. STK, BOND, ETF)"
        varchar(100) type_name "Human Readable Type Name"
        varchar(255) description "Classification Description"
        bigint investment_id FK "Foreign Key to investment"
    }

    saving {
        bigint saving_id PK "Identity Auto-increment"
        double_precision amount "Liquid Savings Amount ($)"
        timestamp saving_date "Date Recorded"
        varchar(255) remarks "Deposit Description"
        varchar(50) action "DEPOSIT / WITHDRAW"
        varchar(100) user_id FK "Foreign Key to user_login"
    }
```

---

## 2. Table-by-Table Schema Specification

### 2.1 `user_login`
Represents core authentication credentials.
* **JPA Entity**: [`com.greenboard.investman.model.user.User`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/model/user/User.java)

| Column | SQL Type | Modifiers | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `user_id` | `VARCHAR(100)` | `NOT NULL` | `PRIMARY KEY` | Unique account login identifier. |
| `password` | `VARCHAR(255)` | `NOT NULL` | — | BCrypt-hashed password string. |

---

### 2.2 `user_profile`
Stores personal and biographical details. Inherits audit fields from [`UserAudit`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/model/common/UserAudit.java).
* **JPA Entity**: [`com.greenboard.investman.model.user.UserProfile`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/model/user/UserProfile.java)

| Column | SQL Type | Modifiers | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `user_profile_id` | `BIGINT` | `NOT NULL` | `PRIMARY KEY`, `IDENTITY` | Surrogate auto-generated ID. |
| `first_name` | `VARCHAR(100)` | `NOT NULL` | — | First name of the account holder. |
| `middle_name` | `VARCHAR(100)` | `NULL` | — | Optional middle name. |
| `last_name` | `VARCHAR(100)` | `NOT NULL` | — | Last name / family name. |
| `email_id` | `VARCHAR(150)` | `NOT NULL` | — | Primary contact email address. |
| `mobile_number` | `VARCHAR(30)` | `NULL` | — | Contact telephone number. |
| `profile_picture`| `VARCHAR(255)` | `NULL` | — | URL or file path for profile picture. |
| `user_id` | `VARCHAR(100)` | `NULL` | `UNIQUE`, `FK (user_login)` | Foreign key linking 1:1 to `user_login`. |
| `created_on` | `TIMESTAMP` | `NOT NULL` | `DEFAULT CURRENT_TIMESTAMP` | Account creation timestamp. |
| `updated_on` | `TIMESTAMP` | `NOT NULL` | `DEFAULT CURRENT_TIMESTAMP` | Last profile update timestamp. |
| `last_login` | `TIMESTAMP` | `NOT NULL` | `DEFAULT CURRENT_TIMESTAMP` | Most recent authentication timestamp. |

* **Foreign Key**: `CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES user_login(user_id) ON DELETE CASCADE`

---

### 2.3 `user_address`
Stores physical mailing, residential, or billing addresses.
* **JPA Entity**: [`com.greenboard.investman.model.user.Address`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/model/user/Address.java)

| Column | SQL Type | Modifiers | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `address_id` | `BIGINT` | `NOT NULL` | `PRIMARY KEY`, `IDENTITY` | Auto-increment primary key. |
| `line1` | `VARCHAR(255)` | `NULL` | — | Primary street address line. |
| `line2` | `VARCHAR(255)` | `NULL` | — | Suite, unit, or building number. |
| `city` | `VARCHAR(100)` | `NULL` | — | Municipality / city. |
| `state` | `VARCHAR(100)` | `NULL` | — | State or province code. |
| `country` | `VARCHAR(100)` | `NULL` | — | Country name or ISO code. |
| `postal_code` | `VARCHAR(20)` | `NULL` | — | Postal or ZIP routing code. |
| `user_profile_id`| `BIGINT` | `NULL` | `FK (user_profile)` | Owning user profile ID. |

* **Foreign Key**: `CONSTRAINT fk_user_address_profile FOREIGN KEY (user_profile_id) REFERENCES user_profile(user_profile_id) ON DELETE CASCADE`

---

### 2.4 `investment`
Tracks portfolio assets, holdings, equities, bonds, and real estate allocations.
* **JPA Entity**: [`com.greenboard.investman.model.investment.Investment`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/model/investment/Investment.java)

| Column | SQL Type | Modifiers | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `investment_id` | `BIGINT` | `NOT NULL` | `PRIMARY KEY`, `IDENTITY` | Auto-increment primary key. |
| `amount` | `DOUBLE PRECISION`| `NULL` | `DEFAULT 0.0` | Total monetary valuation or cost basis. |
| `quantity` | `INT` | `NULL` | `DEFAULT 0` | Share count, token quantity, or units held. |
| `investment_date`| `TIMESTAMP` | `NULL` | — | Timestamp when transaction was recorded. |
| `remarks` | `VARCHAR(255)` | `NULL` | — | Name or description of holding (e.g., AAPL). |
| `action` | `VARCHAR(50)` | `NULL` | — | Portfolio action (`BUY`, `SELL`, `HOLD`). |
| `user_id` | `VARCHAR(100)` | `NULL` | `FK (user_login)` | Owning investor user ID. |

* **Foreign Key**: `CONSTRAINT fk_investment_user FOREIGN KEY (user_id) REFERENCES user_login(user_id) ON DELETE CASCADE`

---

### 2.5 `investment_type`
Categorizes investment records into asset classes (Equities, Fixed Income, Real Estate, Crypto).
* **JPA Entity**: [`com.greenboard.investman.model.investment.InvestmentType`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/model/investment/InvestmentType.java)

| Column | SQL Type | Modifiers | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `type_id` | `BIGINT` | `NOT NULL` | `PRIMARY KEY`, `IDENTITY` | Auto-increment primary key. |
| `type` | `VARCHAR(50)` | `NULL` | — | Asset class code (e.g. `STK`, `REIT`). |
| `type_name` | `VARCHAR(100)` | `NULL` | — | Category display label. |
| `description` | `VARCHAR(255)` | `NULL` | — | Long-form asset class explanation. |
| `investment_id` | `BIGINT` | `NULL` | `FK (investment)` | Associated parent investment record. |

* **Foreign Key**: `CONSTRAINT fk_type_investment FOREIGN KEY (investment_id) REFERENCES investment(investment_id) ON DELETE CASCADE`

---

### 2.6 `saving`
Tracks liquid cash reserves, emergency funds, and high-yield savings accounts.
* **JPA Entity**: [`com.greenboard.investman.model.saving.Saving`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/model/saving/Saving.java)

| Column | SQL Type | Modifiers | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `saving_id` | `BIGINT` | `NOT NULL` | `PRIMARY KEY`, `IDENTITY` | Auto-increment primary key. |
| `amount` | `DOUBLE PRECISION`| `NULL` | `DEFAULT 0.0` | Balance amount. |
| `saving_date` | `TIMESTAMP` | `NULL` | — | Date recorded. |
| `remarks` | `VARCHAR(255)` | `NULL` | — | Account or goal notes. |
| `action` | `VARCHAR(50)` | `NULL` | — | Flow direction (`DEPOSIT`, `WITHDRAW`). |
| `user_id` | `VARCHAR(100)` | `NULL` | `FK (user_login)` | Owning user account. |

* **Foreign Key**: `CONSTRAINT fk_saving_user FOREIGN KEY (user_id) REFERENCES user_login(user_id) ON DELETE CASCADE`

---

## 3. Auditing & JPA Entity Lifecycle

All temporal audit columns are managed through the mapped superclass [`UserAudit.java`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/model/common/UserAudit.java).

```mermaid
stateDiagram-v2
    [*] --> Instantiated: new UserProfile(...)
    Instantiated --> PrePersist: entityManager.persist()
    PrePersist --> InsertExecuted: @PrePersist sets createdOn, updatedOn, lastLogin = now()
    InsertExecuted --> Managed: Persisted in PostgreSQL
    Managed --> PreUpdate: entityManager.merge() or transaction commit
    PreUpdate --> UpdateExecuted: @PreUpdate sets updatedOn = now()
    UpdateExecuted --> Managed
    Managed --> [*]
```

### Defensive Persistence Architecture:
1. **Spring Data JPA Auditing**: Enabled via `@EnableJpaAuditing` on `InvestmentManagerApplication.java`.
2. **Explicit Fallback Hooks**: Declared `@PrePersist` and `@PreUpdate` methods inside `UserAudit` ensure that even if Spring Data's reflection listener is bypassed, timestamps are guaranteed non-null before the SQL `INSERT` or `UPDATE` executes.

---

## 4. Flyway Migrations & DDL Strategy

* **Migration Script**: [`V1__init_schema.sql`](file:///d:/F_Drive/github/mio-wealth/server/src/main/resources/db/migration/V1__init_schema.sql)
* **Configuration** (`application.yml`):
  ```yaml
  spring:
    jpa:
      hibernate:
        ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
    flyway:
      enabled: ${SPRING_FLYWAY_ENABLED:false}
      baseline-on-migrate: true
      locations: classpath:db/migration
  ```
* **Production Recommendation**: When deploying to production environments, set `SPRING_FLYWAY_ENABLED=true` and `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` to enforce deterministic, versioned database migrations.
