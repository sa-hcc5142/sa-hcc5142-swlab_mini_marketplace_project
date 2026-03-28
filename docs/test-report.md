# Test Report - Mini Marketplace

## Test Objective

Validate core business logic, access control behavior, and API integration flow according to course requirements.

## Required Minimums

- Unit tests (service layer): >= 15
- Integration tests (controller layer): >= 3
- All tests must pass in CI

## Tooling

- JUnit 5
- Mockito
- SpringBootTest
- MockMvc

## Planned Unit Test Coverage

AuthService:
- register success
- register duplicate email rejection
- password encryption on registration
- login success
- login invalid credentials

ProductService:
- create product success for seller
- update own product success
- update non-owner denied
- delete own product success
- list products pagination

OrderService:
- place order success
- place order insufficient stock
- calculate order total correctly
- order retrieval for buyer
- unauthorized order access denied

AdminService:
- list users for admin
- role update success
- deactivate user success

## Planned Integration Test Coverage

AuthControllerIT:
- register endpoint success
- login endpoint success/failure

ProductControllerIT:
- create endpoint forbidden for buyer
- list endpoint success

OrderControllerIT:
- place order success
- get my orders success

AdminControllerIT:
- admin users endpoint forbidden for non-admin

## Execution Command

```bash
mvn clean test
```

## Evidence Table

| Date | Branch | Total Unit | Total Integration | Result | Notes |
|------|--------|------------|-------------------|--------|-------|
| TBD  | TBD    | TBD        | TBD               | TBD    | TBD   |

## CI Result Link

- GitHub Actions run URL: TO_BE_ADDED

## Known Gaps

- Populate actual test counts after Day 6 completion.
- Add CI run URL after workflows are active.
