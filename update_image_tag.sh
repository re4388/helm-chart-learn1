#!/bin/bash

# 腳本用於更新 ArgoCD 應用程式的映像標籤
# 使用方法: ./update_image_tag.sh <new-tag>

if [ $# -eq 0 ]; then
    echo "使用方法: $0 <image-tag>"
    echo "範例: $0 main-abc1234"
    exit 1
fi

NEW_TAG=$1
ARGOCD_APP_FILE="k8s/argocd/argocd-application.yaml"
VALUES_FILE="k8s/helm-chart/demo-app/values.yaml"
VALUES_PROD_FILE="k8s/helm-chart/demo-app/values-production.yaml"

echo "更新映像標籤為: $NEW_TAG"

# 更新 ArgoCD 應用程式配置
sed -i "s/value: \"main-[a-f0-9]*\"/value: \"$NEW_TAG\"/" $ARGOCD_APP_FILE

# 更新 Helm values 檔案
sed -i "s/tag: \"main-[a-f0-9]*\"/tag: \"$NEW_TAG\"/" $VALUES_FILE

# 更新生產環境 values 檔案
if [ -f $VALUES_PROD_FILE ]; then
    sed -i "s/tag: \"main-[a-f0-9]*\"/tag: \"$NEW_TAG\"/" $VALUES_PROD_FILE
fi

echo "映像標籤已更新完成！"
echo "請提交變更並推送到 Git 倉庫以觸發 ArgoCD 同步。"