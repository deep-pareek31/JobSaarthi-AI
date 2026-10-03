# JobSaarthi Architecture Design Document

## 1. System Overview

JobSaarthi is structured as a scalable, distributed SaaS platform designed to ingest hundreds of thousands of job opportunities across legitimate sources, normalize them, detect duplicates and expirations, and match them deterministically against user profiles.

```
       +----------------------------+     +----------------------------+
       |   Android Client (Kotlin)  |     | Next.js / Web / Admin Dash |
       +--------------+-------------+     +--------------+-------------+
                      |                                  |
                      | HTTPS / REST (JWT Bearer Token)  |
                      v                                  v
       +---------------------------------------------------------------+
       |             Reverse Proxy / Load Balancer (NGINX / Cloud Run) |
       +-------------------------------+-------------------------------+
                                       |
                                       v
       +---------------------------------------------------------------+
       |              JobSaarthi FastAPI Core Backend API             |
       |  - Authentication & RBAC        - Matching Scoring Engine     |
       |  - User & Profile Management     - Search & Filter Engine      |
       |  - Resume Analysis Broker        - Subscription Entitlement    |
       |  - Application Tracking Hub      - Admin Telemetry & Control   |
       +-------------------------------+-------------------------------+
                  |                    |                    |
                  | SQLAlchemy 2.0     | Redis Cache        | Async Celery Tasks
                  v                    v                    v
         +-----------------+   +----------------+   +--------------------+
         |   PostgreSQL    |   | Redis (Broker, |   | Ingestion Workers  |
         | Relational DB   |   | Cache, Token   |   | - Greenhouse ATS   |
         | (24 Core Tables)|   | Revocation)    |   | - Lever ATS        |
         +-----------------+   +----------------+   | - Company Portals  |
                                                    | - Expiration Engine|
                                                    +--------------------+
```

## 2. Core Subsystems

### A. Client Layer
- **Android**: Native Kotlin + Jetpack Compose application following MVVM and Clean Architecture. Communicates with the FastAPI backend over secure TLS, storing tokens in EncryptedSharedPreferences and caching user data locally in Room.
- **Web & Admin Portal**: Responsive Next.js application providing candidates with desktop discovery and system administrators with live telemetry, connector controls, and plan pricing management.

### B. API & Business Logic Layer (FastAPI)
- **High Concurrency**: Asynchronous I/O utilizing `asyncpg` and Uvicorn workers.
- **Strict Data Validation**: Pydantic v2 schemas enforce input sanitization and strict API contracts.
- **Token Security**: Dual-token architecture (short-lived Access Token + long-lived Refresh Token with Redis-backed revocation).

### C. Ingestion & Worker Subsystem (Celery + Redis)
- **Decoupled Workers**: Crawling and ingestion tasks run outside of the web server loop to protect API latency.
- **Rate-Limiting & Politeness**: Each connector enforces per-source rate limits and respects HTTP 429 backoff headers.
- **Deduplication Engine**: Calculates normalized hashes over `(company_id, normalized_title, location_hash)` and computes text similarity before storing duplicates as canonical references.

### D. Data Persistence (PostgreSQL)
- 24 relational tables with B-Tree and GIN indexes for rapid text search, foreign keys with appropriate cascade rules, and full audit logs for administrative accountability.
