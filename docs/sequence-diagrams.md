# End-to-End Sequence & Workflow Diagrams

This document illustrates the step-by-step runtime flows across the presentation, security, business service, and database tiers using detailed Mermaid sequence diagrams.

---

## 1. User Self-Registration Workflow

Illustrates the end-to-end flow when a user creates an account from [`/signup`](file:///d:/F_Drive/github/mio-wealth/imui/src/app/components/signup/signup.component.ts).

```mermaid
sequenceDiagram
    autonumber
    actor User as User Browser
    participant UI as SignupComponent
    participant AuthSvcUI as Angular AuthService
    participant API as AuthController
    participant Svc as AuthServiceImpl
    participant Encrypt as BCryptPasswordEncoder
    participant UserRepo as UserRepository
    participant ProfileRepo as UserProfileRepository
    participant DB as PostgreSQL (investmanDB)

    User->>UI: Fills form (userId, password, name, email) & clicks "Create Account"
    UI->>UI: isFormValid() checks password length >= 6 and match
    UI->>AuthSvcUI: register(userRegisterRequest)
    AuthSvcUI->>API: POST /api/v1/auth/register (UserRegisterVO JSON)
    API->>Svc: register(request)
    Svc->>UserRepo: existsById(request.getUserId())
    UserRepo->>DB: SELECT count(*) FROM user_login WHERE user_id = ?
    DB-->>UserRepo: 0 (User does not exist)

    Svc->>Encrypt: encode(rawPassword)
    Encrypt-->>Svc: BCrypt hash string ($2a$10$...)

    Svc->>UserRepo: save(new User(userId, encodedPassword))
    UserRepo->>DB: INSERT INTO user_login (user_id, password) VALUES (?, ?)

    Svc->>ProfileRepo: save(new UserProfile(firstName, lastName, email, ..., user))
    ProfileRepo->>DB: INSERT INTO user_profile (user_profile_id, first_name, last_name, email_id, user_id, ...) VALUES (?, ?, ?, ?, ...)
    DB-->>ProfileRepo: Generated UUID v4 (user_profile_id)

    Svc-->>API: StatusVO(status: "Success")
    API-->>AuthSvcUI: HTTP 201 Created (StatusVO JSON)
    AuthSvcUI-->>UI: Observable completes successfully
    UI->>UI: Displays green toast: "Account created successfully!"
    UI->>User: Navigates router to /login
```

---

## 2. Authentication & JWT Token Issuance

Illustrates the credential validation, BCrypt comparison, and HMAC-SHA256 signature generation.

```mermaid
sequenceDiagram
    autonumber
    actor User as User Browser
    participant UI as LoginComponent
    participant AuthSvcUI as Angular AuthService
    participant API as AuthController
    participant Svc as AuthServiceImpl
    participant UserRepo as UserRepository
    participant ProfileRepo as UserProfileRepository
    participant Encrypt as BCryptPasswordEncoder
    participant JwtProvider as JwtTokenProvider
    participant Storage as Browser sessionStorage

    User->>UI: Enters userId & password, clicks "Sign In"
    UI->>AuthSvcUI: login({ userId, password })
    AuthSvcUI->>API: POST /api/v1/auth/login (AuthRequestVO JSON)
    API->>Svc: login(request)
    Svc->>UserRepo: findById(request.getUserId())
    UserRepo-->>Svc: User entity (with BCrypt hash)

    Svc->>Encrypt: matches(rawPassword, entityPasswordHash)
    Encrypt-->>Svc: true (Valid password)

    Svc->>JwtProvider: generateToken(userId)
    JwtProvider->>JwtProvider: Builds JWT with subject, iat, exp (+24h), signs with HMAC-SHA256
    JwtProvider-->>Svc: Compact signed JWT token string

    Svc->>ProfileRepo: findByUser_UserId(userId)
    ProfileRepo-->>Svc: UserProfile entity
    Svc-->>API: AuthResponseVO(token, userId, userProfileVO)
    API-->>AuthSvcUI: HTTP 200 OK (AuthResponseVO JSON)

    AuthSvcUI->>Storage: sessionStorage.setItem('mio_wealth_auth_token', token)
    AuthSvcUI->>Storage: sessionStorage.setItem('mio_wealth_user_profile', profile)
    AuthSvcUI-->>UI: Login Observable succeeds
    UI->>User: Navigates to /overview dashboard
```

---

## 3. Authenticated Financial Request Execution

Demonstrates how [`authInterceptor`](file:///d:/F_Drive/github/mio-wealth/imui/src/app/interceptors/auth.interceptor.ts) and [`JwtAuthenticationFilter`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/security/JwtAuthenticationFilter.java) secure requests.

```mermaid
sequenceDiagram
    autonumber
    actor User as User Browser
    participant Component as BalanceComponent
    participant Interceptor as authInterceptor
    participant Storage as sessionStorage
    participant Filter as JwtAuthenticationFilter
    participant JwtProvider as JwtTokenProvider
    participant SecContext as SecurityContextHolder
    participant Ctrl as BalanceController
    participant Svc as PortfolioServiceImpl
    participant AssetSvc as AssetServiceImpl
    participant LiabSvc as LiabilityServiceImpl

    Component->>Interceptor: Dispatches concurrent requests (forkJoin):<br/>1. GET /api/v1/balance/summary<br/>2. GET /api/v1/balance/assets<br/>3. GET /api/v1/balance/liabilities
    Interceptor->>Storage: getItem('mio_wealth_auth_token')
    Storage-->>Interceptor: Bearer Token String
    Interceptor->>Filter: HTTP GET /api/v1/balance/*<br/>Header: Authorization: Bearer <token>

    Filter->>Filter: Extracts substring after "Bearer "
    Filter->>JwtProvider: validateToken(token)
    JwtProvider->>JwtProvider: Verifies HMAC-SHA256 signature & checks exp > now()
    JwtProvider-->>Filter: true (Valid)

    Filter->>JwtProvider: getUsernameFromToken(token)
    JwtProvider-->>Filter: "alex_smith"

    Filter->>SecContext: setAuthentication(UsernamePasswordAuthenticationToken("alex_smith", ROLE_USER))
    Filter->>Ctrl: Proceeds filterChain.doFilter(request, response)

    Ctrl->>Svc: getBalanceMetrics(userId) -> calls AssetSvc and LiabSvc
    Svc-->>Ctrl: BalanceMetricsVO(totalAssets: 1795650.0, totalLiabilities: 547340.0, netWorth: 1248310.0)
    Ctrl-->>Component: HTTP 200 OK (Independent JSON endpoints)
    Component->>User: Renders Net Worth & Asset/Liability Cards with Currency Pipe
```

---

## 4. Investment Creation & Persistence

Shows how financial asset transactions are recorded with category classification and UUID generation.

```mermaid
sequenceDiagram
    autonumber
    actor User as User Browser
    participant UI as InvestmentComponent
    participant API as InvestmentController
    participant Svc as InvestmentServiceImpl
    participant UserRepo as UserRepository
    participant CatRepo as FinancialCategoryRepository
    participant InvestRepo as InvestmentRepository
    participant DB as PostgreSQL (investmanDB)

    User->>UI: Submits new holding (Symbol: INFY, Name: Infosys Ltd, Category: STOCKS, Amount: 75000, Qty: 50)
    UI->>API: POST /api/v1/investments (InvestmentRequestVO JSON)<br/>Header: Authorization: Bearer <token>
    API->>API: Extracts Principal ("alex_smith")
    API->>Svc: createInvestment("alex_smith", request)

    Svc->>UserRepo: findByUserId("alex_smith")
    UserRepo-->>Svc: User entity
    Svc->>CatRepo: findById("STOCKS")
    CatRepo-->>Svc: FinancialCategory entity

    Svc->>Svc: Instantiates new Investment(symbol: "INFY", category, amount: 75000.0, quantity: 50, tags: "#tech", user)
    Svc->>InvestRepo: save(investment)
    InvestRepo->>DB: INSERT INTO investment (investment_id, symbol, asset_name, category_code, amount, quantity, user_id, ...) VALUES (?, ?, ?, ?, ?, ?, ?, ...)
    DB-->>InvestRepo: Generated UUID v4 (investment_id)

    Svc-->>API: InvestmentVO(id: "UUID", symbol: "INFY", categoryCode: "STOCKS", amount: 75000.0, ...)
    API-->>UI: HTTP 201 Created (InvestmentVO JSON)
    UI->>User: Updates UI holdings list reactively with currency pipe formatting
```

---

## 5. Token Expiry & Automatic Session Purge

Shows the fault-tolerant session termination when a 24-hour token expires.

```mermaid
sequenceDiagram
    autonumber
    actor User as User Browser
    participant UI as Angular Dashboard
    participant Outbound as authInterceptor
    participant Filter as JwtAuthenticationFilter
    participant JwtProvider as JwtTokenProvider
    participant Inbound as errorInterceptor
    participant AuthSvc as AuthService
    participant Toast as ToastService

    User->>UI: Clicks tab or refreshes page after 24+ hours
    UI->>Outbound: Dispatches GET /api/v1/investments
    Outbound->>Filter: Authorization: Bearer <expired_token>
    Filter->>JwtProvider: validateToken(expiredToken)
    JwtProvider-->>Filter: Catches ExpiredJwtException, returns false
    Filter->>Filter: SecurityContext remains unauthenticated
    Filter-->>Inbound: HTTP 401 Unauthorized

    Inbound->>Inbound: Detects status === 401 on non-login request
    Inbound->>Toast: showToast("Your session has expired. Please sign in again.", "danger")
    Inbound->>AuthSvc: logout()
    AuthSvc->>AuthSvc: sessionStorage.clear()
    AuthSvc->>UI: router.navigate(['/login'])
    Toast-->>User: Renders red alert banner on login screen
```
