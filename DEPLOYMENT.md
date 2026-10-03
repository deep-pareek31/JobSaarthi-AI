# JobSaarthi Production Deployment Guide

## 1. Production Architecture Overview

The system is designed to run in containerized environments (Kubernetes, AWS ECS, GCP Cloud Run, or Railway/Render) backed by managed persistence:

- **Web & Admin Portal**: Vercel / Cloudflare Pages / AWS CloudFront.
- **FastAPI Core**: GCP Cloud Run or AWS ECS behind Application Load Balancer with SSL termination.
- **PostgreSQL**: AWS RDS PostgreSQL or GCP Cloud SQL (v15+) with automated backups and read-replicas.
- **Redis**: AWS ElastiCache Redis or GCP Memorystore.
- **Workers**: Celery containers orchestrated with auto-scaling based on queue depth.
- **Private Object Storage**: AWS S3 / Google Cloud Storage with private buckets and signed URLs for resumes.

## 2. Docker Compose Production Deployment

See `/backend/docker-compose.yml` for complete multi-container orchestration.
To deploy on a single VPS or staging machine:

```bash
# Clone repository
git clone https://github.com/jobsaarthi/jobsaarthi.git /opt/jobsaarthi
cd /opt/jobsaarthi/backend

# Configure production environment
cp ../.env.example .env
chmod 600 .env

# Build and start services
docker-compose -f docker-compose.prod.yml up -d --build
```
