# JobSaarthi (रोज़गार साथी) 🧭

> **Enterprise-Grade AI-Powered Job Discovery and Application Tracking Platform**

JobSaarthi is an intelligent, high-throughput career discovery ecosystem that eliminates the noise of modern job hunting. Instead of manually scouring dozens of company career pages and ATS feeds, JobSaarthi continuously monitors, verifies, and normalizes openings directly from legitimate sources (official company portals, Greenhouse, Lever, and authorized feeds), evaluates profile compatibility with deep explainability ("Why you match" vs "Possible skill gaps"), and tracks applications from discovery to offer.

---

## 🏗️ System Architecture & Interfaces

The platform consists of four interconnected subsystems:

1. **Android Mobile Application (`/app`)**: Built with Jetpack Compose, Kotlin Coroutines, Navigation Compose, Room for offline caching, and Retrofit with automatic JWT refresh.
2. **FastAPI Shared Backend (`/backend`)**: High-performance asynchronous REST API powered by SQLAlchemy 2.0, PostgreSQL, Pydantic v2, and secure JWT authentication.
3. **Background Ingestion & Expiration Workers (`/workers`)**: Celery + Redis scheduler managing source connectors (Greenhouse, Lever, etc.), deduplication pipelines, and automated deadline/expiration detection.
4. **Responsive Web Application & Admin Control Panel (`/web` & `/admin`)**: Next.js + TypeScript SaaS application with real-time source health monitoring, dynamic subscription plan management, and analytics.

---

## 🚀 Phase 1 Implementation Status

- [x] **Enterprise System Architecture**: Microservices-ready modular monolith with clean separation between Android, Web, Backend, Workers, Database, and Admin.
- [x] **Complete PostgreSQL Relational Schema**: 24 normalized tables supporting users, resumes, skills, jobs, sources, applications, history, subscriptions, and audit logs.
- [x] **Authentication Engine**:
  - Secure bcrypt password hashing
  - JWT token pair generation (Access + Refresh token rotation)
  - Role-Based Access Control (`USER`, `ADMIN`, `SUPERADMIN`)
  - Google OAuth token verification adapter
  - Password reset workflows with secure hashed tokens
  - Audit logging for authentication events
- [x] **Android Client Foundation**:
  - Android Material 3 theme & custom adaptive brand launcher icon
  - MVVM Clean Architecture
  - Room local database (`UserDao`, `AppDatabase`)
  - Network layer with Retrofit, OkHttp, and automatic Bearer `AuthInterceptor`
  - Authentication UI (Login, Registration, Password Reset Dialog, Dashboard)
- [x] **Developer Operations & Testing**:
  - Complete `.env.example`
  - Automated tests for authentication, tokens, and RBAC
  - Docker Compose configuration for PostgreSQL, Redis, Backend, and Workers

---

## 💻 Quick Start: Running Locally

### Prerequisites
- Python 3.11+
- Node.js 18+ (for Web/Admin)
- Android Studio Ladybug or later (for Android app)
- Docker & Docker Compose (for PostgreSQL & Redis)

### 1. Start Infrastructure (PostgreSQL & Redis)
```bash
cd backend
docker-compose up -d postgres redis
```

### 2. Configure Environment
```bash
cp ../.env.example .env
# Edit .env with your local settings if needed
```

### 3. Run Backend (FastAPI)
```bash
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# Run database migrations / table creation
python -m app.core.database

# Launch FastAPI server
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```
API Documentation will be live at: `http://localhost:8000/docs`

### 4. Run Backend Tests
```bash
pytest app/tests/ -v
```

### 5. Launch Android Application
Open the project in Android Studio or compile via:
```bash
gradle :app:assembleDebug
```
