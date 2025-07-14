# CI/CD Setup Guide

This repository includes automated CI/CD pipelines using GitHub Actions for testing and Docker image deployment.

## Overview

- **CI (Continuous Integration)**: Runs tests on every push and pull request
- **CD (Continuous Deployment)**: Builds and pushes Docker images to Docker Hub on main branch pushes
- **PR Checks**: Additional quality checks for pull requests
- **Dependency Updates**: Automated dependency updates via Dependabot

## Setup Instructions

### 1. Docker Hub Configuration

#### Create Docker Hub Repository
1. Go to [Docker Hub](https://hub.docker.com/)
2. Create a new repository (e.g., `your-username/demo-app`)
3. Make note of the repository name

#### Generate Docker Hub Access Token
1. Go to Docker Hub → Account Settings → Security
2. Click "New Access Token"
3. Name: `GitHub Actions`
4. Permissions: `Read, Write, Delete`
5. Copy the generated token

### 2. GitHub Repository Secrets

Add the following secrets to your GitHub repository:

1. Go to your GitHub repository
2. Navigate to Settings → Secrets and variables → Actions
3. Add these repository secrets:

| Secret Name          | Value                        | Description                        |
| -------------------- | ---------------------------- | ---------------------------------- |
| `DOCKERHUB_USERNAME` | Your Docker Hub username     | Used for Docker Hub login          |
| `DOCKERHUB_TOKEN`    | Your Docker Hub access token | Used for Docker Hub authentication |

### 3. Update Configuration

#### Update Docker Image Name
Edit `.github/workflows/ci-cd.yml` and change:
```yaml
env:
  DOCKER_IMAGE_NAME: your-dockerhub-username/demo-app  # ← Change this
```

#### Update Dependabot Configuration
Edit `.github/dependabot.yml` and change:
```yaml
reviewers:
  - "your-github-username"  # ← Change this
assignees:
  - "your-github-username"  # ← Change this
```

## Workflow Details

### CI Pipeline (`ci-cd.yml`)

**Triggers:**
- Push to `main`, `master`, or `develop` branches
- Pull requests to `main` or `master` branches

**CI Job:**
- Sets up Java 17 and Maven
- Starts MongoDB and MinIO services
- Runs all tests with proper service dependencies
- Generates test reports
- Uploads test artifacts

**CD Job:**
- Runs only on pushes to `main`/`master` (after CI passes)
- Builds the Spring Boot application
- Creates multi-platform Docker image (AMD64 + ARM64)
- Pushes to Docker Hub with multiple tags:
  - `latest` (for main branch)
  - `main-<commit-sha>` (for traceability)
  - Branch name (for feature branches)

### PR Check Pipeline (`pr-check.yml`)

**Triggers:**
- Pull requests to `main` or `master` branches

**Features:**
- Runs the same tests as CI
- Checks code formatting (if Spotless is configured)
- Security scanning (if configured)
- Adds automated comments to PRs with results

### Dependabot (`dependabot.yml`)

**Features:**
- Weekly dependency updates for Maven, GitHub Actions, and Docker
- Automatic PR creation for updates
- Configurable reviewers and assignees

## Usage

### Automatic Triggers

1. **Push to main/master**: Runs CI → CD (if CI passes)
2. **Push to feature branch**: Runs CI only
3. **Create Pull Request**: Runs PR checks
4. **Weekly**: Dependabot creates dependency update PRs

### Manual Triggers

You can manually trigger workflows from the GitHub Actions tab:
1. Go to Actions tab in your repository
2. Select the workflow
3. Click "Run workflow"

## Docker Image Tags

The CD pipeline creates multiple tags for flexibility:

- `latest`: Always points to the latest main branch build
- `main-<sha>`: Specific commit from main branch
- `<branch>-<sha>`: Builds from other branches (if configured)

### Using the Images

```bash
# Pull latest version
docker pull your-username/demo-app:latest

# Pull specific commit
docker pull your-username/demo-app:main-abc1234

# Run the container
docker run -p 8080:8080 your-username/demo-app:latest
```

## Monitoring and Troubleshooting

### Check Workflow Status

1. Go to the Actions tab in your GitHub repository
2. Click on any workflow run to see details
3. Expand job steps to see logs

### Common Issues

#### Docker Hub Authentication Failed
- Verify `DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN` secrets
- Ensure the Docker Hub token has write permissions
- Check if the repository name in the workflow matches your Docker Hub repo

#### Tests Failing
- Check if MongoDB and MinIO services are starting correctly
- Verify environment variables in the workflow
- Look at test logs in the workflow output

#### Build Failing
- Ensure the application builds locally with `mvn clean package`
- Check if all dependencies are available
- Verify Java version compatibility

### Viewing Logs

```bash
# Check workflow logs in GitHub Actions tab
# Or use GitHub CLI
gh run list
gh run view <run-id> --log
```

## Customization

### Adding More Test Environments

You can add matrix builds for different Java versions:

```yaml
strategy:
  matrix:
    java-version: [17, 21]
```

### Adding Code Quality Checks

Add tools like SonarQube, SpotBugs, or Checkstyle:

```yaml
- name: SonarQube Scan
  uses: sonarqube-quality-gate-action@master
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

### Deploy to Multiple Registries

Add additional registries like GitHub Container Registry:

```yaml
- name: Log in to GitHub Container Registry
  uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}
```

## Security Best Practices

1. **Use secrets for sensitive data** - Never hardcode credentials
2. **Limit token permissions** - Use minimal required permissions
3. **Regular updates** - Keep actions and dependencies updated
4. **Scan images** - Add container security scanning
5. **Sign images** - Consider using cosign for image signing

## Next Steps

1. **Set up the secrets** in your GitHub repository
2. **Update the configuration** with your Docker Hub details
3. **Push to main branch** to trigger the first CI/CD run
4. **Monitor the workflow** in the Actions tab
5. **Verify the Docker image** is pushed to Docker Hub

For more advanced configurations, see the [GitHub Actions documentation](https://docs.github.com/en/actions).