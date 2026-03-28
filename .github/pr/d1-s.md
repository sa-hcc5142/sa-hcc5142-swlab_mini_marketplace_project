## Summary
- Implemented Day 1 S timeline foundation for security and order domain.
- Added entities: User, Role, Order, OrderItem.
- Added repositories for user/role/order/order-item data access.
- Added security skeleton with method security enabled and BCrypt password encoder.
- Added application configuration with environment-based datasource placeholders.

## Requirement Mapping
- Functional requirement(s):
  - Authentication and authorization groundwork (Spring Security + BCrypt bean).
  - PostgreSQL and JPA model foundation with role-aware user model.
- Rubric section(s):
  - Architecture and code quality (layered entity-repository structure).
  - Security and role management (initial security configuration).
  - Database design (core entities and relationships).

## Files Changed
- src/main/java/com/marketplace/MarketplaceApplication.java
- src/main/java/com/marketplace/entity/User.java
- src/main/java/com/marketplace/entity/Role.java
- src/main/java/com/marketplace/entity/Order.java
- src/main/java/com/marketplace/entity/OrderItem.java
- src/main/java/com/marketplace/repository/UserRepository.java
- src/main/java/com/marketplace/repository/RoleRepository.java
- src/main/java/com/marketplace/repository/OrderRepository.java
- src/main/java/com/marketplace/repository/OrderItemRepository.java
- src/main/java/com/marketplace/config/SecurityConfig.java
- src/main/resources/application.yml

## Test Evidence
- [ ] Unit tests passed locally (N/A in Day 1 scope)
- [ ] Integration tests passed locally (N/A in Day 1 scope)
- [x] Build-impacting config reviewed for Day 1 structure and naming

## Compliance Checklist
- [x] Role-based access scaffolding preserved
- [x] No direct main branch push
- [x] Docker compatibility preserved (env-driven config)
- [x] CI compatibility preserved (no hardcoded machine-specific values)

## Reviewer Notes
- Please focus on: entity mappings and security skeleton defaults.
- Risk/edge case: Product relation in OrderItem is currently stored as productId and will be upgraded once Product entity from M branch is merged.
