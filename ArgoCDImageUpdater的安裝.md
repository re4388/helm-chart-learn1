1. 下載安裝 Manifests：

Argo CD Image Updater 的安裝 Manifests 通常可以在其 GitHub 倉庫中找到。你可以使用 kubectl apply -f 直接從 URL 安裝。

kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj-labs/argocd-image-updater/stable/manifests/install.yaml

這個命令會將 Image Updater 的 Deployment、Service Account、Role、RoleBinding 等資源部署到 argocd 命名空間。

1. 確認 Pod 運行狀態：
部署後，請等待幾分鐘，然後再次檢查 argocd-image-updater Pod 的狀態：

kubectl get pods -n argocd | grep argocd-image-updater

你應該會看到一個 argocd-image-updater 開頭的 Pod 正在運行（狀態為 Running）。


1. 配置 Docker Hub 憑證 (如果你的映像是私有的)：
如果你的 re4388/demo-app 映像是私有的，Argo CD Image Updater 需要憑證才能訪問 Docker Hub。這通常是透過創建一個 Secret 並將其引用到 Image Updater 的配置中來完成的。


    * 創建 Docker Hub Secret (如果尚未創建)：


1         kubectl create secret docker-registry regcred \
--namespace argocd \
3           --docker-server=https://index.docker.io/v1/ \
4           --docker-username=<your-docker-username> \
5           --docker-password=<your-docker-password> \
6           --docker-email=<your-docker-email>

       將 <your-docker-username>、<your-docker-password> 和 <your-docker-email> 替換為你的 Docker Hub 憑證。


    * 配置 Image Updater 使用 Secret：
       你需要編輯 argocd-image-updater 的 Deployment，將這個 Secret 掛載到 Pod 中，並配置 Image Updater 使用它。這通常是透過修改 argocd-image-updater 的 Deployment 中的 args 或
env 變數來實現的。

       注意：對於公共映像（如 re4388/demo-app 似乎是公共的），通常不需要額外的憑證配置。


1. 配置 Argo CD Image Updater 的 Git 寫入權限：
   Argo CD Image Updater 需要有權限將更新後的 argocd-application.yaml 提交回你的 Git 倉庫。這通常是透過配置一個 Git 憑證 Secret 並將其引用到 Image Updater 的配置中來完成的。


    * 創建 Git Secret：


kubectl create secret generic argocd-image-updater-git-creds \
--namespace argocd \
--from-literal=username=re4388 \
--from-literal=password=<use your-git-personal-access-token in github password manger>

       將 <your-git-username> 和 <your-git-personal-access-token> 替換為你的 GitHub 用戶名和個人訪問令牌 (Personal Access Token)，該令牌需要有寫入你倉庫的權限。


    * 配置 Image Updater 使用 Git Secret：
       你需要編輯 argocd-image-updater 的 Deployment，將這個 Secret 掛載到 Pod 中，並配置 Image Updater 使用它。這通常是透過修改 argocd-image-updater 的 Deployment 中的 args 或
env 變數來實現的。

       注意：這一步是必須的，因為 Image Updater 需要寫回 Git 倉庫。