# Docker Setup Guide

## Quick Start

### Prerequisites
- Docker Desktop (or Docker Engine) installed
- Free ports: `8888` (app), `5432` (PostgreSQL)

### Required Environment Variable

`DB_PASSWORD` is required. Compose fails fast if it is missing.

PowerShell:
```powershell
$env:DB_PASSWORD="your-strong-password"
```

Command Prompt:
```cmd
set DB_PASSWORD=your-strong-password
```

Optional variables (safe defaults exist):
- `DB_USERNAME` default: `marketplace_user`
- `DB_NAME` default: `mini_marketplace`
- `DB_PORT` default: `5432`

### Run

```bash
cd sa-hcc5142-swlab_mini_marketplace_project
docker compose up --build
```

Health check:
```bash
curl http://localhost:8888/api/actuator/health
```

## Runtime Port Consistency

- `Dockerfile` exposes container port `8080`
- Spring server runs on `SERVER_PORT=8080` in compose
- Host port mapping is `8888:8080`

## Current Compose Behavior

`docker-compose.yml` services:
- `postgres`: PostgreSQL 16 with volume `postgres_data`
- `app`: Spring Boot service using docker profile

Key environment wiring:
- `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/${DB_NAME}`
- `SPRING_DATASOURCE_USERNAME=${DB_USERNAME}`
- `SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}`

Security hardening:
- No weak password placeholder defaults in compose
- DB password must be explicitly provided by operator

## Useful Commands

Start:
```bash
docker compose up --build
```

Stop:
```bash
docker compose down
```

Stop and remove data volume:
```bash
docker compose down -v
```

Logs:
```bash
docker compose logs -f app
docker compose logs -f postgres
```

## Troubleshooting

### Missing `DB_PASSWORD`

Compose error indicates required variable is not set.
Set it in your shell and rerun.

### Port Conflict

If `8888` or `5432` is in use, change host-side port mappings in `docker-compose.yml`.

### App Unhealthy

Check:
```bash
docker compose logs -f app
docker compose logs -f postgres
```

Then verify DB credentials and service startup order.
