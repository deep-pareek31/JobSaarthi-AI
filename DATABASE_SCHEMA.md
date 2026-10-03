# JobSaarthi Database Schema Specification

This document details the PostgreSQL relational schema comprising 24 tables, their constraints, foreign keys, indexes, and design rationale.

---

## 1. Table Summary & Taxonomy

| Group | Tables |
|---|---|
| **Identity & Users** | `users`, `profiles`, `admin_users`, `audit_logs` |
| **Resumes & Skills** | `resumes`, `resume_skills` |
| **Preferences & History** | `job_preferences`, `search_history`, `notification_preferences` |
| **Catalog & Ingestion** | `companies`, `job_sources`, `jobs`, `job_skills`, `job_locations`, `job_eligibility`, `job_history`, `source_health` |
| **Tracking** | `saved_jobs`, `applied_jobs`, `application_status_history` |
| **Monetization** | `subscriptions`, `subscription_events`, `subscription_plan_configs` |
| **Engagement & AI** | `notifications`, `ai_conversations` |

---

## 2. Table Schemas

### `users`
Core user identity for candidate and administrative accounts.
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    hashed_password VARCHAR(255) NULL, -- Nullable if Google OAuth only
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER', -- 'USER', 'ADMIN', 'SUPERADMIN'
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    avatar_url VARCHAR(500) NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX idx_users_email ON users(email);
```

### `profiles`
Detailed candidate education, current work status, and background.
```sql
CREATE TABLE profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    current_location VARCHAR(150) NULL,
    education_level VARCHAR(100) NULL,
    degree VARCHAR(100) NULL,
    branch VARCHAR(100) NULL,
    institution VARCHAR(255) NULL,
    graduation_year INT NULL,
    is_student BOOLEAN NOT NULL DEFAULT FALSE,
    years_experience NUMERIC(4, 1) NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
```

### `resumes` & `resume_skills`
Stores parsed resume documents, private cloud storage keys, and extracted competencies.
```sql
CREATE TABLE resumes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    storage_key VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size_bytes INT NOT NULL,
    raw_text TEXT NULL,
    parsed_json JSONB NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE resume_skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    skill_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NULL, -- 'LANGUAGE', 'FRAMEWORK', 'DATABASE', 'TOOL'
    proficiency_evidence TEXT NULL
);
```

### `job_preferences`
Manual and AI-derived discovery parameters.
```sql
CREATE TABLE job_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    preferred_roles TEXT[] NOT NULL DEFAULT '{}',
    employment_types TEXT[] NOT NULL DEFAULT '{}', -- 'INTERNSHIP', 'FULL_TIME', etc.
    preferred_locations TEXT[] NOT NULL DEFAULT '{}',
    remote_preferences TEXT[] NOT NULL DEFAULT '{}', -- 'REMOTE', 'HYBRID', 'ON_SITE'
    preferred_companies TEXT[] NOT NULL DEFAULT '{}',
    excluded_companies TEXT[] NOT NULL DEFAULT '{}',
    min_salary NUMERIC(12, 2) NULL,
    salary_currency VARCHAR(10) DEFAULT 'INR',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
```

### `companies` & `job_sources`
Tracks verified employer profiles and independent job ingestion feeds.
```sql
CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE NOT NULL,
    normalized_name VARCHAR(255) NOT NULL,
    domain VARCHAR(255) NULL,
    logo_url VARCHAR(500) NULL,
    careers_page_url VARCHAR(500) NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE job_sources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) UNIQUE NOT NULL,
    source_type VARCHAR(50) NOT NULL, -- 'OFFICIAL_COMPANY', 'OFFICIAL_ATS', 'AUTHORIZED_API', 'PUBLIC_JOB_BOARD'
    base_url VARCHAR(500) NOT NULL,
    api_endpoint VARCHAR(500) NULL,
    auth_config JSONB NULL,
    parser_config JSONB NULL,
    refresh_interval_minutes INT NOT NULL DEFAULT 60,
    rate_limit_per_minute INT NOT NULL DEFAULT 30,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    priority INT NOT NULL DEFAULT 1,
    last_successful_run TIMESTAMP WITH TIME ZONE NULL,
    last_failed_run TIMESTAMP WITH TIME ZONE NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
```

### `jobs`
Canonical job listings normalized across sources.
```sql
CREATE TABLE jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_id UUID NOT NULL REFERENCES job_sources(id) ON DELETE RESTRICT,
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    external_job_id VARCHAR(255) NOT NULL,
    title VARCHAR(300) NOT NULL,
    normalized_title VARCHAR(300) NOT NULL,
    description TEXT NOT NULL,
    normalized_description TEXT NULL,
    employment_type VARCHAR(50) NOT NULL, -- 'FULL_TIME', 'INTERNSHIP', 'CONTRACT', 'PART_TIME'
    job_category VARCHAR(100) NULL,
    department VARCHAR(100) NULL,
    location VARCHAR(200) NOT NULL,
    country VARCHAR(100) DEFAULT 'India',
    state VARCHAR(100) NULL,
    city VARCHAR(100) NULL,
    remote_type VARCHAR(50) NOT NULL DEFAULT 'ON_SITE', -- 'REMOTE', 'HYBRID', 'ON_SITE'
    experience_min NUMERIC(4, 1) NOT NULL DEFAULT 0.0,
    experience_max NUMERIC(4, 1) NULL,
    education_requirements TEXT[] DEFAULT '{}',
    degree_requirements TEXT[] DEFAULT '{}',
    branch_requirements TEXT[] DEFAULT '{}',
    graduation_year_min INT NULL,
    graduation_year_max INT NULL,
    skills TEXT[] DEFAULT '{}',
    salary_min NUMERIC(12, 2) NULL,
    salary_max NUMERIC(12, 2) NULL,
    salary_currency VARCHAR(10) DEFAULT 'INR',
    stipend NUMERIC(12, 2) NULL,
    posted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_verified_at TIMESTAMP WITH TIME ZONE NOT NULL,
    application_deadline TIMESTAMP WITH TIME ZONE NULL,
    deadline_source VARCHAR(100) NULL,
    application_url VARCHAR(1000) NOT NULL,
    source_url VARCHAR(1000) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'EXPIRED', 'CLOSED', 'REMOVED', 'UNKNOWN'
    first_seen_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_seen_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_source_external_job UNIQUE(source_id, external_job_id)
);
CREATE INDEX idx_jobs_status_posted ON jobs(status, posted_at DESC);
CREATE INDEX idx_jobs_company ON jobs(company_id);
CREATE INDEX idx_jobs_skills ON jobs USING GIN(skills);
```

### `saved_jobs` & `applied_jobs` & `application_status_history`
Application tracking and workflow history.
```sql
CREATE TABLE saved_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL DEFAULT 'Saved', -- 'Saved', 'Apply Later', 'High Priority'
    notes TEXT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_saved_job UNIQUE(user_id, job_id)
);

CREATE TABLE applied_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'Applied',
    applied_date DATE NOT NULL DEFAULT CURRENT_DATE,
    notes TEXT NULL,
    resume_used_id UUID NULL REFERENCES resumes(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_applied_job UNIQUE(user_id, job_id)
);

CREATE TABLE application_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    applied_job_id UUID NOT NULL REFERENCES applied_jobs(id) ON DELETE CASCADE,
    from_status VARCHAR(50) NOT NULL,
    to_status VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    remarks TEXT NULL
);
```

### `subscriptions` & `subscription_plan_configs`
Configurable monetization tiers.
```sql
CREATE TABLE subscription_plan_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_tier VARCHAR(50) UNIQUE NOT NULL, -- 'FREE', 'PRO_99', 'ELITE_189', 'PREMIUM_CONFIGURABLE'
    name VARCHAR(100) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    billing_cycle VARCHAR(50) NOT NULL DEFAULT 'MONTHLY',
    max_active_jobs INT NOT NULL,
    refresh_interval_days INT NOT NULL,
    ai_features_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    expanded_alerts_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_config_id UUID NOT NULL REFERENCES subscription_plan_configs(id),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'PAST_DUE', 'CANCELLED', 'EXPIRED'
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NULL,
    external_order_id VARCHAR(255) NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
```
