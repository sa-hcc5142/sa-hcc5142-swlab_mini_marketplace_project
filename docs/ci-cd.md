# CI/CD Pipeline Documentation

This project uses GitHub Actions for automated validation and deployment.

## 1. Continuous Integration (CI)

Workflow file: `.github/workflows/ci.yml`

### Triggers

- Push to: `main`, `develop`
- Pull request to: `main`, `develop`

### Steps

1. Checkout source
2. Set up Java 21
3. Cache Maven dependencies
4. Run full validation:
```bash
mvn clean verify -Dmaven.javadoc.skip=true
```
5. Mark workflow failed if any build/test stage fails

### Coverage

- Compile validation
- Unit tests (Surefire)
- Integration tests (Failsafe)

## 2. Continuous Deployment (CD)

Workflow file: `.github/workflows/cd-render.yml`

### Trigger

- Push to `main`

### Steps

1. Trigger Render Deploy Hook
2. Wait for deployment
3. Poll health endpoint

### Render runtime

- Build: `mvn clean package`
- Start: `java -jar target/*.jar`
- Health check: `/api/actuator/health`

## 3. Required GitHub Secrets

- `RENDER_DEPLOY_HOOK_URL`
- `RENDER_HEALTHCHECK_URL`

App runtime secrets (DB credentials, etc.) are configured in Render environment variables.

## 4. Branch Protection Checklist

Recommended for `main`:

- Pull request required
- At least one approval required
- Required status checks must pass (CI)
- Prevent force push and branch deletion

## 5. Local Verification Before Push

```bash
mvn clean verify -Dmaven.javadoc.skip=true
```

Container runtime verification:

```bash
docker compose up --build
```

## 6. Outcome

- PRs to main/develop are automatically validated.
- Main stays deployment-ready.
- Pushes to main trigger automated Render deployment.
