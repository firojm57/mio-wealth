# High-Level Architecture & System Design

This document describes the high-level system architecture, deployment topology, enterprise Schema-per-Tenant multi-tenancy, monorepo unified packaging, container virtualization, and database portability strategy for **Mio Wealth**.

---

## 1. System Architecture Overview

Mio Wealth is engineered according to the **12-Factor App** methodology and cloud-native architectural patterns. The system serves as a unified wealth and financial management platform where a client-side Single Page Application (SPA) communicates statelessly with a Java REST backend over HTTPS/HTTP, leveraging enterprise-grade schema isolation for strict tenant data segregation.

```mermaid
graph TB
    subgraph ClientBrowser["Client Tier (Web Browser / Mobile Viewport)"]
        SPA["Angular 21 Single Page Application<br/>(Signals, RxJS, Reactive Modals, i18n)"]
        Storage["Browser Session Storage<br/>(JWT Token with Tenant Claim)"]
        SPA <--> Storage
    end

    subgraph AppContainer["Docker Container: mio-wealth-app (:8080)"]
        subgraph WebServer["Embedded Apache Tomcat 10.1"]
            StaticFilter["Static Resource Handler<br/>(/index.html, *.js, *.css, /assets)"]
            SpaController["SpaController<br/>(Client-Side Route Forwarding)"]
            SecFilter["Spring Security 6 Filter Chain<br/>(JwtAuthenticationFilter & TenantContext)"]
            Dispatcher["Spring MVC DispatcherServlet"]
        end

        subgraph BusinessLayer["Application Services Tier"]
            AuthSvc["AuthService<br/>(BCrypt, Tenant Provisioning)"]
            PortfolioSvc["PortfolioService<br/>(Net Worth & Summary Metrics)"]
            AssetSvc["AssetService<br/>(Investments & Savings Assets)"]
            LiabSvc["LiabilityService<br/>(Debt CRUD)"]
            InvestSvc["InvestmentService<br/>(Full Investment CRUD)"]
            CatSvc["CategoryService<br/>(Global Catalog)"]
            UserSvc["UserService & UserProfileService"]
        end

        subgraph MultiTenancyCore["Multi-Tenancy Routing & Connection Layer"]
            TenantCtx["TenantContext<br/>(ThreadLocal Tenant ID)"]
            TenantConn["SchemaMultiTenantConnectionProvider<br/>(PostgreSQL search_path Router)"]
            TenantRes["TenantIdentifierResolver<br/>(Current Tenant Identifier)"]
            TenantProv["TenantProvisioningService<br/>(Flyway Tenant Migration Runner)"]
            HikariPool["HikariCP Connection Pool<br/>(Shared High-Performance Pool)"]
        end

        subgraph PersistenceLayer["Data Access Tier (JPA & Hibernate 6)"]
            UserRepo["UserRepository"]
            CatRepo["FinancialCategoryRepository"]
            ProfileRepo["UserProfileRepository"]
            InvestRepo["InvestmentRepository"]
            SavingRepo["SavingRepository"]
            LiabRepo["LiabilityRepository"]
            ExpRepo["ExpenseRepository"]
        end
    end

    subgraph DataTier["Database Container: mio-wealth-postgres (:5432)"]
        subgraph SharedMaster["Master Schema: public"]
            UserLoginTbl[("public.user_login<br/>(Auth & Tenant Mapping)")]
            CatTbl[("public.financial_category<br/>(Global Canonical Catalog)")]
        end

        subgraph TenantA["Tenant Schema: tenant_uuid_a"]
            InvestA[("investment")]
            SavingA[("saving")]
            LiabA[("liability")]
            ExpA[("expense")]
            ProfA[("user_profile & user_address")]
        end

        subgraph TenantB["Tenant Schema: tenant_uuid_b"]
            InvestB[("investment")]
            SavingB[("saving")]
            LiabB[("liability")]
            ExpB[("expense")]
            ProfB[("user_profile & user_address")]
        end
    end

    SPA -->|1. Static Assets GET /| StaticFilter
    SPA -->|2. SPA Route GET /investments| SpaController
    SPA -->|3. REST API Calls /api/v1/*| SecFilter
    SpaController -->|Forward| StaticFilter
    SecFilter -->|Populate TenantContext| TenantCtx
    SecFilter -->|Authenticated Context| Dispatcher
    Dispatcher --> AuthSvc
    Dispatcher --> PortfolioSvc
    Dispatcher --> AssetSvc
    Dispatcher --> LiabSvc
    Dispatcher --> InvestSvc
    Dispatcher --> CatSvc
    Dispatcher --> UserSvc
    AuthSvc --> TenantProv
    TenantCtx --> TenantRes
    TenantRes --> TenantConn
    TenantConn --> HikariPool
    HikariPool -->|SET search_path TO tenant_id, public| SharedMaster
    HikariPool --> TenantA
    HikariPool --> TenantB
```

---

## 2. Enterprise Schema-per-Tenant Multi-Tenancy

Mio Wealth implements **Schema-per-Tenant Multi-Tenancy** in PostgreSQL and Spring Boot 3.4 (Hibernate 6.6). This architecture guarantees complete physical logical data isolation between users while maintaining operational simplicity and high hardware utilization.

### Multi-Tenancy Architecture Principles:
1. **Shared Master Schema (`public`)**:
   - Contains global system entities that span across all tenants:
     - `public.user_login`: Master identity table containing credentials (BCrypt password hash), user status, role, and the assigned `tenant_schema` identifier.
     - `public.financial_category`: Canonical master catalog of investment, saving, liability, and expense categories accessible across all tenants.
2. **Isolated Per-Tenant Schemas (`tenant_<uuid>`)**:
   - Each registered user owns a private PostgreSQL schema named using a cryptographically random UUID v4 string (sanitized with alphanumeric underscores, e.g., `tenant_a1b2c3d4e5f64a1b8c2d3e4f5a6b7c8d`).
   - Tenant tables include: `user_profile`, `user_address`, `investment`, `saving`, `liability`, `expense`.
   - **Zero User ID Foreign Keys**: Because all records reside in a strictly dedicated schema, tables do not store `user_id` foreign keys. This eliminates cross-tenant data leakage vulnerabilities at the database engine level.
3. **Dynamic Connection Routing via PostgreSQL `search_path`**:
   - The application maintains a single, high-performance HikariCP connection pool.
   - When a connection is checked out for a request, [`SchemaMultiTenantConnectionProvider`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/multitenancy/SchemaMultiTenantConnectionProvider.java) dynamically executes:
     ```sql
     SET search_path TO "tenant_1234567890", public;
     ```
   - On connection release, `search_path` is safely reverted to `public;`.
   - Resolving against `"tenant_...", public` allows queries in tenant tables to seamlessly reference `public.financial_category` without cross-schema qualification overhead.
4. **Automated Tenant Lifecycle & Provisioning**:
   - **User Registration**: [`TenantProvisioningService`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/multitenancy/TenantProvisioningService.java) executes `CREATE SCHEMA IF NOT EXISTS "<schema>"` and programmatically runs Flyway migrations from `classpath:db/migration/tenants` on the new schema.
   - **Session Expiry & Re-Authentication**: The tenant schema assignment is permanent in `public.user_login`. When a user logs in after token expiration, the server issues a new JWT containing the existing tenant claim, connecting them immediately to their persistent schema.

---

## 3. Monorepo Unified Fat JAR Packaging

The application utilizes a **Unified Deployment Artifact** approach. Both the Angular frontend (`imui`) and the Spring Boot backend (`server`) reside in the same monorepo and compile into a single executable JAR (`investman-0.0.1-SNAPSHOT.jar`).

### Benefits of the Unified Model:
1. **Zero CORS Overheads**: The Angular SPA and REST API share the identical origin (`http://host:8080`). Browsers execute direct requests without triggering cross-origin preflight `OPTIONS` traffic.
2. **Atomic Versioning**: UI and API are deployed simultaneously. There is zero risk of frontend-backend version mismatch or breaking contract drift.
3. **Operational Simplicity**: Deployment targets (Docker, AWS ECS, GCP Cloud Run, Kubernetes) manage a single container image.

```mermaid
graph LR
    subgraph BuildStage1["Stage 1: Frontend Build (Node.js 22)"]
        A1["imui/src & dependencies"] -->|npm run build| A2["Compiled Static Assets<br/>(dist/imui/browser/)"]
    end

    subgraph BuildStage2["Stage 2: Backend Build (Maven & JDK 21)"]
        B1["server/src & pom.xml"]
        A2 -->|Copied into| B2["src/main/resources/static/"]
        B1 --> B3["Maven Package"]
        B2 --> B3
        B3 -->|Generates| B4["Executable Fat JAR<br/>(investman-0.0.1-SNAPSHOT.jar)"]
    end

    subgraph BuildStage3["Stage 3: Hardened Runtime (JRE 21)"]
        C1["Alpine Linux + Eclipse Temurin JRE 21"]
        B4 -->|COPY --from=backend-builder| C2["Non-root user 'appuser'<br/>/app/app.jar"]
    end

    BuildStage1 --> BuildStage2 --> BuildStage3
```

---

## 4. Docker Infrastructure & Networking

The system is defined as an infrastructure-as-code deployment via `docker-compose.yml` operating on an isolated bridge network `mio-wealth-net`.

```mermaid
graph TB
    subgraph HostSystem["Host Operating System (Windows / Linux / WSL2)"]
        UserBrowser["Client Web Browser<br/>http://localhost:8080"]
        DBTool["Database Tool (DBeaver / psql)<br/>localhost:5432"]

        subgraph BridgeNetwork["Docker Network: mio-wealth-net (bridge)"]
            subgraph AppNode["Container: mio-wealth-app"]
                App["app.jar<br/>Port: 8080<br/>User: appuser (non-root)"]
            end

            subgraph DBNode["Container: mio-wealth-postgres"]
                DB["PostgreSQL 16 Alpine<br/>Port: 5432<br/>Database: investmanDB"]
                HealthCheck["Healthcheck: pg_isready<br/>Interval: 5s, Retries: 5"]
            end
        end

        subgraph VolumeStorage["Host Persistent Storage"]
            PersistVol[("Named Volume: postgres_data<br/>/var/lib/postgresql/data")]
        end
    end

    UserBrowser -->|Port 8080:8080| App
    DBTool -->|Port 5432:5432| DB
    App -->|Depends on DB healthy<br/>jdbc:postgresql://postgres:5432/investmanDB| DB
    DB --- PersistVol
```

### Key Infrastructure Specifications:
* **Container Hardening**: The application runs under a dedicated, unprivileged POSIX user (`appuser`, group `appgroup`).
* **Service Dependency Healthcheck**: The application container (`mio-wealth-app`) uses `depends_on: postgres: condition: service_healthy` ensuring Spring Boot only attempts database initialization after PostgreSQL is verified ready via `pg_isready`.
* **Data Durability**: Relational state is stored in the external volume `postgres_data`, surviving container teardown, rebuilds, and upgrades.

---

## 5. Detachable Multi-Database Architecture

Mio Wealth supports **database detachability**: the ability to swap the backing database engine (PostgreSQL, MySQL, MariaDB, or H2) without modifying source code.

```mermaid
graph TD
    JPA["Spring Data JPA Repositories & Entities"] --> Hibernate["Hibernate ORM 6.6 Dialect Engine"]

    subgraph EnvConfig["Runtime Environment Injection"]
        URL["SPRING_DATASOURCE_URL"]
        DRIVER["SPRING_DATASOURCE_DRIVER"]
        USER["SPRING_DATASOURCE_USERNAME"]
        PASS["SPRING_DATASOURCE_PASSWORD"]
    end

    EnvConfig --> Hibernate

    subgraph SupportedEngines["Detachable Supported Engines"]
        PG["PostgreSQL (Default & Multi-Tenant)<br/>Driver: org.postgresql.Driver<br/>URL: jdbc:postgresql://..."]
        MY["MySQL / MariaDB<br/>Driver: com.mysql.cj.jdbc.Driver<br/>URL: jdbc:mysql://..."]
        H2["In-Memory H2 (Testing)<br/>Driver: org.h2.Driver<br/>URL: jdbc:h2:mem:..."]
    end

    Hibernate --> PG
    Hibernate --> MY
    Hibernate --> H2
```

### Portability Guarantees in Code:
1. **Vendor-Agnostic ID Generation**: All JPA entities declare `@GeneratedValue(strategy = GenerationType.UUID)` or natural keys, ensuring compatibility across relational database engines.
2. **Modular Flyway Migrations**:
   - `classpath:db/migration/shared`: Master shared catalog and login schemas.
   - `classpath:db/migration/tenants`: Modular, tenant-scoped schema definitions executed dynamically upon new tenant provisioning.
3. **Driver-Agnostic External Configuration**: Datasource driver classes, JDBC URLs, and credentials in `application.yml` are 100% externalized via standard Spring environment variables with zero hardcoding.

---

## 6. Single Page Application (SPA) Forwarding Engine

Because Angular utilizes HTML5 PushState routing (clean URLs like `/overview`, `/investments`, `/balance` without hash fragments), direct navigation or browser page reloads must not result in HTTP 404 Not Found from the embedded server.

```mermaid
sequenceDiagram
    autonumber
    actor User as Client Browser
    participant Tomcat as Embedded Tomcat
    participant SpaCtrl as SpaController
    participant StaticHandler as ResourceHttpRequestHandler
    participant Angular as Angular Router (Client)

    User->>Tomcat: GET /investments (Page Refresh)
    Tomcat->>SpaCtrl: Matches @GetMapping("/investments")
    SpaCtrl-->>Tomcat: Returns "forward:/index.html"
    Tomcat->>StaticHandler: Resolves /static/index.html
    StaticHandler-->>User: HTTP 200 OK (index.html + JS scripts)
    User->>Angular: Angular boots in browser
    Angular->>Angular: Inspects window.location.pathname ("/investments")
    Angular-->>User: Renders InvestmentComponent view
```

### Controller Implementation ([`SpaController.java`](file:///d:/F_Drive/github/mio-wealth/server/src/main/java/com/greenboard/investman/controller/spa/SpaController.java)):
```java
@Controller
public class SpaController {
    @GetMapping(value = {
        "/", "/overview", "/investments", "/balance", 
        "/cash-flow", "/expenses", "/performance", 
        "/goals", "/settings", "/login", "/signup"
    })
    public String forwardToSpa() {
        return "forward:/index.html";
    }
}
```
All client-side route entries are intercepted and transparently dispatched to `/index.html` server-side, enabling full deep-linking and browser navigation resilience.
