# REST API Specification & Contract Reference

This document provides the authoritative API specification for all REST endpoints exposed by the **Mio Wealth** backend server. All payloads adhere to Clean Code principles: zero UI formatting (no currency symbols or artificial display strings) and strictly typed raw numeric and UUID values with automatic tenant schema routing.

---

## 1. Authentication Endpoints

### 1.1 User Self-Registration
Creates a master login account, assigns a cryptographically unique tenant schema, provisions the isolated schema using Flyway, and seeds the tenant profile.

* **Method**: `POST`
* **Path**: `/api/v1/auth/register`
* **Authentication**: None (`Public`)
* **Consumes**: `application/json`
* **Produces**: `application/json`

#### Request Body ([`UserRegisterVO`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/vo/auth/UserRegisterVO.java)):
```json
{
  "username": "alex_smith",
  "password": "SecurePassword123!",
  "firstName": "Alex",
  "middleName": "J",
  "lastName": "Smith",
  "email": "alex.smith@example.com",
  "mobile": "+1-555-0199"
}
```

| Field | Type | Required | Validation Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `username` | `String` | Yes | `@NotBlank` | Unique account login username. |
| `password` | `String` | Yes | `@NotBlank`, min 6 chars | Raw password to be BCrypt encrypted. |
| `firstName` | `String` | Yes | `@NotBlank` | Investor's given name. |
| `middleName`| `String` | No | Optional | Investor's middle name. |
| `lastName` | `String` | Yes | `@NotBlank` | Investor's surname. |
| `email` | `String` | Yes | `@NotBlank`, `@Email` | Valid email address. |
| `mobile` | `String` | No | Optional | Telephone number. |

#### Responses:
* **HTTP 201 Created**:
  ```json
  {
    "status": "Success",
    "message": null
  }
  ```
* **HTTP 400 Bad Request**:
  ```json
  {
    "timestamp": "2026-09-20T10:00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Username already exists.",
    "path": "/api/v1/auth/register"
  }
  ```

---

### 1.2 User Sign In / Token Generation
Validates user credentials against `public.user_login` and returns a signed 24-hour JWT Bearer token containing the user identity and assigned `tenant` schema claim.

* **Method**: `POST`
* **Path**: `/api/v1/auth/login`
* **Authentication**: None (`Public`)
* **Consumes**: `application/json`
* **Produces**: `application/json`

#### Request Body ([`AuthRequestVO`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/vo/auth/AuthRequestVO.java)):
```json
{
  "username": "alex_smith",
  "password": "SecurePassword123!"
}
```

#### Responses:
* **HTTP 200 OK**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGV4X3NtaXRoIiwidGVuYW50IjoidGVuYW50X2ExYjJjM2Q0Li4uIn0...",
    "tokenType": "Bearer",
    "userId": "alex_smith",
    "userProfile": {
      "firstName": "Alex",
      "middleName": "J",
      "lastName": "Smith",
      "email": "alex.smith@example.com",
      "mobile": "+1-555-0199"
    }
  }
  ```
* **HTTP 401 Unauthorized**: Invalid credentials.

---

## 2. Category Catalog Endpoints

### 2.1 Get Categories by Domain
Retrieves canonical master categories from `public.financial_category`, optionally filtered by domain (`INVESTMENT`, `SAVING`, `EXPENSE`, `LIABILITY`).

* **Method**: `GET`
* **Path**: `/api/v1/categories?domain={INVESTMENT|SAVING|EXPENSE|LIABILITY}`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)
* **Produces**: `application/json`

#### Responses:
* **HTTP 200 OK**:
  ```json
  [
    {
      "code": "STOCKS",
      "domain": "INVESTMENT",
      "name": "Direct Equities / Stocks",
      "description": "Public company shares"
    },
    {
      "code": "MUTUAL_FUNDS",
      "domain": "INVESTMENT",
      "name": "Mutual Funds & ETFs",
      "description": "Index, equity, and debt funds"
    }
  ]
  ```

---

## 3. Financial Portfolio Endpoints

### 3.1 Get Lightweight Portfolio Summary
Returns top-level balance sheet metrics from the tenant's schema without loading large itemized collections.

* **Method**: `GET`
* **Path**: `/api/v1/balance/summary`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)
* **Produces**: `application/json`

#### Responses:
* **HTTP 200 OK**:
  ```json
  {
    "totalAssets": 1795650.0,
    "totalLiabilities": 547340.0,
    "netWorth": 1248310.0,
    "growthRate": 5.4
  }
  ```

---

### 3.2 Get Assets Breakdown
Retrieves all asset holdings aggregated from the tenant's investments and liquid savings with pure numeric values.

* **Method**: `GET`
* **Path**: `/api/v1/balance/assets`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)
* **Produces**: `application/json`

#### Responses:
* **HTTP 200 OK**:
  ```json
  [
    {
      "id": "e7b8c2d1-4f9a-4c8e-9a1b-3c5d7e9f1a3b",
      "name": "Vanguard S&P 500 ETF",
      "categoryCode": "MUTUAL_FUNDS",
      "categoryName": "Mutual Funds & ETFs",
      "domain": "INVESTMENT",
      "value": 452300.0,
      "changeRate": 6.4,
      "tags": "#retirement #equity",
      "updatedAt": "2026-09-20T10:30:00"
    }
  ]
  ```

---

### 3.3 Get Liabilities Breakdown
Retrieves user debt obligations from the tenant's schema.

* **Method**: `GET`
* **Path**: `/api/v1/balance/liabilities`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)
* **Produces**: `application/json`

#### Responses:
* **HTTP 200 OK**:
  ```json
  [
    {
      "id": "b3c4d5e6-f7a8-4b2c-9d3e-4f5a6b7c8d9e",
      "name": "Home Mortgage Loan",
      "categoryCode": "HOME_LOAN",
      "categoryName": "Home Mortgage Loan",
      "domain": "LIABILITY",
      "amount": 512140.0,
      "interestRate": 8.5,
      "tags": "#tax_deductible",
      "createdAt": "2026-09-20T10:00:00"
    }
  ]
  ```

---

### 3.4 Create Liability
Adds a new liability or loan obligation to the tenant schema.

* **Method**: `POST`
* **Path**: `/api/v1/balance/liabilities`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)
* **Consumes**: `application/json`
* **Produces**: `application/json`

#### Request Body ([`LiabilityRequestVO`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/vo/balance/LiabilityRequestVO.java)):
```json
{
  "name": "Car Loan",
  "categoryCode": "AUTO_LOAN",
  "amount": 450000.0,
  "interestRate": 9.2,
  "tags": "#vehicle"
}
```

#### Responses:
* **HTTP 201 Created**: Returns created [`LiabilityItemVO`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/vo/balance/LiabilityItemVO.java).

---

### 3.5 Delete Liability
Deletes a liability obligation by its UUID from the tenant schema.

* **Method**: `DELETE`
* **Path**: `/api/v1/balance/liabilities/{id}`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)

#### Responses:
* **HTTP 204 No Content**: Deleted successfully.

---

### 3.6 Composite Balance Endpoint
Returns summary metrics, assets, and liabilities combined in a single response.

* **Method**: `GET`
* **Path**: `/api/v1/balance`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)
* **Produces**: `application/json`

---

## 4. Investment Endpoints (Full CRUD)

### 4.1 List Tenant Investments
Retrieves all asset holdings for the active tenant.

* **Method**: `GET`
* **Path**: `/api/v1/investments`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)
* **Produces**: `application/json`

#### Responses:
* **HTTP 200 OK**:
  ```json
  [
    {
      "id": "c4d5e6f7-a8b9-4c3d-0e4f-5a6b7c8d9e0f",
      "symbol": "AAPL",
      "name": "Apple Inc.",
      "domain": "INVESTMENT",
      "categoryCode": "STOCKS",
      "categoryName": "Direct Equities / Stocks",
      "amount": 150000.0,
      "quantity": 10,
      "unitPrice": 15000.0,
      "returnRate": 0.0,
      "tags": "TECH, LONG_TERM",
      "action": "BUY",
      "investmentDate": "2026-09-20T14:30:00"
    }
  ]
  ```

---

### 4.2 Get Investment by ID
Retrieves a single investment holding by its UUID.

* **Method**: `GET`
* **Path**: `/api/v1/investments/{id}`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)
* **Produces**: `application/json`

#### Responses:
* **HTTP 200 OK**: Returns [`InvestmentVO`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/vo/investment/InvestmentVO.java).
* **HTTP 400 Bad Request**: If investment does not exist in the tenant schema.

---

### 4.3 Create Investment
Records a new investment holding within the active tenant schema.

* **Method**: `POST`
* **Path**: `/api/v1/investments`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)
* **Consumes**: `application/json`
* **Produces**: `application/json`

#### Request Body ([`InvestmentRequestVO`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/vo/investment/InvestmentRequestVO.java)):
```json
{
  "symbol": "INFY",
  "assetName": "Infosys Ltd",
  "categoryCode": "STOCKS",
  "amount": 75000.0,
  "quantity": 50,
  "unitPrice": 1500.0,
  "remarks": "Long-term tech holding",
  "tags": "TECH, EQUITY",
  "action": "BUY"
}
```

#### Responses:
* **HTTP 201 Created**: Returns created [`InvestmentVO`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/vo/investment/InvestmentVO.java).

---

### 4.4 Update Investment
Updates an existing investment holding by UUID within the active tenant schema.

* **Method**: `PUT`
* **Path**: `/api/v1/investments/{id}`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)
* **Consumes**: `application/json`
* **Produces**: `application/json`

#### Request Body: Same as `InvestmentRequestVO`.

#### Responses:
* **HTTP 200 OK**: Returns updated [`InvestmentVO`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/vo/investment/InvestmentVO.java).

---

### 4.5 Delete Investment
Permanently removes an investment holding by UUID from the active tenant schema.

* **Method**: `DELETE`
* **Path**: `/api/v1/investments/{id}`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)

#### Responses:
* **HTTP 204 No Content**: Deleted successfully.

---

## 5. User Profile Endpoints

### 5.1 Get Current User Profile
Retrieves the profile information for the active tenant from `tenant_<uuid>.user_profile`.

* **Method**: `GET`
* **Path**: `/api/v1/users/profile`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)
* **Produces**: `application/json`

#### Responses:
* **HTTP 200 OK**:
  ```json
  {
    "firstName": "Alex",
    "middleName": "J",
    "lastName": "Smith",
    "email": "alex.smith@example.com",
    "mobile": "+1-555-0199",
    "addressVOS": []
  }
  ```
