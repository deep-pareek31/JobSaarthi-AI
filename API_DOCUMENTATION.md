# JobSaarthi REST API Specification

Base URL: `https://api.jobsaarthi.com/api/v1` (or `http://localhost:8000/api/v1` locally)

All endpoints consume and produce `application/json` unless otherwise noted.
Authenticated endpoints require:
`Authorization: Bearer <access_token>`

---

## 1. Authentication Endpoints

### `POST /auth/register`
Creates a new candidate account.
- **Request Body**:
```json
{
  "email": "candidate@example.com",
  "password": "SecurePassword123!",
  "full_name": "Arjun Sharma",
  "phone": "+919876543210"
}
```
- **Response `201 Created`**:
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "c7a84518-e325-4c07-84fe-19a9d7bb34ad",
      "email": "candidate@example.com",
      "full_name": "Arjun Sharma",
      "role": "USER",
      "is_active": true
    },
    "tokens": {
      "access_token": "eyJhbGciOi...",
      "refresh_token": "eyJhbGciOi...",
      "token_type": "bearer",
      "expires_in": 3600
    }
  }
}
```

### `POST /auth/login`
Authenticates a user with email and password.
- **Request Body**:
```json
{
  "email": "candidate@example.com",
  "password": "SecurePassword123!"
}
```
- **Response `200 OK`**:
```json
{
  "success": true,
  "data": {
    "user": { ... },
    "tokens": { ... }
  }
}
```

### `POST /auth/refresh`
Refreshes an expired access token using a valid refresh token.
- **Request Body**:
```json
{
  "refresh_token": "eyJhbGciOi..."
}
```
- **Response `200 OK`**:
```json
{
  "success": true,
  "data": {
    "access_token": "eyJhbGciOi...",
    "refresh_token": "eyJhbGciOi...",
    "token_type": "bearer",
    "expires_in": 3600
  }
}
```

### `POST /auth/google`
Authenticates using an official Google Identity Services ID Token.
- **Request Body**:
```json
{
  "id_token": "eyJhbGciOi..."
}
```

### `GET /auth/me`
Retrieves authenticated candidate identity, profile, and subscription tier.

---

## 2. Jobs & Application Tracking Endpoints

### `GET /jobs/today`
Returns verified opportunities posted or renewed in the last 24 hours.

### `GET /jobs/recommended`
Returns personalized recommendations based on candidate's skills and preferences.
- Honors plan tier maximum quota (e.g. max 3 for Free tier, 20 for Pro tier).

### `GET /jobs/{id}`
Returns complete details of a job, original source disclaimer, and match explanation.

### `POST /jobs/{id}/save`
Saves a job with optional notes and priority category.

### `POST /applications`
Creates an applied job record when the candidate applies on the official source.

### `PATCH /applications/{id}`
Updates status (Applied, Assessment, Interview, Offer, Rejected, etc.).

---

## 3. Administration Endpoints (Role: `ADMIN` or `SUPERADMIN`)

### `GET /admin/source-health`
Returns live health metrics, error logs, and ingestion counts across all active connectors.

### `PATCH /admin/plans/{plan_tier}`
Updates plan pricing, limits, refresh intervals, and AI capabilities without redeploying code.
