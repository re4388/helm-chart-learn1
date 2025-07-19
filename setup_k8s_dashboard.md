```sh

# 新增 Kubernetes Dashboard 的 Helm 儲存庫
helm repo add kubernetes-dashboard https://kubernetes.github.io/dashboard/

# 更新 Helm 儲存庫列表
helm repo update

# 使用 Helm 安裝 Kubernetes Dashboard。我們將其安裝在 kubernetes-dashboard 命名空間中
# 預設情況下，Kubernetes Dashboard 不會自動建立一個 LoadBalancer 或 NodePort 服務。為了方便存取，我們將在安裝時將其服務類型設定為 NodePort
helm install kubernetes-dashboard kubernetes-dashboard/kubernetes-dashboard --create-namespace --namespace kubernetes-dashboard --set service.type=NodePort


# 為了安全地存取 Dashboard，我們需要建立一個服務帳戶 (Service Account) 和一個集群角色綁定 (ClusterRoleBinding)，讓 Dashboard 能夠讀取集群中的所有資源
# 建立 `dashboard-adminuser.yaml` 檔案 -> see dashboard-adminuser.yaml

kubectl apply -f dashboard-adminuser.yaml

# 需要獲取 admin-user 服務帳戶的認證 Token。您將使用這個 Token 登入 Dashboard
kubectl -n kubernetes-dashboard create token admin-user

eyJhbGciOiJSUzI1NiIsImtpZCI6IlAwWHBaTlVlT2xzdV9LVWpDamZMV1UyRUJOUV9FM2dGcm5nb0FuQWw4bW8ifQ.eyJhdWQiOlsiaHR0cHM6Ly9rdWJlcm5ldGVzLmRlZmF1bHQuc3ZjLmNsdXN0ZXIubG9jYWwiLCJrM3MiXSwiZXhwIjoxNzUyOTA5MzgyLCJpYXQiOjE3NTI5MDU3ODIsImlzcyI6Imh0dHBzOi8va3ViZXJuZXRlcy5kZWZhdWx0LnN2Yy5jbHVzdGVyLmxvY2FsIiwianRpIjoiOTBjNTU2MTYtMmU5Ny00YzJkLWFiMTctN2Y3OWM3NmQzNDA4Iiwia3ViZXJuZXRlcy5pbyI6eyJuYW1lc3BhY2UiOiJrdWJlcm5ldGVzLWRhc2hib2FyZCIsInNlcnZpY2VhY2NvdW50Ijp7Im5hbWUiOiJhZG1pbi11c2VyIiwidWlkIjoiZjg5MzA4NzMtNDRjNC00NDVjLWIzYTEtNDliNTAwMDA2YzVlIn19LCJuYmYiOjE3NTI5MDU3ODIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlcm5ldGVzLWRhc2hib2FyZDphZG1pbi11c2VyIn0.afy0A3FF4gqgcaGsYSfRfKRLKpBU4V-h-L0cRSygpkNvztDZkf6kTzEj5f59eROBu7X-lK19KYmRfKyv1cDoIgjswGC5RtAu2h2B_g5V_Q0S5BxKKZ3T5-oYS5FjUnh5LqgRHZ95rE2SPu5yORtoeOjEGi55CRek0j6US7qTXH7rhmUHkKHM_KbvF0Ar2QWFxpRMqsBV08qCaml28t119gwqyY0iTWg1ZHF9In4yn2cFAokR2Jbs8XUe2m_UAkYf4bSumzr9db5c3vSCqWAXkY9obmNdKYbACJuFn4zTMos2rCMGr9kDnPWYiJxS7myFPw0w5tkunimL7uJqLinhxA


# 由於我們將服務類型設定為 NodePort，您可以透過 kubectl port-forward 來存取它
# 列出 kubernetes-dashboard 命名空間中的所有服務，以找到正確的服務名稱
kubectl get services -n kubernetes-dashboard

服務名稱是 kubernetes-dashboard-kong-proxy。這是 Dashboard 的主要服務，它監聽 443 埠 (HTTPS)

kubectl port-forward svc/kubernetes-dashboard-kong-proxy 8085:443 --address=0.0.0.0 -n kubernetes-dashboard

# 存取 Dashboard
https://localhost:8085

# need to use Brave Browser 

```