# Complete Kubernetes Setup Guide

This guide provides step-by-step instructions to set up your Spring Boot application with MongoDB and MinIO using ArgoCD and Helm charts.

## 🎯 Overview

After following this guide, you will have:
- ArgoCD installed and configured
- Automatic deployment from your GitHub repository
- Spring Boot application running with MongoDB and MinIO
- Continuous deployment when you push to the main branch

## 📋 Prerequisites

1. **Kubernetes Cluster** (v1.20+)
   - Local: minikube, kind, or Docker Desktop
   - Cloud: EKS, GKE, AKS, or any managed Kubernetes service

2. **Tools Installed:**
   ```bash
   # Check if tools are installed
   kubectl version --client
   helm version
   docker --version
   ```

3. **Docker Hub Account** for storing your application images

## 🚀 Quick Start (5 Steps)

### Step 1: Prepare Your Repository

1. **Fork or clone** your repository: `https://github.com/re4388/helm-chart-learn1/`

2. **Update Docker image repository** in ArgoCD application:
   ```bash
   # Edit the file
   vim k8s/argocd/argocd-application.yaml
   
   # Change this line:
   # value: "your-dockerhub-username/demo-app"
   # To:
   # value: "YOUR-ACTUAL-DOCKERHUB-USERNAME/demo-app"
   ```

### Step 2: Build and Push Your Application

```bash
# Navigate to application directory
cd application

# Build the application
mvn clean package

# Build Docker image
docker build -t YOUR-DOCKERHUB-USERNAME/demo-app:latest .

# Login to Docker Hub
docker login

# Push the image
docker push YOUR-DOCKERHUB-USERNAME/demo-app:latest
```

### Step 3: Install ArgoCD

```bash
# Add Helm repository
helm repo add argo https://argoproj.github.io/argo-helm
helm repo update

# Create namespace
kubectl create namespace argocd

# Install ArgoCD
helm install argocd argo/argo-cd \
  --namespace argocd \
  --values k8s/argocd/argocd-values.yaml \
  --wait

# Get admin password
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d && echo
```

### Step 4: Deploy Your Application

```bash
# Apply ArgoCD application
kubectl apply -f k8s/argocd/argocd-application.yaml

# Watch the deployment
kubectl get pods -n demo-app -w
```

### Step 5: Access Your Application

```bash
# Port forward to your application
kubectl port-forward svc/demo-app -n demo-app 8080:8080

# Test the application
curl http://localhost:8080/actuator/health

# Access ArgoCD UI
kubectl port-forward svc/argocd-server -n argocd 8080:443
# Visit: https://localhost:8080 (admin / password-from-step-3)
```

## 🔧 Detailed Configuration

### ArgoCD Configuration

The ArgoCD application is configured to:
- Monitor your GitHub repository
- Use the `main` branch (HEAD)
- Deploy from `k8s/helm-chart/demo-app/`
- Automatically sync changes
- Self-heal any configuration drift

### Helm Chart Structure

```
k8s/helm-chart/demo-app/
├── Chart.yaml              # Chart metadata and dependencies
├── values.yaml             # Default configuration
├── values-production.yaml  # Production overrides
└── templates/
    ├── deployment.yaml     # Application deployment
    ├── service.yaml        # Service definition
    ├── configmap.yaml      # Configuration
    ├── serviceaccount.yaml # Service account
    ├── ingress.yaml        # Ingress (optional)
    ├── hpa.yaml           # Horizontal Pod Autoscaler
    └── _helpers.tpl       # Template helpers
```

### Dependencies

The chart automatically installs:
- **MongoDB** (Bitnami chart)
- **MinIO** (Bitnami chart)

## 🗄️ Database and Storage Access

### MongoDB Access

```bash
# Port forward to MongoDB
kubectl port-forward svc/demo-app-mongodb -n demo-app 27017:27017

# Connect using MongoDB client
mongosh mongodb://demo_user:demo_password@localhost:27017/demo

# Test via application API
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"id":"test1","title":"Hello World","content":"Test content"}'

curl http://localhost:8080/api/posts
```

### MinIO Access

```bash
# Port forward to MinIO console
kubectl port-forward svc/demo-app-minio -n demo-app 9001:9001

# Access MinIO Console: http://localhost:9001
# Username: minioadmin
# Password: minioadmin123

# Test file upload via API
curl -X POST \
  -F "file=@test.txt" \
  -F "bucket=demo-bucket" \
  -F "uploadedBy=testuser" \
  http://localhost:8080/api/files/upload
```

## 🔄 Continuous Deployment Workflow

### Current Setup
1. You push code to GitHub main branch
2. ArgoCD detects the change (polls every 3 minutes)
3. ArgoCD syncs the latest Helm chart
4. Kubernetes deploys the updated application

### Enhanced CI/CD (Optional)

To automatically build and push Docker images, add this GitHub Actions workflow:

```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline
on:
  push:
    branches: [main]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
        
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Build application
        run: |
          cd application
          mvn clean package -DskipTests
          
      - name: Build and push Docker image
        env:
          DOCKER_USERNAME: ${{ secrets.DOCKER_USERNAME }}
          DOCKER_PASSWORD: ${{ secrets.DOCKER_PASSWORD }}
        run: |
          cd application
          echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
          docker build -t $DOCKER_USERNAME/demo-app:${{ github.sha }} .
          docker tag $DOCKER_USERNAME/demo-app:${{ github.sha }} $DOCKER_USERNAME/demo-app:latest
          docker push $DOCKER_USERNAME/demo-app:${{ github.sha }}
          docker push $DOCKER_USERNAME/demo-app:latest
```

## 🔍 Monitoring and Troubleshooting

### Health Checks

```bash
# Check all components
kubectl get pods -n demo-app
kubectl get pods -n argocd

# Application health
curl http://localhost:8080/actuator/health

# Detailed health with dependencies
curl http://localhost:8080/actuator/health | jq
```

### Common Issues and Solutions

1. **Image Pull Errors**
   ```bash
   # Check if image exists
   docker pull YOUR-DOCKERHUB-USERNAME/demo-app:latest
   
   # Update image in ArgoCD application
   kubectl edit application demo-app -n argocd
   ```

2. **Database Connection Issues**
   ```bash
   # Check MongoDB logs
   kubectl logs -f deployment/demo-app-mongodb -n demo-app
   
   # Check application logs
   kubectl logs -f deployment/demo-app -n demo-app
   ```

3. **ArgoCD Sync Issues**
   ```bash
   # Manual sync
   kubectl patch application demo-app -n argocd --type merge -p '{"operation":{"sync":{}}}'
   
   # Check ArgoCD logs
   kubectl logs -f deployment/argocd-server -n argocd
   ```

### Useful Commands

```bash
# View all resources
kubectl get all -n demo-app

# Describe problematic pods
kubectl describe pod <pod-name> -n demo-app

# View events
kubectl get events -n demo-app --sort-by='.lastTimestamp'

# Scale application
kubectl scale deployment demo-app -n demo-app --replicas=3

# Update image manually
kubectl set image deployment/demo-app demo-app=YOUR-USERNAME/demo-app:new-tag -n demo-app
```

## 🚀 Production Considerations

### 1. Use Production Values

```bash
# Deploy with production configuration
helm upgrade demo-app k8s/helm-chart/demo-app \
  --namespace demo-app \
  --values k8s/helm-chart/demo-app/values-production.yaml
```

### 2. Secure Secrets

Replace hardcoded passwords with Kubernetes secrets:

```bash
# Create secrets
kubectl create secret generic mongodb-secret \
  --from-literal=password=your-secure-password \
  -n demo-app

kubectl create secret generic minio-secret \
  --from-literal=password=your-secure-password \
  -n demo-app
```

### 3. Set up Ingress

```yaml
# In values-production.yaml
ingress:
  enabled: true
  className: "nginx"
  hosts:
    - host: demo-app.yourdomain.com
      paths:
        - path: /
          pathType: Prefix
```

### 4. Enable Monitoring

```bash
# Install Prometheus and Grafana
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack
```

## 📚 Next Steps

1. **Set up SSL/TLS** with cert-manager
2. **Configure backup** for MongoDB and MinIO
3. **Implement proper logging** with ELK stack
4. **Add resource quotas** and limits
5. **Set up network policies** for security
6. **Configure persistent volumes** for production storage

## 🆘 Getting Help

If you encounter issues:

1. **Check the logs** of all components
2. **Verify network connectivity** between services
3. **Ensure resource limits** are appropriate
4. **Check ArgoCD UI** for sync status
5. **Review Kubernetes events** for error messages

## 🔗 Useful Resources

- [ArgoCD Documentation](https://argo-cd.readthedocs.io/)
- [Helm Best Practices](https://helm.sh/docs/chart_best_practices/)
- [Kubernetes Troubleshooting](https://kubernetes.io/docs/tasks/debug-application-cluster/)
- [Spring Boot on Kubernetes](https://spring.io/guides/gs/spring-boot-kubernetes/)

---

**🎉 Congratulations!** You now have a fully automated Kubernetes deployment pipeline with ArgoCD!