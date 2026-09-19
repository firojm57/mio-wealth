# REST API Specification & Contract Reference

This document provides the authoritative API specification for all REST endpoints exposed by the **Mio Wealth** backend server.

---

## 1. Authentication Endpoints

### 1.1 User Self-Registration
Creates a new investor user account and initializes the corresponding personal profile.

* **Method**: `POST`
* **Path**: `/api/v1/auth/register`
* **Authentication**: None (`Public`)
* **Consumes**: `application/json`
* **Produces**: `application/json`

#### Request Body ([`UserRegisterVO`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/vo/auth/UserRegisterVO.java)):
```json
{
  "userId": "alex_smith",
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
| `userId` | `String` | Yes | `@NotBlank` | Unique account login name. |
| `password` | `String` | Yes | `@NotBlank`, min 6 chars | Raw password to be BCrypt encrypted. |
| `firstName` | `String` | Yes | `@NotBlank` | Investor's given name. |
| `middleName`| `String` | No | Optional | Investor's middle name. |
| `lastName` | `String` | Yes | `@NotBlank` | Investor's surname. |
| `email` | `String` | Yes | `@NotBlank`, `@Email` | Valid email format. |
| `mobile` | `String` | No | Optional | Phone number. |

#### Responses:
* **HTTP 201 Created**: Account created successfully.
  ```json
  {
    "status": "Success",
    "message": null
  }
  ```
* **HTTP 400 Bad Request**: Validation error or user already exists.
  ```json
  {
    "timestamp": "2026-09-19T20:00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "User ID already exists.",
    "path": "/api/v1/auth/register"
  }
  ```

---

### 1.2 User Sign In / Token Generation
Validates user credentials and generates a signed 24-hour JWT Bearer token.

* **Method**: `POST`
* **Path**: `/api/v1/auth/login`
* **Authentication**: None (`Public`)
* **Consumes**: `application/json`
* **Produces**: `application/json`

#### Request Body ([`AuthRequestVO`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/vo/auth/AuthRequestVO.java)):
```json
{
  "userId": "alex_smith",
  "password": "SecurePassword123!"
}
```

#### Responses:
* **HTTP 200 OK**: Credentials verified, JWT returned.
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGV4X3NtaXRoIiwiaWF0IjoxNzI2NzQ1NjAwLCJleHAiOjE3MjY4MzIwMDB9.signature...",
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
  ```json
  {
    "timestamp": "2026-09-19T20:00:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid user ID or password.",
    "path": "/api/v1/auth/login"
  }
  ```

---

## 2. Financial Portfolio Endpoints

### 2.1 Get Balance & Portfolio Summary
Returns aggregated financial metrics including total assets, liabilities, calculated net worth, and equity ratio percentage.

* **Method**: `GET`
* **Path**: `/api/v1/balance`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)
* **Produces**: `application/json`

#### Request Headers:
```http
Authorization: Bearer <JWT_TOKEN>
```

#### Responses:
* **HTTP 200 OK**:
  ```json
  {
    "totalAssets": 1795650.0,
    "totalLiabilities": 547340.0,
    "netWorth": 1248310.0,
    "equityRatio": 69.52,
    "assets": [
      {
        "name": "Cash & Savings",
        "category": "Liquid",
        "value": 120150.0,
        "change": "+1.2%",
        "icon": "icon-cashflow"
      },
      {
        "name": "Brokerage & Stocks",
        "category": "Investment",
        "value": 452300.0,
        "change": "+6.4%",
        "icon": "icon-investments"
      },
      {
        "name": "Real Estate Portfolio",
        "category": "Property",
        "value": 1223200.0,
        "change": "+0.8%",
        "icon": "icon-home"
      }
    ],
    "liabilities": [
      {
        "name": "Home Mortgage",
        "category": "Secured Debt",
        "value": 512140.0,
        "rate": "3.85%",
        "icon": "icon-home"
      },
      {
        "name": "Student & Car Loans",
        "category": "Unsecured",
        "value": 31200.0,
        "rate": "4.5%",
        "icon": "icon-liabilities"
      },
      {
        "name": "Credit Cards Balance",
        "category": "Revolving",
        "value": 4000.0,
        "rate": "14.99%",
        "icon": "icon-expenses"
      }
    ]
  }
  ```
* **HTTP 401 Unauthorized**: Missing, invalid, or expired Bearer token.

---

### 2.2 List User Investments
Retrieves all asset holdings for the currently authenticated investor.

* **Method**: `GET`
* **Path**: `/api/v1/investments`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)
* **Produces**: `application/json`

#### Responses:
* **HTTP 200 OK**:
  ```json
  [
    {
      "id": 1,
      "name": "Vanguard Total Stock ETF",
      "symbol": "VTI",
      "allocation": "35%",
      "shares": "350",
      "price": "$260.40",
      "value": "$91,140",
      "returnRate": "+14.2%",
      "up": true
    },
    {
      "id": 2,
      "name": "Apple Inc.",
      "symbol": "AAPL",
      "allocation": "25%",
      "shares": "280",
      "price": "$225.10",
      "value": "$63,028",
      "returnRate": "+22.5%",
      "up": true
    }
  ]
  ```

---

### 2.3 Create Investment Asset
Records a new financial asset holding associated with the current user.

* **Method**: `POST`
* **Path**: `/api/v1/investments`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)
* **Consumes**: `application/json`
* **Produces**: `application/json`

#### Request Body ([`InvestmentRequestVO`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/vo/investment/InvestmentRequestVO.java)):
```json
{
  "amount": 5000.0,
  "quantity": 25,
  "remarks": "Microsoft Corp (MSFT)",
  "action": "BUY"
}
```

#### Responses:
* **HTTP 201 Created**:
  ```json
  {
    "id": 105,
    "name": "Microsoft Corp (MSFT)",
    "symbol": "INV-105",
    "allocation": "10%",
    "shares": "25",
    "price": "$200.00",
    "value": "$5,000",
    "returnRate": "+5.0%",
    "up": true
  }
  ```

---

## 3. User Profile Endpoints

### 3.1 Get Current User Profile
Retrieves the profile information for the currently authenticated investor based on the verified JWT identity.

* **Method**: `GET`
* **Path**: `/api/v1/users/profile`
* **Authentication**: `Bearer <JWT_TOKEN>` (`Authenticated`)
* **Produces**: `application/json`

#### Request Headers:
```http
Authorization: Bearer <JWT_TOKEN>
```

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
* **HTTP 401 Unauthorized**: Missing, expired, or invalid JWT token.
* **HTTP 404 Not Found**: Profile record does not exist for the authenticated user ID.

