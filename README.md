# Mini Marketplace

Full-stack software engineering lab project built with Spring Boot, Thymeleaf, PostgreSQL, Spring Security, Docker, GitHub Actions, and Render.

## Team

- S
- M

## Project Theme

Mini Marketplace with three role-separated user types:

- ADMIN: platform management and user moderation
- SELLER: product management and order fulfillment actions
- BUYER: product browsing and order placement

## Objective

Build and deliver a small but complete production-style web application that demonstrates a professional workflow:

- layered architecture and clean code
- authentication and role-based authorization
- RESTful API design with global exception handling
- automated testing in CI
- Dockerized runtime
- branch-based collaboration and PR reviews
- automatic cloud deployment

## Tech Stack

- Java 21 (or course-approved version)
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Thymeleaf
- PostgreSQL
- JUnit 5 + Mockito + SpringBootTest + MockMvc
- Docker + Docker Compose
- GitHub Actions
- Render

## Core Features

- User registration and login/logout
- BCrypt password hashing
- Role-based route protection for ADMIN, SELLER, BUYER
- Product CRUD
- Order placement and order history
- Admin user/role management
- Global exception responses and consistent status codes

## Architecture

Layered architecture:

- Controller layer: request handling and HTTP response mapping
- Service layer: business rules and transaction coordination
- Repository layer: persistence access with Spring Data JPA
- Entity and DTO layer: domain model and transport model separation
- Security layer: authentication, authorization, and policy enforcement

Architecture diagram file location:

- docs/architecture-diagram.png

## Database Design

Planned core tables:

- users
- roles
- user_roles
- products
- orders
- order_items

Relationship coverage:

- User M:M Role via user_roles
- User 1:M Product (seller ownership)
- User 1:M Order (buyer ownership)
- Order 1:M OrderItem
- Product 1:M OrderItem

ER diagram file location:

- docs/er-diagram.png

## Security Model

- Password storage: BCrypt
- Authentication: Spring Security login flow
- Authorization: method-level and/or URL-based restrictions

Access rules summary:

- ADMIN: manage users, moderation, platform-level views
- SELLER: create/update/delete own products, view relevant orders
- BUYER: browse products, place orders, view own orders

## REST API Summary

Base path: /api

Authentication:

- POST /auth/register
- POST /auth/login
- POST /auth/logout

Products:

- GET /products
- GET /products/{id}
- POST /products
- PUT /products/{id}
- DELETE /products/{id}

Orders:

- POST /orders
- GET /orders/my
- GET /orders/{id}

Admin:

- GET /admin/users
- PATCH /admin/users/{id}/role
- PATCH /admin/users/{id}/status

Detailed endpoint documentation file:

- docs/api-endpoints.md

## Testing Strategy

Minimum targets:

- Unit tests (service layer): at least 15
- Integration tests (controller layer): at least 3

Testing tools:

- JUnit 5
- Mockito
- SpringBootTest
- MockMvc

## Docker Setup

### Quick Start

#### Prerequisites
- Docker and Docker Compose installed
- Ports 8888 and 5432 available on your machine

#### Running with Docker

```bash
# Build and start services
docker-compose up --build

# In another terminal, verify health
curl http://localhost:8888/api/actuator/health
```

#### Expected Startup
- PostgreSQL: ~30-40 seconds to initialize
- Spring Boot: ~20-30 seconds to start
- **Total: ~1 minute to full readiness**

### Access Application

- **Web**: http://localhost:8888
- **Health Check**: http://localhost:8888/api/actuator/health
- **API Base**: http://localhost:8888/api

### Configuration

#### Environment Variables
Copy `.env.example` to `.env` and customize as needed:

```bash
cp .env.example .env
```

Common variables:
- `DB_URL`: PostgreSQL connection string
- `DB_USERNAME`: Database user (default: marketplace_user)
- `DB_PASSWORD`: Database password
- `SPRING_PROFILES_ACTIVE`: Spring profile (docker)
- `JAVA_TOOL_OPTIONS`: JVM memory settings (-Xmx512m -Xms256m)

#### Spring Profiles
- **default** (application.yml): Local development, PostgreSQL on localhost
- **docker** (application-docker.yml): Docker Compose environment, PostgreSQL via 'postgres' hostname
- **prod**: Production configuration (future)

### Docker Files
- `Dockerfile`: Multi-stage build (Maven compilation + JDK 21 runtime)
- `docker-compose.yml`: PostgreSQL + Spring Boot service orchestration
- `.dockerignore`: Build context optimization

### Stopping Services

```bash
# Stop containers (keep data)
docker-compose stop

# Stop and remove containers
docker-compose down

# Stop, remove containers, and volumes
docker-compose down -v
```

### Troubleshooting

**Port conflicts:**
```bash
# Find process using port 8888
netstat -ano | findstr :8888
taskkill /PID <PID> /F
```

**View logs:**
```bash
docker-compose logs -f app        # Application logs
docker-compose logs -f postgres   # Database logs
```

**Container health issues:**
```bash
docker ps                    # Check container status
docker-compose restart       # Restart services
docker system prune          # Clean up unused resources
```

For comprehensive Docker guidance, see [docs/docker-setup.md](docs/docker-setup.md)

## Deployment

### Render Deployment

The application is configured for automatic deployment to [Render](https://render.com) via GitHub Actions CI/CD pipeline.

Deployment URL: `https://mini-marketplace-prod.onrender.com` (when configured)

Commands:

```bash
mvn clean test
```

Test evidence report file:

- docs/test-report.md

## Local Development Setup

### 1) Clone

```bash
git clone https://github.com/sa-hcc5142/sa-hcc5142-swlab_mini_marketplace_project.git
cd sa-hcc5142-swlab_mini_marketplace_project
```

### 2) Environment Variables

Create local environment variables or use .env values for Docker:

- DB_URL
- DB_USERNAME
- DB_PASSWORD
- SERVER_PORT

### 3) Run with Maven

```bash
mvn spring-boot:run
```

### 4) Run with Docker Compose

```bash
docker compose up --build
```

This command is mandatory as per course requirement and must run successfully.

## CI/CD Pipeline

GitHub Actions workflows:

- CI workflow: build + test on pull requests and protected branches
- CD workflow: deploy to Render from main branch after successful checks

CI/CD documentation file:

- docs/ci-cd.md

## Git Collaboration Workflow

Branch strategy:

- main (protected)
- develop
- feature/*

Rules:

- no direct push to main
- pull request required
- at least one review approval before merge
- CI checks must pass before merge

## Deployment

Platform: Render

- Live URL: TO_BE_ADDED
- Repository URL: https://github.com/sa-hcc5142/sa-hcc5142-swlab_mini_marketplace_project

## Required Deliverables Checklist

- [x] GitHub repository created
- [ ] README complete and kept up to date with implementation
- [ ] Architecture diagram added
- [ ] ER diagram added
- [ ] API endpoint document completed
- [ ] CI/CD explanation completed
- [ ] Public Render URL added
- [ ] 5-minute demo prepared

Demo script file:

- docs/demo-script.md

## Current Progress Status

- Day 1 S baseline completed on feature branch
- Day 1 M tasks planned next

## License

For academic use in CSE 3220 Software Engineering and Project Management Lab.
