# End-to-End Sequence & Workflow Diagrams

This document illustrates the step-by-step runtime flows across presentation, security, multi-tenancy routing, business service, and database tiers using Mermaid sequence diagrams.

---

## 1. User Self-Registration & Tenant Provisioning Workflow

Illustrates the flow when a user creates an account from [`/signup`](file:///d:/F_Drive/github/mio-wealth/imui/src/app/components/signup/signup.component.ts), including dynamic schema provisioning and Flyway migration.

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
    participant ProvSvc as TenantProvisioningService
    participant Flyway as Flyway Migration Engine
    participant ProfileRepo as UserProfileRepository
    participant DB as PostgreSQL (investmanDB)

    User->>UI: Fills form (username, password, name, email) & clicks "Create Account"
    UI->>AuthSvcUI: register(userRegisterRequest)
    AuthSvcUI->>API: POST /api/v1/auth/register (UserRegisterVO JSON)
    API->>Svc: register(request)
    Svc->>UserRepo: existsByUserId(request.getUserId())
    UserRepo->>DB: SELECT count(*) FROM public.user_login WHERE user_id = ?
    DB-->>UserRepo: 0 (Username available)

    Svc->>Svc: Generates tenantSchema = "tenant_" + UUIDv4
    Svc->>Encrypt: encode(rawPassword)
    Encrypt-->>Svc: BCrypt hash string ($2a$10$...)

    Svc->>UserRepo: save(User with tenantSchema)
    UserRepo->>DB: INSERT INTO public.user_login (id, user_id, password, tenant_schema) VALUES (?, ?, ?, ?)

    Svc->>ProvSvc: provisionTenant(tenantSchema)
    ProvSvc->>DB: CREATE SCHEMA IF NOT EXISTS "tenant_uuid"
    ProvSvc->>Flyway: Programmatically execute migrations from db/migration/tenants
    Flyway->>DB: Creates user_profile, user_address, investment, saving, liability, expense in tenant schema

    Svc->>ProvSvc: initTenantProfile(tenantSchema, firstName, ...)
    ProvSvc->>DB: INSERT INTO "tenant_uuid".user_profile (...) VALUES (...)

    Svc-->>API: StatusVO(status: "Success")
    API-->>AuthSvcUI: HTTP 201 Created (StatusVO JSON)
    AuthSvcUI-->>UI: Observable completes successfully
    UI->>UI: Displays green toast: "Account created successfully!"
    UI->>User: Navigates router to /login
```

---

## 2. Authentication & Multi-Tenant Token Issuance

Illustrates credential validation against `public.user_login`, tenant schema extraction, and JWT issuance with embedded `tenant` claim.

```mermaid
sequenceDiagram
    autonumber
    actor User as User Browser
    participant UI as LoginComponent
    participant AuthSvcUI as Angular AuthService
    participant API as AuthController
    participant Svc as AuthServiceImpl
    participant UserRepo as UserRepository
    participant Encrypt as BCryptPasswordEncoder
    participant JwtProvider as JwtTokenProvider
    participant ProvSvc as TenantProvisioningService
    participant Storage as Browser sessionStorage

    User->>UI: Enters username & password, clicks "Sign In"
    UI->>AuthSvcUI: login({ userId, password })
    AuthSvcUI->>API: POST /api/v1/auth/login (AuthRequestVO JSON)
    API->>Svc: login(request)
    Svc->>UserRepo: findByUserId(request.getUserId())
    UserRepo-->>Svc: User entity (with BCrypt hash & tenantSchema)

    Svc->>Encrypt: matches(rawPassword, entityPasswordHash)
    Encrypt-->>Svc: true (Valid password)

    Svc->>JwtProvider: generateToken(userId, user.getTenantSchema())
    JwtProvider->>JwtProvider: Builds JWT with subject: userId, tenant: tenantSchema, exp: +24h
    JwtProvider-->>Svc: Compact signed JWT token string

    Svc->>ProvSvc: getTenantProfile(user.getTenantSchema())
    ProvSvc->>DB: SELECT first_name, last_name, email_id FROM "tenant_uuid".user_profile LIMIT 1
    DB-->>ProvSvc: UserProfileVO
    ProvSvc-->>Svc: UserProfileVO
    Svc-->>API: AuthResponseVO(token, userId, userProfileVO)
    API-->>AuthSvcUI: HTTP 200 OK (AuthResponseVO JSON)

    AuthSvcUI->>Storage: sessionStorage.setItem('mio_wealth_auth_token', token)
    AuthSvcUI->>Storage: sessionStorage.setItem('mio_wealth_user_profile', profile)
    AuthSvcUI-->>UI: Login Observable succeeds
    UI->>User: Navigates to /overview dashboard
```

---

## 3. Authenticated Request with Dynamic Schema Routing

Demonstrates how [`JwtAuthenticationFilter`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/security/JwtAuthenticationFilter.java) populates [`TenantContext`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/multitenancy/TenantContext.java) and [`SchemaMultiTenantConnectionProvider`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/multitenancy/SchemaMultiTenantConnectionProvider.java) routes database queries to the tenant schema.

```mermaid
sequenceDiagram
    autonumber
    actor User as User Browser
    participant Component as InvestmentComponent
    participant Interceptor as authInterceptor
    participant Storage as sessionStorage
    participant Filter as JwtAuthenticationFilter
    participant JwtProvider as JwtTokenProvider
    participant TenantCtx as TenantContext
    participant Ctrl as InvestmentController
    participant Svc as InvestmentServiceImpl
    participant ConnProvider as SchemaMultiTenantConnectionProvider
    participant DB as PostgreSQL (investmanDB)

    Component->>Interceptor: Dispatches GET /api/v1/investments
    Interceptor->>Storage: getItem('mio_wealth_auth_token')
    Storage-->>Interceptor: Bearer Token String
    Interceptor->>Filter: HTTP GET /api/v1/investments<br/>Header: Authorization: Bearer <token>

    Filter->>JwtProvider: validateToken(token)
    JwtProvider-->>Filter: true (Valid)
    Filter->>JwtProvider: getTenantFromToken(token)
    JwtProvider-->>Filter: "tenant_a1b2c3"
    Filter->>TenantCtx: setTenantId("tenant_a1b2c3")
    Filter->>Filter: Sets SecurityContextHolder Authentication

    Filter->>Ctrl: filterChain.doFilter(...)
    Ctrl->>Svc: getInvestments() [Zero manual userId parameters!]

    Svc->>ConnProvider: Requests connection for tenant "tenant_a1b2c3"
    ConnProvider->>DB: SET search_path TO "tenant_a1b2c3", public;
    Svc->>DB: SELECT * FROM investment
    DB-->>Svc: List<Investment> records from tenant schema
    ConnProvider->>DB: SET search_path TO public; (on release)

    Svc-->>Ctrl: List<InvestmentVO>
    Ctrl-->>Component: HTTP 200 OK (JSON array)
    Filter->>TenantCtx: clear() [Executed in finally block]
    Component->>User: Renders Investment holdings table with pure Angular currency formatting
```

---

## 4. Investment Full CRUD Workflow

Illustrates the lifecycle of creating, updating, and deleting investment holdings with modal dialogs, category selection, and toast notifications.

```mermaid
sequenceDiagram
    autonumber
    actor User as User
    participant UI as InvestmentComponent
    participant Modal as Add/Edit Modal
    participant DelModal as Delete Modal
    participant SvcUI as InvestmentService (Angular)
    participant API as InvestmentController
    participant Svc as InvestmentServiceImpl
    participant DB as PostgreSQL (Tenant Schema)
    participant Toast as ToastService

    Note over User,Toast: 1. Create Investment
    User->>UI: Clicks "+ Add Transaction"
    UI->>Modal: Opens modal with dynamic category select from CategoryService
    User->>Modal: Fills Symbol (AAPL), Name, Category (STOCKS), Amount (150000), Qty (10)
    Modal->>UI: Triggers saveInvestment()
    UI->>SvcUI: addInvestment(formData) -> returns Observable<InvestmentHolding>
    SvcUI->>API: POST /api/v1/investments (InvestmentRequestVO)
    API->>Svc: createInvestment(request)
    Svc->>DB: INSERT INTO investment (...) VALUES (...)
    DB-->>Svc: Saved Investment with UUID
    Svc-->>API: InvestmentVO
    API-->>SvcUI: HTTP 201 Created
    SvcUI-->>UI: Observable emits
    UI->>Toast: showToast("Investment added successfully.", "success")
    UI->>SvcUI: loadHoldings() re-fetch

    Note over User,Toast: 2. Update Investment
    User->>UI: Clicks Edit icon on holding row
    UI->>Modal: Pre-populates form with current holding data
    User->>Modal: Modifies amount/quantity and clicks Update
    UI->>SvcUI: updateInvestment(id, formData)
    SvcUI->>API: PUT /api/v1/investments/{id}
    API->>Svc: updateInvestment(id, request)
    Svc->>DB: UPDATE investment SET ... WHERE id = ?
    Svc-->>API: Updated InvestmentVO
    API-->>SvcUI: HTTP 200 OK
    UI->>Toast: showToast("Investment updated successfully.", "success")

    Note over User,Toast: 3. Delete Investment (Accessible Modal)
    User->>UI: Clicks Trash icon on holding row
    UI->>DelModal: Opens accessible custom delete confirmation modal
    User->>DelModal: Clicks "Delete" button
    DelModal->>SvcUI: deleteInvestment(id)
    SvcUI->>API: DELETE /api/v1/investments/{id}
    API->>Svc: deleteInvestment(id)
    Svc->>DB: DELETE FROM investment WHERE id = ?
    API-->>SvcUI: HTTP 204 No Content
    UI->>Toast: showToast("Investment removed successfully.", "success")
    UI->>SvcUI: loadHoldings() re-fetch
```

---

## 5. Token Expiration & Session Invalidation

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

    Inbound->>Inbound: Detects status === 401 on protected endpoint
    Inbound->>Toast: showToast("Your session has expired. Please sign in again.", "danger")
    Inbound->>AuthSvc: logout()
    AuthSvc->>AuthSvc: sessionStorage.clear()
    AuthSvc->>UI: router.navigate(['/login'])
    Toast-->>User: Renders red alert banner on login screen
```
