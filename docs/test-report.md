# Test Report

This report summarizes automated test execution for the Mini Marketplace project.

## 1. Scope

- Unit tests: service and security components
- Integration tests: REST API and Thymeleaf view routes
- Deterministic SQL-seeded setup for integration tests

## 2. Tooling

- JUnit 5
- Mockito
- SpringBootTest
- MockMvc
- Spring Security Test
- Maven Surefire and Failsafe

## 3. Verification Command

```bash
mvn clean verify -Dmaven.javadoc.skip=true
```

## 4. Latest Verified Results

### Unit (Surefire)

- `CustomUserDetailsServiceTest`: 4
- `AdminServiceTest`: 4
- `AuthServiceTest`: 4
- `CartServiceTest`: 6
- `OrderServiceTest`: 5
- `ProductAuthorizationServiceTest`: 9
- `ProductServiceTest`: 8
- `ReviewServiceTest`: 6
- Unit total: 46, failures: 0, errors: 0

### Integration (Failsafe)

- `AdminControllerIT`: 7
- `AuthControllerIT`: 4
- `CartControllerIT`: 4
- `OrderControllerIT`: 2
- `ProductControllerIT`: 7
- `ReviewControllerIT`: 6
- `ViewControllerIT`: 5
- Integration total: 35, failures: 0, errors: 0

### Overall

- Total tests: 81
- Failures: 0
- Errors: 0
- Build: SUCCESS

## 5. Method

- `@SpringBootTest` and `@AutoConfigureMockMvc`
- Security scenarios via `@WithMockUser` or authenticated mock principal
- SQL scripts:
  - `src/test/resources/sql/test-cleanup.sql`
  - `src/test/resources/sql/test-seed-roles.sql`

## 6. Rubric Compliance

- Minimum unit tests (15): Achieved (46)
- Minimum integration tests (3): Achieved (35)
- CI integrated testing: Achieved

## 7. Evidence Paths

- Surefire reports: `target/surefire-reports/`
- Failsafe reports: `target/failsafe-reports/`

## 8. Conclusion

Automated tests are stable and passing across unit and integration levels.
