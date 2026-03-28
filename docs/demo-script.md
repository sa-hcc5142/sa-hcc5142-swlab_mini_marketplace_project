# 5-Minute Demo Script - Mini Marketplace

## Goal

Show that the project satisfies all mandatory lab requirements end to end.

## Total Duration

5 minutes

## Speaker Split

- S: 2.5 minutes
- M: 2.5 minutes

## Demo Flow

### 0:00 - 0:30 (S) - Project Intro

- Introduce project theme: Mini Marketplace
- Show team members: S and M
- Mention stack briefly: Spring Boot, PostgreSQL, Security, Docker, CI/CD, Render

### 0:30 - 1:30 (S) - Security and Roles

- Show registration/login flow
- Explain BCrypt password encryption
- Show role-based access:
  - ADMIN-only endpoint access
  - SELLER product management access
  - BUYER order placement access

### 1:30 - 2:30 (M) - Core Features and API

- Show product listing and product CRUD flow
- Show order placement and order history
- Open API endpoint documentation file
- Mention proper HTTP status handling + global exceptions

### 2:30 - 3:20 (M) - Database and Architecture

- Show entity relationship diagram
- Explain key relationships (User-Role, Order-OrderItem, Product-OrderItem)
- Show layered package structure in project

### 3:20 - 4:10 (S) - Testing and CI

- Run or show test command output
- Show unit and integration test count satisfaction
- Open GitHub Actions run page showing successful CI

### 4:10 - 4:45 (M) - Docker and Deployment

- Show docker compose up --build command
- Show app running from containerized setup
- Open Render deployed URL and confirm public accessibility

### 4:45 - 5:00 (S + M) - Conclusion

- Show repository URL and README
- Reconfirm mandatory items are satisfied
- Close demo

## Demo Checklist

- [ ] Repository URL ready
- [ ] Render URL ready
- [ ] CI run page ready
- [ ] diagrams ready (architecture + ER)
- [ ] role-based test accounts ready
- [ ] docker compose command tested

## Backup Plan (If Internet Fails During Demo)

- Keep local app running in advance
- Keep screenshots of:
  - CI success
  - Render deployment success
  - API responses for role-restricted endpoints
- Continue walkthrough from local environment and screenshots
