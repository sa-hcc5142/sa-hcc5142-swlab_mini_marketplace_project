# CI/CD Documentation - Mini Marketplace

## Pipeline Goals

The CI/CD setup ensures:
- every PR is validated by automated build and tests
- unstable code cannot be merged to protected branches
- main branch updates trigger production deployment to Render

## Branch Workflow

- main: protected production branch
- develop: integration branch
- feature/*: implementation branches for S and M

Mandatory rules:
- no direct push to main
- PR required with at least one approval
- status checks required before merge

## CI Workflow (GitHub Actions)

Planned workflow file: .github/workflows/ci.yml

Suggested trigger:
- pull_request on develop and main
- push on develop and main

Core CI steps:
1. Checkout code
2. Setup Java
3. Cache Maven dependencies
4. Build project
5. Run unit + integration tests
6. Publish test result summary

Example Maven command:
```bash
mvn -B clean test
```

## CD Workflow (Render Deployment)

Planned workflow file: .github/workflows/cd-render.yml

Suggested trigger:
- push on main

Core CD steps:
1. Checkout code
2. Validate build (optional pre-deploy)
3. Trigger Render deploy hook
4. Report deployment status

## Required Secrets

Set these in GitHub repository secrets:
- RENDER_DEPLOY_HOOK_URL
- (optional) RENDER_API_KEY if API-based deploy is used

Never hardcode credentials in workflow files.

## Recommended Branch Protection Setup

For main:
- Require pull request before merging
- Require at least 1 approval
- Require status checks to pass
- Restrict direct push

For develop:
- Require pull request before merging
- Require status checks (recommended)

## Failure Handling

If CI fails:
1. Review failed job logs
2. Fix in same feature branch
3. Push updates
4. Wait for CI green before merge

If CD fails:
1. Verify Render service status and env vars
2. Verify deploy hook validity
3. Re-run workflow after fix

## Proof for Lab Submission

Capture and include:
- screenshot of successful CI run
- screenshot of successful deployment run
- link to workflow run history
- mention of branch protection settings in README
