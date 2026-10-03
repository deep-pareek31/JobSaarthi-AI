# JobSaarthi Testing Strategy

## 1. Test Layers

| Tier | Tooling | Focus |
|---|---|---|
| **Unit Tests** | `pytest`, `pytest-asyncio` | Password hashing, token generation, matching engine weights, deduplication hashing |
| **API Integration** | `TestClient` (FastAPI), in-memory SQLite / test Postgres | Auth registration, login, refresh, permissions, rate limiting |
| **Database Tests** | SQLAlchemy transaction rollbacks | Foreign key cascades, constraint validation, JSONB serialization |
| **Android Tests** | Robolectric, Compose Test | Token manager, ViewModel state transitions, UI rendering |

## 2. Running Backend Tests

```bash
cd backend
pytest app/tests/ -v
```

## 3. Running Android JVM Unit & Robolectric Tests

```bash
gradle :app:testDebugUnitTest
```
