import os
from celery import Celery
from celery.schedules import crontab

broker_url = os.getenv("CELERY_BROKER_URL", "redis://localhost:6379/1")
backend_url = os.getenv("CELERY_RESULT_BACKEND", "redis://localhost:6379/2")

celery_app = Celery("jobsaarthi_workers", broker=broker_url, backend=backend_url)

celery_app.conf.update(
    task_serializer="json",
    accept_content=["json"],
    result_serializer="json",
    timezone="UTC",
    enable_utc=True,
    task_track_started=True,
    task_time_limit=300, # 5 minutes hard limit
)

celery_app.conf.beat_schedule = {
    "poll-high-priority-sources-every-15m": {
        "task": "workers.tasks.ingestion.poll_sources",
        "schedule": crontab(minute="*/15"),
        "args": (1,) # priority 1 sources
    },
    "poll-standard-sources-hourly": {
        "task": "workers.tasks.ingestion.poll_sources",
        "schedule": crontab(minute="0", hour="*"),
        "args": (2,)
    },
    "validate-expirations-every-hour": {
        "task": "workers.tasks.ingestion.check_expirations",
        "schedule": crontab(minute="30", hour="*"),
    },
    "full-consistency-daily": {
        "task": "workers.tasks.ingestion.full_consistency_audit",
        "schedule": crontab(minute="0", hour="2"),
    }
}
