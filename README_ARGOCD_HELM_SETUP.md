# 🚀 Complete ArgoCD + Helm Setup Guide

This repository provides a complete setup for deploying a Spring Boot application with MongoDB and MinIO using ArgoCD and Helm charts on Kubernetes.

## 📁 Project Structure

```
├── application/                 # Spring Boot application source code
├── k8s/
│   ├── argocd/                 # ArgoCD configuration
│   │   ├── argocd-application.yaml
│   │   ├── argocd-values.yaml
│   │   ├── ARGOCD_SETUP_GUIDE.md
│   │   └── README.md
│   ├── helm-chart/             # Helm chart for the application
│   │   └── demo-app/
│   │       ├── Chart.yaml
│   │       ├── values.yaml
│   │       ├── values-production.yaml
│   │       └── templates/
│   └── KUBERNETES_SETUP_GUIDE.md
└── README_ARGOCD_HELM_SETUP.md # This file
```

## 🎯 What You'll Get

After following this setup:

✅ **ArgoCD** installed and configured for GitOps  
✅ **Automatic deployment** from your GitHub repository  
✅ **Spring Boot application** running in Kubernetes  
✅ **MongoDB** database automatically provisioned  
✅ **MinIO** object storage automatically provisioned  
✅ **Continuous deployment** when you push to main branch  
✅ **Health checks** and monitoring endpoints  
✅ **Production-ready** configuration options  

## 🚀 Quick Start (3 Commands)

### Prerequisites
- Kubernetes cluster running
- `kubectl` and `helm` installed
- Docker Hub account

### 1. Update Configuration
```bash
# Clone your repository
git clone https://github.com/re4388/helm-chart-learn1.git
cd helm-chart-learn1

# Update Docker repository (replace YOUR-USERNAME)
sed -i 's/your-dockerhub-username/YOUR-USERNAME/g' k8s/argocd/argocd-application.yaml
```

### 2. Build and Push Application
```bash
cd application
mvn clean package
docker build -t YOUR-USERNAME/demo-app:latest .
docker push YOUR-USERNAME/demo-app:latest
```

### 3. Deploy Everything
```bash
# Install ArgoCD
helm repo add argo https://argoproj.github.io/argo-helm
kubectl create namespace argocd
helm install argocd argo/argo-cd --namespace argocd --values k8s/argocd/argocd-values.yaml

# Deploy your application
kubectl apply -f k8s/argocd/argocd-application.yaml

# Watch deployment
kubectl get pods -n demo-app -w
```

## 🔧 Detailed Setup Instructions

### Step 1: Prepare Your Environment

1. **Ensure you have a Kubernetes cluster:**
   ```bash
   kubectl cluster-info
   ```

2. **Install required tools:**
   ```bash
   # Helm
   curl https://get.helm.sh/helm-v3.12.0-linux-amd64.tar.gz | tar xz
   sudo mv linux-amd64/helm /usr/local/bin/
   
   # Verify installation
   helm version
   kubectl version --client
   ```

### Step 2: Configure Your Application

1. **Update the Docker repository:**
   ```bash
   # Edit k8s/argocd/argocd-application.yaml
   # Change: your-dockerhub-username/demo-app
   # To: YOUR-ACTUAL-USERNAME/demo-app
   ```

2. **Build and push your Docker image:**
   ```bash
   cd application
   mvn clean package
   docker build -t YOUR-USERNAME/demo-app:latest .
   docker login
   docker push YOUR-USERNAME/demo-app:latest
   ```

### Step 3: Install ArgoCD

```bash
# Add Helm repository
helm repo add argo https://argoproj.github.io/argo-helm
helm repo update

# Create namespace
kubectl create namespace argocd

# Install ArgoCD with custom values
helm install argocd argo/argo-cd \
  --namespace argocd \
  --values k8s/argocd/argocd-values.yaml \
  --wait

# Get admin password
echo "ArgoCD Admin Password:"
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d && echo
```

### Step 4: Deploy Your Application

```bash
# Apply the ArgoCD application
kubectl apply -f k8s/argocd/argocd-application.yaml

# Check application status
kubectl get applications -n argocd

# Watch pods being created
kubectl get pods -n demo-app -w
```

### Step 5: Access Your Services

```bash
# Access ArgoCD UI
kubectl port-forward svc/argocd-server -n argocd 8080:443 &
echo "ArgoCD UI: https://localhost:8080"
echo "Username: admin"
echo "Password: (from step 3)"

# Access your application
kubectl port-forward svc/demo-app -n demo-app 8081:8080 &
echo "Application: http://localhost:8081"

# Test application health
curl http://localhost:8081/actuator/health
```

## 🗄️ Database and Storage Access

### MongoDB
```bash
# Port forward to MongoDB
kubectl port-forward svc/demo-app-mongodb -n demo-app 27017:27017 &

# Test via application API
curl -X POST http://localhost:8081/api/posts \
  -H "Content-Type: application/json" \
  -d '{"id":"test1","title":"Hello Kubernetes","content":"Deployed with ArgoCD!"}'

curl http://localhost:8081/api/posts
```

### MinIO
```bash
# Port forward to MinIO console
kubectl port-forward svc/demo-app-minio -n demo-app 9001:9001 &
echo "MinIO Console: http://localhost:9001"
echo "Username: minioadmin"
echo "Password: minioadmin123"

# Test file upload
curl -X POST \
  -F "file=@test.txt" \
  -F "bucket=demo-bucket" \
  -F "uploadedBy=kubernetes-user" \
  http://localhost:8081/api/files/upload
```

## 🔄 How Continuous Deployment Works

1. **You push code** to your GitHub repository main branch
2. **ArgoCD detects changes** (polls every 3 minutes by default)
3. **ArgoCD syncs** the latest Helm chart from your repository
4. **Kubernetes deploys** the updated application automatically
5. **Health checks ensure** the deployment is successful

### Triggering a Deployment

```bash
# Make a change to your application
echo "// Updated at $(date)" >> application/src/main/java/com/example/demoapp/DemoAppApplication.java

# Build and push new image
cd application
mvn clean package
docker build -t YOUR-USERNAME/demo-app:$(date +%s) .
docker push YOUR-USERNAME/demo-app:$(date +%s)

# Update the image tag in ArgoCD (or use 'latest' for automatic updates)
# ArgoCD will automatically detect and deploy the changes
```

## 🔍 Monitoring and Troubleshooting

### Check Application Status
```bash
# All resources in demo-app namespace
kubectl get all -n demo-app

# Application logs
kubectl logs -f deployment/demo-app -n demo-app

# ArgoCD application status
kubectl get application demo-app -n argocd -o yaml
```

### Common Issues and Solutions

1. **Image Pull Errors:**
   ```bash
   # Verify image exists
   docker pull YOUR-USERNAME/demo-app:latest
   
   # Check pod events
   kubectl describe pod <pod-name> -n demo-app
   ```

2. **Database Connection Issues:**
   ```bash
   # Check MongoDB pod
   kubectl logs -f deployment/demo-app-mongodb -n demo-app
   
   # Test connectivity from app pod
   kubectl exec -it deployment/demo-app -n demo-app -- nc -zv demo-app-mongodb 27017
   ```

3. **ArgoCD Sync Issues:**
   ```bash
   # Manual sync
   kubectl patch application demo-app -n argocd --type merge -p '{"spec":{"syncPolicy":{"automated":null}}}'
   kubectl patch application demo-app -n argocd --type merge -p '{"operation":{"sync":{}}}'
   ```

## 🚀 Production Deployment

For production environments, use the production values:

```bash
# Deploy with production configuration
helm upgrade demo-app k8s/helm-chart/demo-app \
  --namespace demo-app \
  --values k8s/helm-chart/demo-app/values-production.yaml \
  --install
```

Production features include:
- **Multiple replicas** for high availability
- **Resource limits** and requests
- **Horizontal Pod Autoscaling**
- **Ingress configuration** for external access
- **Enhanced security** settings
- **Monitoring** and metrics collection

## 📊 Helm Chart Features

The included Helm chart provides:

- ✅ **Configurable deployments** with health checks
- ✅ **Service discovery** and load balancing
- ✅ **ConfigMaps** for application configuration
- ✅ **Secrets management** for sensitive data
- ✅ **Ingress support** for external access
- ✅ **Horizontal Pod Autoscaling**
- ✅ **Resource management** (limits/requests)
- ✅ **MongoDB dependency** (Bitnami chart)
- ✅ **MinIO dependency** (Bitnami chart)

## 🔧 Customization

### Environment-Specific Values

```bash
# Development
helm install demo-app k8s/helm-chart/demo-app --values k8s/helm-chart/demo-app/values.yaml

# Production
helm install demo-app k8s/helm-chart/demo-app --values k8s/helm-chart/demo-app/values-production.yaml

# Custom values
helm install demo-app k8s/helm-chart/demo-app --set image.tag=v2.0.0 --set replicaCount=5
```

### Adding New Dependencies

```yaml
# In Chart.yaml
dependencies:
  - name: redis
    version: "17.3.7"
    repository: "https://charts.bitnami.com/bitnami"
    condition: redis.enabled
```

## 📚 Additional Resources

- **[Detailed Setup Guide](k8s/KUBERNETES_SETUP_GUIDE.md)** - Complete step-by-step instructions
- **[ArgoCD Setup Guide](k8s/argocd/ARGOCD_SETUP_GUIDE.md)** - ArgoCD-specific configuration
- **[Application README](application/README.md)** - Spring Boot application details

## 🆘 Getting Help

If you encounter issues:

1. **Check the logs** of all components:
   ```bash
   kubectl logs -f deployment/demo-app -n demo-app
   kubectl logs -f deployment/argocd-server -n argocd
   ```

2. **Verify ArgoCD application status:**
   ```bash
   kubectl get application demo-app -n argocd
   kubectl describe application demo-app -n argocd
   ```

3. **Check resource status:**
   ```bash
   kubectl get events -n demo-app --sort-by='.lastTimestamp'
   kubectl top pods -n demo-app
   ```

## 🎉 Success Indicators

You'll know everything is working when:

- ✅ All pods in `demo-app` namespace are `Running`
- ✅ ArgoCD shows application as `Healthy` and `Synced`
- ✅ Application health endpoint returns `UP`: `curl http://localhost:8081/actuator/health`
- ✅ You can create posts via API: `curl -X POST http://localhost:8081/api/posts -H "Content-Type: application/json" -d '{"id":"test","title":"Success!","content":"ArgoCD + Helm working!"}'`
- ✅ You can upload files to MinIO: `curl -X POST -F "file=@test.txt" -F "bucket=demo-bucket" http://localhost:8081/api/files/upload`

---

**🎊 Congratulations!** You now have a production-ready Kubernetes deployment with GitOps using ArgoCD and Helm!