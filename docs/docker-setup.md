# Docker Setup Guide

## Quick Start

### Prerequisites
- Docker installed (version 20.10 or higher)
- Docker Compose installed (version 1.29 or higher)
- No services running on ports 8888 or 5432

### Running the Application

```bash
cd sa-hcc5142-swlab_mini_marketplace_project

# Build and start services
docker-compose up --build

# In another terminal, verify health
curl http://localhost:8888/api/actuator/health
```

### Expected Startup Time
- PostgreSQL initialization: 30-40 seconds
- Spring Boot startup: 20-30 seconds
- **Total: ~1 minute from docker-compose up to application ready**

## Environment Variables

### Database Configuration
| Variable | Default | Description |
|----------|---------|-------------|
| DB_URL | jdbc:postgresql://postgres:5432/mini_marketplace | PostgreSQL connection string |
| DB_USERNAME | marketplace_user | Database user |
| DB_PASSWORD | secure_password_here | Database password |
| DB_HOST | postgres | Database hostname (internal network) |
| DB_PORT | 5432 | Database port |

### Application Configuration
| Variable | Default | Description |
|----------|---------|-------------|
| SERVER_PORT | 8080 | Internal Spring Boot port |
| SPRING_PROFILES_ACTIVE | docker | Active Spring profile |
| SPRING_JPA_HIBERNATE_DDL_AUTO | update | Hibernate DDL strategy |
| LOGGING_LEVEL_ROOT | INFO | Root logging level |

### JVM Configuration
| Variable | Default | Description |
|----------|---------|-------------|
| JAVA_TOOL_OPTIONS | -Xmx512m -Xms256m | JVM memory settings |

## profiles: dev vs docker vs prod

### application.yml (Default/Dev)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mini_marketplace
    username: postgres
    password: postgres
```
- **Use for**: Local development without Docker
- **Targets**: PostgreSQL on localhost:5432

### application-docker.yml (Docker Profile)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/mini_marketplace
    username: marketplace_user
    password: secure_password_here
```
- **Use for**: Docker Compose environment
- **Targets**: PostgreSQL service via internal network hostname 'postgres'
- **Activated by**: SPRING_PROFILES_ACTIVE=docker in docker-compose.yml

### Switching Profiles

**For development (no Docker):**
```bash
mvn spring-boot:run
# Uses application.yml (default profile)
```

**For Docker:**
```bash
docker-compose up --build
# Activates docker profile automatically via SPRING_PROFILES_ACTIVE=docker
```

## Database Initialization

### Automatic Schema Creation
When the application starts:
1. Hibernate reads all `@Entity` classes
2. Sets `spring.jpa.hibernate.ddl-auto=update`
3. Creates missing tables automatically
4. Adds indexes and constraints

### Entities Initialized
- User, Role
- Product, Order, OrderItem
- Review
- Cart, CartItem

### Tables Created
- users
- roles
- products
- orders
- order_items
- reviews
- carts
- cart_items

## Health Check Endpoints

### Application Health
```bash
curl http://localhost:8888/api/actuator/health
```

Response (healthy):
```json
{"status":"UP"}
```

### Docker Container Health
```bash
docker ps
```

Check STATUS column:
- `Up X minutes (healthy)` = Container is healthy
- `Up X minutes (unhealthy)` = Container failed health check

### Detailed Health Info
```bash
curl http://localhost:8888/api/actuator/health/details
```

## Troubleshooting

### Port 8888 Already in Use
```bash
# Find process using port 8888
netstat -ano | findstr :8888

# Kill process (Windows)
taskkill /PID <PID> /F

# Or change docker-compose.yml port mapping
# Change "8888:8080" to "8889:8080"
```

### Port 5432 Already in Use
```bash
# Find process using port 5432
netstat -ano | findstr :5432

# Or change docker-compose.yml PostgreSQL port
# Change "5432:5432" to "5433:5432"
```

### PostgreSQL Connection Refused
**Symptoms**: `org.postgresql.util.PSQLException: Connection refused`

**Solutions**:
1. Check if postgres service is running:
   ```bash
   docker ps | findstr postgres
   ```

2. Check PostgreSQL logs:
   ```bash
   docker-compose logs postgres
   ```

3. Verify container has time to start:
   - postgres has `start_period: 30s`
   - app has `start_period: 90s`
   - If still failing, increase values in docker-compose.yml

### App Won't Start (Unhealthy)
**Symptoms**: Container marked unhealthy, Tomcat not starting

**Solutions**:
1. Check app logs:
   ```bash
   docker-compose logs app
   ```

2. Verify environment variables are set in docker-compose.yml

3. Check for port conflicts on 8080 (internal)

4. Increase health check start_period in docker-compose.yml

### Data Persists Between Restarts
**Problem**: Old data still present after `docker-compose down`

**Solutions**:
```bash
# Option 1: Remove volume
docker volume rm <project_name>_postgres_data

# Option 2: Clean everything
docker-compose down -v

# Option 3: Check named volume
docker volume ls
```

## Logs

### View Application Logs
```bash
docker-compose logs -f app
```

### View Database Logs
```bash
docker-compose logs -f postgres
```

### View Specific Log Lines
```bash
# Last 50 lines
docker-compose logs --tail=50 app

# Since specific time
docker-compose logs --since 2026-04-03T12:00:00 app
```

## Stopping and Cleaning

### Stop Services (Keep Data)
```bash
docker-compose stop
```

### Stop and Remove Containers
```bash
docker-compose down
```

### Stop, Remove Containers and Volumes
```bash
docker-compose down -v
```

### Remove All Docker Resources (CAUTION)
```bash
docker system prune -a
```

## Development Workflow

### Make Code Changes
```bash
# Edit source files
vim src/main/java/com/marketplace/...

# Rebuild container
docker-compose up --build
```

### View Hot Reload (if enabled)
- Changes are picked up after Maven recompile
- No restart needed if using Spring DevTools

### Rebuild from Scratch
```bash
docker-compose down -v
docker-compose up --build
```

## Testing

### Run Tests in Container
```bash
docker exec mini_marketplace_app mvn clean test
```

### Run Tests Locally
```bash
mvn clean test
```

## Performance Tips

1. **Use Named Volumes**: Data persists across restarts
2. **Set Resource Limits**: `-Xmx512m` prevents OOM
3. **Enable Caching**: Multi-stage Dockerfile uses Maven cache
4. **Optimize .dockerignore**: Reduces build context
5. **Use Health Checks**: Orchestrators can restart failed services

## Useful Docker Commands

```bash
# See all running containers
docker ps

# See all containers (including stopped)
docker ps -a

# See images
docker images

# See volumes
docker volume ls

# Inspect container
docker inspect mini_marketplace_app

# Execute command in container
docker exec mini_marketplace_app sh

# Copy file from container
docker cp mini_marketplace_app:/app/app.jar ./app.jar

# Remove unused resources
docker system prune
```

## Networking

### Docker Compose Network
- Network name: `marketplace_network`
- Services can communicate using hostname (internal DNS)
- postgres:5432 resolves to PostgreSQL service
- Isolated from host network except mapped ports

### Port Mappings
| Service | Internal | External |
|---------|----------|----------|
| PostgreSQL | 5432 | 5432 |
| Spring Boot | 8080 | 8888 |

### Accessing Services
| Service | From Host | From Container |
|---------|-----------|-----------------|
| PostgreSQL | localhost:5432 | postgres:5432 |
| Spring Boot | localhost:8888 | app:8080 |

---
