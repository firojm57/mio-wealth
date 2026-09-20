# Security, Authentication & Session Architecture

This document provides a comprehensive analysis of the security architecture implemented in **Mio Wealth**, covering Spring Security 6, JWT token mechanics, password hashing, stateless sessions, and frontend session recovery.

---

## 1. Spring Security 6 Stateless Filter Chain

Mio Wealth uses Spring Security 6 configured with **stateless session creation** (`SessionCreationPolicy.STATELESS`). The server does not allocate or maintain HTTP sessions (`HttpSession`) in memory; all authentication state is carried within signed JSON Web Tokens (JWT).

```mermaid
graph TD
    ClientReq["Incoming HTTP Request"] --> CorsF["1. CorsFilter<br/>(Allowed Origins, Methods, Headers)"]
    CorsF --> CsrfF["2. CsrfFilter (Disabled for Stateless API)"]
    CsrfF --> JwtF["3. JwtAuthenticationFilter<br/>Extracts Bearer Token from Authorization Header"]

    JwtF --> TokenCheck{Valid & Non-Expired JWT?}

    TokenCheck -->|Yes| SetContext["SecurityContextHolder.setAuthentication()<br/>Principal: userId, Role: ROLE_USER"]
    TokenCheck -->|No / Missing| NoAuth["SecurityContextHolder remains anonymous"]

    SetContext --> AuthFilter["4. AuthorizationFilter<br/>Inspects SecurityContext vs Endpoint Rules"]
    NoAuth --> AuthFilter

    AuthFilter --> MatchRules{Endpoint Rule}

    MatchRules -->|PermitAll| Dispatch["Forward to Controller / DispatcherServlet"]
    MatchRules -->|Authenticated & Principal Present| Dispatch
    MatchRules -->|Authenticated & Anonymous| Reject401["HTTP 401 Unauthorized Response"]

    Dispatch --> ControllerResponse["Controller Execution & JSON Response"]
```

---

## 2. JWT (JSON Web Token) Lifecycle & Specification

* **Implementation**: [`JwtTokenProvider.java`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/security/JwtTokenProvider.java)
* **Cryptographic Algorithm**: **HMAC-SHA256** using an externalized 256-bit secret key.
* **Token Structure**:
  * **Header**: `{"alg": "HS256", "typ": "JWT"}`
  * **Payload Claims**:
    * `sub` (Subject): The unique `userId`
    * `iat` (Issued At): Timestamp of issuance
    * `exp` (Expiration): Issued timestamp + expiration window (Default: 24 hours / `86,400,000 ms`)
  * **Signature**: `HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)`

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client (Angular)
    participant AuthCtrl as AuthController
    participant AuthSvc as AuthServiceImpl
    participant JwtProvider as JwtTokenProvider
    participant ResourceCtrl as Protected Controller (e.g. BalanceController)

    Note over Client,ResourceCtrl: Phase 1: Authentication & Token Issuance
    Client->>AuthCtrl: POST /api/v1/auth/login { userId, password }
    AuthCtrl->>AuthSvc: login(request)
    AuthSvc->>AuthSvc: Verify BCrypt password hash
    AuthSvc->>JwtProvider: generateToken(userId)
    JwtProvider-->>AuthSvc: Signed JWT string
    AuthSvc-->>AuthCtrl: AuthResponseVO(token, userId, profile)
    AuthCtrl-->>Client: HTTP 200 OK + JWT Token
    Client->>Client: Stores token in browser sessionStorage

    Note over Client,ResourceCtrl: Phase 2: Authenticated API Access
    Client->>ResourceCtrl: GET /api/v1/balance<br/>Header: Authorization: Bearer <token>
    ResourceCtrl->>JwtProvider: validateToken(token)
    alt Token is Valid
        JwtProvider-->>ResourceCtrl: true
        ResourceCtrl-->>Client: HTTP 200 OK (Financial Data)
    else Token Expired or Invalid Signature
        JwtProvider-->>ResourceCtrl: Throws ExpiredJwtException
        ResourceCtrl-->>Client: HTTP 401 Unauthorized
    end
```

---

## 3. Endpoint Access Control Matrix

Declared inside [`SecurityConfiguration.java`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/SecurityConfiguration.java):

| Route / URI Pattern | HTTP Methods | Access Level | Description |
| :--- | :--- | :--- | :--- |
| `/api/v1/auth/**` | `POST` | `permitAll()` | Public self-registration (`/register`) and sign in (`/login`). |
| `/swagger-ui/**`, `/v3/api-docs/**` | `GET` | `permitAll()` | Interactive OpenAPI 3 / Swagger documentation discovery. |
| `/`, `/index.html`, `/favicon.ico` | `GET` | `permitAll()` | Static frontend bootstrapping assets. |
| `/*.js`, `/*.css`, `/assets/**`, `/static/**` | `GET` | `permitAll()` | Angular compiled JavaScript, styles, fonts, and SVG icons. |
| `/overview`, `/investments`, `/balance`, `/cash-flow`, `/expenses`, `/performance`, `/goals`, `/settings`, `/login`, `/signup` | `GET` | `permitAll()` | Client-side SPA routes forwarded server-side to `index.html`. |
| `/api/v1/categories/**` | `GET` | `authenticated()` | Database-backed category catalog partitioned by domain. |
| `/api/v1/users/**` | `GET` | `authenticated()` | Protected investor user profile (`/profile`). |
| `/api/v1/investments/**` | `GET`, `POST` | `authenticated()` | Protected user portfolio holdings and investment creation. |
| `/api/v1/balance/**` | `GET`, `POST`, `DELETE` | `authenticated()` | Protected portfolio summary, assets, liabilities CRUD. |
| `/api/v1/savings/**`, `/api/v1/portfolio/**` | Any | `authenticated()` | Reserved protected financial calculation endpoints. |
| Any other request | Any | `authenticated()` | Zero-trust default: all unlisted routes require authentication. |

---

## 4. Password Encryption Architecture

Passwords stored in `user_login.password` are protected using **BCrypt** with an adaptive work factor (cost 10).

### Smooth Migration Support in [`AuthServiceImpl.java`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/service/auth/impl/AuthServiceImpl.java#L43-L49):
```java
// Supports BCrypt-hashed passwords, while smoothly migrating legacy accounts
boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword())
        || request.getPassword().equals(user.getPassword());

if (!matches) {
    throw new BadCredentialsException("Invalid user ID or password.");
}
```
* **New accounts**: During registration (`/api/v1/auth/register`), all passwords are encrypted with `passwordEncoder.encode(request.getPassword())` before being persisted.
* **Legacy accounts**: Existing accounts created before BCrypt migration are recognized, validated, and upgrade-ready without invalidating user passwords.

---

## 5. Cross-Origin Resource Sharing (CORS) & CSRF Policy

### CORS Configuration:
Externalized via the `CORS_ALLOWED_ORIGINS` environment variable (default: `http://localhost:8080,http://localhost:4200`):
* **Allowed Methods**: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`, `PATCH`
* **Allowed Headers**: `*` (including `Authorization`, `Content-Type`)
* **Credentials Allowed**: `true`
* **Preflight Max Age**: `3600` seconds (1 hour browser cache for preflight checks)

### CSRF Strategy:
Cross-Site Request Forgery (CSRF) protection is **explicitly disabled** (`csrf(AbstractHttpConfigurer::disable)`).
* **Rationale**: CSRF attacks rely on browser ambient credentials (automatic cookie submission). Because Mio Wealth uses stateless Bearer tokens stored in client `sessionStorage` and sent via the `Authorization` header, the browser does not transmit authentication state implicitly, making classic CSRF vectors inapplicable.

---

## 6. Client-Side Session Recovery & Expiration Interception

When a JWT token reaches its expiration threshold (24 hours), the client smoothly intercepts the failure and sanitizes its local storage.

```mermaid
sequenceDiagram
    autonumber
    actor User as User
    participant AngularApp as Angular SPA
    participant AuthInterceptor as authInterceptor
    participant Server as Spring Boot API
    participant ErrorInterceptor as errorInterceptor
    participant AuthService as AuthService
    participant Toast as ToastService

    User->>AngularApp: Opens dashboard tab after 24+ hours
    AngularApp->>AuthInterceptor: Dispatches GET /api/v1/investments
    AuthInterceptor->>Server: Adds Header: Authorization: Bearer <expired_token>
    Server-->>ErrorInterceptor: HTTP 401 Unauthorized (ExpiredJwtException)
    ErrorInterceptor->>AuthService: authService.logout()
    AuthService->>AuthService: sessionStorage.removeItem(TOKEN_KEY)<br/>sessionStorage.removeItem(USER_KEY)
    AuthService->>AngularApp: Redirects router to /login
    ErrorInterceptor->>Toast: showToast("Your session has expired. Please sign in again.", "danger")
    Toast-->>User: Displays red financial security alert banner
```
* **Storage Isolation**: Tokens are stored in `sessionStorage` rather than `localStorage`, preventing tokens from persisting indefinitely or being shared across disparate browser windows.
