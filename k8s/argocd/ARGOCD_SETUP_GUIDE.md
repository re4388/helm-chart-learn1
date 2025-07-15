# ArgoCD Setup Guide for Demo App

This guide will help you set up ArgoCD with Helm to automatically deploy your Spring Boot application with MongoDB and MinIO from your GitHub repository.

## 📋 Prerequisites

- Kubernetes cluster (v1.20+)
- kubectl configured and connected to your cluster
- Helm 3.x installed
- Docker Hub account (for storing your application images)

## 🚀 Step-by-Step Installation

### 1. Install ArgoCD using Helm

```bash
# Add ArgoCD Helm repository
helm repo add argo https://argoproj.github.io/argo-helm
helm repo update

# Create ArgoCD namespace
kubectl create namespace argocd

# Install ArgoCD with custom values
helm install argocd argo/argo-cd \
  --namespace argocd \
  --values k8s/argocd/argocd-values.yaml \
  --wait
```

### 2. Access ArgoCD UI

```bash
# Get the initial admin password
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d && echo

# Port forward to access ArgoCD UI
kubectl port-forward svc/argocd-server -n argocd 8080:443

# Access ArgoCD at: https://localhost:8080
# Username: admin
# Password: (from the command above)
```

### 3. Update Your Docker Image Repository

Before deploying, update the image repository in the ArgoCD application:

```bash
# Edit the ArgoCD application file
vim k8s/argocd/argocd-application.yaml

# Update the image.repository parameter to your Docker Hub username:
# - name: image.repository
#   value: "YOUR-DOCKERHUB-USERNAME/demo-app"
```

### 4. Deploy the Application

```bash
# Apply the ArgoCD application
kubectl apply -f k8s/argocd/argocd-application.yaml

# Check application status
kubectl get applications -n argocd
```

### 5. Monitor Deployment

```bash
# Watch the application sync
kubectl get pods -n demo-app -w

# Check ArgoCD application status
argocd app get demo-app

# Or use the UI at https://localhost:8080
```

## 🔧 Configuration Details

### Image Repository Setup

1. **Build and push your Docker image:**
```bash
# Build the application
cd application
mvn clean package

# Build Docker image
docker build -t YOUR-DOCKERHUB-USERNAME/demo-app:latest .

# Push to Docker Hub
docker push YOUR-DOCKERHUB-USERNAME/demo-app:latest
```

2. **Update ArgoCD application:**
```yaml
# In k8s/argocd/argocd-application.yaml
helm:
  parameters:
    - name: image.repository
      value: "YOUR-DOCKERHUB-USERNAME/demo-app"
    - name: image.tag
      value: "latest"
```

### Automatic Sync Configuration

The ArgoCD application is configured for automatic sync:

```yaml
syncPolicy:
  automated:
    prune: true      # Remove resources not in Git
    selfHeal: true   # Automatically fix drift
  syncOptions:
    - CreateNamespace=true
    - Validate=true
```

## 🗄️ Database and Storage Setup

### MongoDB Configuration

MongoDB is automatically deployed as a dependency in the Helm chart:

```yaml
# In k8s/helm-chart/demo-app/values.yaml
mongodb:
  enabled: true
  auth:
    enabled: true
    rootUser: admin
    rootPassword: "admin123"
    username: demo_user
    password: "demo_password"
    database: demo
```

### MinIO Configuration

MinIO is automatically deployed as a dependency:

```yaml
# In k8s/helm-chart/demo-app/values.yaml
minio:
  enabled: true
  auth:
    rootUser: minioadmin
    rootPassword: "minioadmin123"
  defaultBuckets: "demo-bucket"
```

## 🔄 CI/CD Workflow

When you push code to your GitHub repository:

1. **GitHub Actions** (you need to set this up) builds and pushes Docker image
2. **ArgoCD** detects changes in the Git repository
3. **Automatic sync** pulls the latest Helm chart
4. **Deployment** updates the Kubernetes cluster

### Setting up GitHub Actions (Optional)

Create `.github/workflows/build-and-deploy.yml`:

```yaml
name: Build and Deploy
on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Build with Maven
        run: |
          cd application
          mvn clean package
          
      - name: Build Docker image
        run: |
          cd application
          docker build -t ${{ secrets.DOCKER_USERNAME }}/demo-app:${{ github.sha }} .
          docker tag ${{ secrets.DOCKER_USERNAME }}/demo-app:${{ github.sha }} ${{ secrets.DOCKER_USERNAME }}/demo-app:latest
          
      - name: Push to Docker Hub
        run: |
          echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
          docker push ${{ secrets.DOCKER_USERNAME }}/demo-app:${{ github.sha }}
          docker push ${{ secrets.DOCKER_USERNAME }}/demo-app:latest
```

## 🔍 Verification and Testing

### 1. Check Application Health

```bash
# Check all pods are running
kubectl get pods -n demo-app

# Check application health
kubectl port-forward svc/demo-app -n demo-app 8080:8080
curl http://localhost:8080/actuator/health
```

### 2. Test MongoDB Connection

```bash
# Port forward to MongoDB
kubectl port-forward svc/demo-app-mongodb -n demo-app 27017:27017

# Test API endpoints
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"id":"test1","title":"Test Post","content":"Test content"}'

curl http://localhost:8080/api/posts
```

### 3. Test MinIO Connection

```bash
# Port forward to MinIO console
kubectl port-forward svc/demo-app-minio -n demo-app 9001:9001

# Access MinIO console at http://localhost:9001
# Username: minioadmin
# Password: minioadmin123

# Test file upload
curl -X POST \
  -F "file=@test.txt" \
  -F "bucket=demo-bucket" \
  -F "uploadedBy=testuser" \
  http://localhost:8080/api/files/upload
```

## 🛠️ Troubleshooting

### Common Issues

1. **ArgoCD can't access Git repository:**
   - Ensure the repository is public or configure SSH keys
   - Check the repository URL in `argocd-application.yaml`

2. **Image pull errors:**
   - Verify Docker image exists and is accessible
   - Check image repository and tag in values

3. **Database connection issues:**
   - Check MongoDB pod logs: `kubectl logs -f deployment/demo-app-mongodb -n demo-app`
   - Verify connection strings in application configuration

4. **MinIO connection issues:**
   - Check MinIO pod logs: `kubectl logs -f deployment/demo-app-minio -n demo-app`
   - Verify MinIO endpoint configuration

### Useful Commands

```bash
# View ArgoCD application details
argocd app get demo-app

# Sync application manually
argocd app sync demo-app

# View application logs
kubectl logs -f deployment/demo-app -n demo-app

# Describe problematic pods
kubectl describe pod <pod-name> -n demo-app

# Check events
kubectl get events -n demo-app --sort-by='.lastTimestamp'
```

## 📚 Next Steps

1. **Set up monitoring** with Prometheus and Grafana
2. **Configure ingress** for external access
3. **Set up backup** for MongoDB and MinIO data
4. **Implement proper secrets management** with Kubernetes secrets or external secret operators
5. **Add resource limits and requests** for production workloads

## 🔗 Useful Links

- [ArgoCD Documentation](https://argo-cd.readthedocs.io/)
- [Helm Documentation](https://helm.sh/docs/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Bitnami MongoDB Chart](https://github.com/bitnami/charts/tree/main/bitnami/mongodb)
- [Bitnami MinIO Chart](https://github.com/bitnami/charts/tree/main/bitnami/minio)