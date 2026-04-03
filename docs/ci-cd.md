# CI/CD Pipeline Documentation

## Overview

This document describes the Continuous Integration (CI) and Continuous Deployment (CD) pipeline for the Mini Marketplace application.

The CI/CD pipeline ensures:
- ✅ Code compiles without errors
- ✅ All tests pass before merge
- ✅ Code quality standards are met
- ✅ Automatic deployment to Render when code reaches main branch

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     GitHub Repository                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Branches: main, develop, feature/*                  │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────┬──────────────────────────────────────────────────┘
           │
           ├─────────────────┬─────────────────┐
           │                 │                 │
           v                 v                 v
    ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
    │  CI Workflow│  │  CI Workflow│  │  CI Workflow│
    │ (compile &  │  │ (compile &  │  │ (compile &  │
    │   test)     │  │   test)     │  │   test)     │
    │ On: feature │  │ On: develop │  │ On: main    │
    │    branch   │  │    branch   │  │   branch    │
    │  Required:  │  │  Required:  │  │  Required:  │
    │  PR checks  │  │  Auto-run   │  │  Auto-run   │
    └─────────────┘  └─────────────┘  └─────────────┘
                │           │                 │
                └───────────┼─────────────────┘
                            │
                   Pass: ✅ Merge allowed
                   Fail: ❌ Merge blocked
                            │
                            v
                   ┌─────────────────┐
                   │  CD Workflow    │
                   │ (deploy-render) │
                   │ Trigger: main   │
                   │ branch push     │
                   └─────────────────┘
                            │
                            v
                   ┌─────────────────┐
                   │  Render Deploy  │
                   │  Health Check   │
                   │  Live URL Ready │
                   └─────────────────┘
```

## Workflow Triggers and Phases

### 1. CI Workflow: `.github/workflows/ci.yml`

**Trigger Events:**
- Push to `develop` branch
- Push to `main` branch
- Pull Request targeting `develop` or `main`

**Execution Phases:**

#### Phase 1: Checkout Code
```yaml
- uses: actions/checkout@v3
```
Gets the latest code from the branch/PR

#### Phase 2: Setup JDK 21
```yaml
- uses: actions/setup-java@v3
  with:
    java-version: '21'
    distribution: 'temurin'
    cache: maven
```
- Installs Java 21 (required for Spring Boot 3.3)
- Enables Maven dependency caching (faster builds)

#### Phase 3: Compile
```bash
mvn clean compile -DskipTests
```
- Cleans previous build artifacts
- Compiles all source code
- Fails if syntax errors present
- Skips tests (they run separately)
- Timeout: 15 minutes

#### Phase 4: Run Tests
```bash
mvn clean test -DskipITs
```
- Compiles and runs all unit tests
- Skips integration tests (faster feedback)
- Generates Surefire test reports
- Timeout: 20 minutes

#### Phase 5: Upload Test Reports
- Artifacts uploaded for 30 days
- Available in: Actions → Run → Artifacts
- Includes: `test-results/` and `surefire-report/` folders

#### Phase 6: Publish Results
- Creates comment on PR with test summary
- Shows passing/failing tests inline

### 2. CD Workflow: `.github/workflows/cd-render.yml`

**Trigger Events:**
- Push to `main` branch ONLY (not develop)

**Deployment Process:**
1. Build JAR with Maven
2. Authenticate with Render API
3. Deploy to Render service
4. Poll deployment status
5. Verify health endpoint (10 max attempts)

## Branch Protection Rules

### `main` branch (Production)
```
✅ Require a pull request before merging
✅ Require 1 approval review
✅ Require status checks to pass (CI must pass)
✅ Require branches to be up to date before merge
✅ Require code owner review
```

**When to merge to main:**
- Only after feature is complete and tested
- Only after release/version tag
- CI pipeline must show green ✅

### `develop` branch (Integration)
```
✅ Require status checks to pass (CI must pass)
⚠️  Allow direct pushes from M & S only
```

**Merge Protocol:**
- Create PR from feature/s-dX-* or feature/m-dX-*
- CI runs automatically
- Reviewer approves
- Merge to develop
- CI runs again after merge

### `feature/*` branches (Development)
```
No restrictions
```

**Deletion:** Auto-delete after merge (recommended)

## How to Re-run Failed Checks

### If CI fails on PR:

**Option 1: View GitHub Actions logs**
1. Go to PR → "Checks" tab
2. Click failed job name
3. View detailed logs
4. Identify error message

**Option 2: Re-run from GitHub UI**
1. Go to Actions → Failed workflow
2. Click "Re-run failed jobs"
3. GitHub re-runs only failed jobs
4. Faster than re-running entire workflow

**Option 3: Push new commit to PR**
1. Fix local code
2. `git add . && git commit -m "Fix: ..."`
3. `git push`
4. CI re-runs automatically

### If CI fails locally:

```bash
# 1. Run locally first
mvn clean compile
mvn clean test

# 2. Fix errors locally
# Edit code, commit

# 3. Push fixed code
git push

# 4. GitHub CI will verify
```

## Troubleshooting

### Build Cache Issues

**Problem:** "Cache miss, dependencies downloading..."

**Solution:**
```bash
# Clear GitHub Actions cache
# 1. Go to Settings → Actions → Caches
# 2. Delete maven cache
# 3. Re-run workflow (cache rebuilds)
```

### Test Timeout

**Problem:** Tests timeout after 20 minutes

**Solution:**
```yaml
# In ci.yml, increase timeout:
- name: Run Maven tests
  run: mvn clean test -DskipITs
  timeout-minutes: 30  # Increased from 20
```

### Compilation Fails

**Problem:** `[ERROR] COMPILATION ERROR`

**Solution:**
1. Check Java version: `java -version` (must be 21)
2. Check Maven: `mvn --version` (must be 3.8+)
3. Delete local cache: `rm -rf ~/.m2/repository`
4. Rerun: `mvn clean compile`

### Tests Fail with Database Error

**Problem:** `PSQLException: Connection refused`

**Solution:**
- GitHub CI doesn't run PostgreSQL
- Use H2 in-memory for tests
- Or mock database in tests
- Tests should be isolated, not require live DB

### Render Deployment Fails

**Problem:** CD workflow shows error

**Check:**
1. `RENDER_API_KEY` secret exists in GitHub
2. `RENDER_SERVICE_ID` secret exists
3. Render service is active and not scaled down
4. Check Render logs: https://dashboard.render.com

## Manual Deployment

If you need to deploy without code changes:

```bash
# 1. Go to Render dashboard
# 2. Select Mini Marketplace service
# 3. Click "Manual Deploy"
# 4. Select branch: main
# 5. Wait for deployment

# Or via Render CLI:
render deploy --service=mini-marketplace-prod
```

## Environment Variables in CI

### Available Built-in Variables

```yaml
github.sha         # Commit SHA
github.ref         # Branch name (refs/heads/main)
github.event_name  # Event type (push, pull_request)
github.actor       # User who triggered workflow
github.run_id      # Unique workflow run ID
```

### Example Usage

```yaml
- name: Report Status
  run: |
    echo "Commit: ${{ github.sha }}"
    echo "Branch: ${{ github.ref }}"
    echo "Triggered by: ${{ github.actor }}"
```

## Secrets Management

### Adding GitHub Secrets

1. Go to GitHub Repo → Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Add secrets (used by CI/CD):
   - `RENDER_API_KEY`: API token from Render
   - `RENDER_SERVICE_ID`: Service ID from Render

### Using Secrets in Workflows

```yaml
- name: Deploy
  env:
    RENDER_API_KEY: ${{ secrets.RENDER_API_KEY }}
  run: |
    # Secrets are masked in logs (shows ***)
    curl -X POST https://api.render.com/deploy \
      -H "Authorization: Bearer $RENDER_API_KEY"
```

⚠️ **Never commit secrets to GitHub!**

## Performance Tips

### 1. Maven Caching
```yaml
cache: maven
```
- Caches `~/.m2/repository`
- Saves 2-3 minutes per build
- Automatically managed by GitHub

### 2. Parallel Test Execution
```bash
mvn test -T 1C  # 1 thread per core
```

### 3. Skip Tests in Compile Phase
- Compile phase doesn't need tests
- Tests run separate phase
- Faster feedback

### 4. Use `-q` (quiet) flag
```bash
mvn clean compile -q  # Less verbose output
```

## Supporting CI/CD (First Time)

### Step 1: Create workflows directory
```bash
mkdir -p .github/workflows
```

### Step 2: Add ci.yml
Create `.github/workflows/ci.yml` with content from this guide

### Step 3: Add cd-render.yml (for S)
S will create this in Day 8[S]

### Step 4: Commit and push
```bash
git add .github/workflows/
git commit -m "D8[M]: Add CI pipeline workflow"
git push
```

### Step 5: Verify CI runs
1. Go to GitHub repo
2. Click "Actions" tab
3. Should show "CI Pipeline" running
4. Wait for ✅ or ❌ result

## Monitoring

### Real-time Monitoring
- Go to repo → Actions tab
- See all workflow runs
- Click run to see detailed logs
- Download artifacts

### Email Notifications
- GitHub sends email if:
  - Workflow fails
  - Workflow succeeds (if configured)
- Configure in: Settings → Notifications

### Status Badge
Add to README.md:
```markdown
[![CI](https://github.com/[org]/[repo]/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/[org]/[repo]/actions)
```

## Common Issues and Solutions

| Issue | Root Cause | Solution |
|-------|-----------|----------|
| Maven compile fails | JDK version mismatch | Use JDK 21 only |
| Tests fail in CI but pass locally | Database dependency | Mock DB in tests |
| Cache hit ratio low | Dependencies changed frequently | Update cache key |
| Deployment timeout | Render service slow | Increase timeout to 30min |
| Secrets not found | Secret name mismatch | Check exact secret name |
| PR blocked by CI | Missing test file | Add test file and commit |

## Support & Contact

- **Issues:** Report in GitHub Issues
- **Questions:** Check workflow logs in Actions tab
- **Failures:** Re-run or contact M (GitHub Actions maintainer)

---

**Last Updated:** April 2026
**Maintained by:** M (k-i-mahi)
