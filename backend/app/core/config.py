import os
from typing import List, Union
from pydantic import AnyHttpUrl, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore"
    )

    APP_NAME: str = "JobSaarthi"
    APP_ENV: str = "development"
    DEBUG: bool = True
    PORT: int = 8000
    HOST: str = "0.0.0.0"
    API_V1_PREFIX: str = "/api/v1"

    # CORS
    CORS_ORIGINS: List[str] = [
        "http://localhost:3000",
        "http://localhost:8000",
        "http://10.0.2.2:8000",
        "*"
    ]

    # Database
    DATABASE_URL: str = "sqlite:///./jobsaarthi_local.db"
    DATABASE_POOL_SIZE: int = 20
    DATABASE_MAX_OVERFLOW: int = 10

    # Redis
    REDIS_URL: str = "redis://localhost:6379/0"

    # JWT Security
    JWT_SECRET: str = "09d25e094faa6ca2556c818166b7a9563b93f7099f6f0f4caa6cf63b88e8d3e7"
    JWT_ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60
    REFRESH_TOKEN_EXPIRE_DAYS: int = 30
    BCRYPT_ROUNDS: int = 12

    # Google OAuth
    GOOGLE_CLIENT_ID: str = ""
    GOOGLE_CLIENT_SECRET: str = ""
    GOOGLE_ANDROID_CLIENT_ID: str = ""

    # Super Admin Bootstrap
    SUPERADMIN_EMAIL: str = "admin@jobsaarthi.com"
    SUPERADMIN_PASSWORD: str = "ChangeMeInProduction_StrongP@ss2026!"

    # AI Configuration
    GEMINI_API_KEY: str = ""

    # Storage
    STORAGE_PROVIDER: str = "local"
    STORAGE_BUCKET: str = "jobsaarthi-resumes"

settings = Settings()
