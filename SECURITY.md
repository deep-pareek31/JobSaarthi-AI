# JobSaarthi Security Architecture & Hardening

## 1. Authentication & Session Security
- **Bcrypt Hashing**: Passwords are salted and hashed with 12 work rounds (`passlib[bcrypt]`).
- **Dual JWT Lifecycle**:
  - Access Token: Short-lived (60 minutes), signed with `HS256` or `RS256`.
  - Refresh Token: Long-lived (30 days), stored with rotation and unique token ID (`jti`) tracked in Redis for immediate revocation on logout or account compromise.
- **Role-Based Access Control (RBAC)**: Strictly enforced via FastAPI dependencies (`require_admin`, `require_superadmin`).
- **Encrypted Local Storage**: On Android, credentials and tokens are stored using Android Keystore and EncryptedSharedPreferences.

## 2. Injection & Transport Hardening
- **SQL Injection Prevention**: Completely mitigated via parameterized SQLAlchemy ORM queries and Pydantic schema validation.
- **XSS Prevention**: All frontend inputs are encoded and sanitized; responses use `Content-Type: application/json` and strict Content-Security-Policy (CSP) headers.
- **Zero Secrets in Clients**: Android and Web codebases contain no API keys, database credentials, or payment secrets. All external integrations are proxied through the authenticated backend.

## 3. Resume Privacy & File Upload Security
- **File Validation**: Strict MIME-type inspection (magic byte check for PDF and DOCX), maximum file size enforcement (5 MB default).
- **Private Cloud Storage**: Resumes are uploaded to private buckets with no public read permissions. Download access requires short-lived (15-minute) pre-signed URLs generated server-side for the owning candidate only.
- **Audit Trails**: Security events (logins, failed logins, admin configuration changes, resume access) are written to the immutable `audit_logs` table.
