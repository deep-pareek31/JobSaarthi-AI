from fastapi import APIRouter
from app.api.v1 import admin, ai, applications, auth, health, jobs, preferences, resumes, subscriptions, users

api_router = APIRouter()
api_router.include_router(health.router)
api_router.include_router(auth.router)
api_router.include_router(users.router)
api_router.include_router(jobs.router)
api_router.include_router(applications.router)
api_router.include_router(ai.router)
api_router.include_router(resumes.router)
api_router.include_router(preferences.router)
api_router.include_router(subscriptions.router)
api_router.include_router(admin.router)
