#!/bin/bash

# 簡易腳本，用於測試 MinIO 檔案上傳 API
# 用法：./scripts/test_file_upload.sh [bucket_name] [uploader_name]

set -e  # Exit on any error

# Configuration
API_BASE_URL="http://localhost:8080"
BUCKET_NAME="${1:-test-bucket}"
UPLOADER_NAME="${2:-test-user}"

# 輸出顏色設定
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # 無顏色

echo -e "${BLUE}=== MinIO 檔案上傳測試腳本 ===${NC}"
echo ""

# 檢查應用程式是否正在運行
echo -e "${YELLOW}正在檢查應用程式是否運行中...${NC}"
if ! curl -s -f "${API_BASE_URL}/health" > /dev/null; then
    echo -e "${RED}❌ 應用程式未在 ${API_BASE_URL} 運行${NC}"
    echo -e "${YELLOW}請先啟動應用程式：mvn spring-boot:run${NC}"
    exit 1
fi
echo -e "${GREEN}✅ 應用程式正在運行${NC}"
echo ""

# 產生隨機檔名與內容
RANDOM_ID=$(date +%s%N | cut -b1-13)  # 基於時間戳的隨機 ID
FILENAME="test_file_${RANDOM_ID}.txt"
RANDOM_WORDS=("hello" "world" "test" "file" "upload" "minio" "spring" "boot" "hexagonal" "architecture")
RANDOM_CONTENT=""

# 以 3-5 個隨機單字產生內容
NUM_WORDS=$((RANDOM % 3 + 3))  # 3 到 5 個單字
for i in $(seq 1 $NUM_WORDS); do
    WORD_INDEX=$((RANDOM % ${#RANDOM_WORDS[@]}))
    RANDOM_CONTENT="${RANDOM_CONTENT}${RANDOM_WORDS[$WORD_INDEX]} "
done

# 在內容中加入時間戳
RANDOM_CONTENT="${RANDOM_CONTENT}(Generated at: $(date))"

echo -e "${YELLOW}正在建立測試檔案...${NC}"
echo "📄 檔名: ${FILENAME}"
echo "📝 內容: ${RANDOM_CONTENT}"
echo "${RANDOM_CONTENT}" > "${FILENAME}"
echo -e "${GREEN}✅ 測試檔案已建立${NC}"
echo ""

# 使用 curl 上傳檔案
echo -e "${YELLOW}正在上傳檔案到 MinIO...${NC}"
echo "🚀 上傳到 bucket: ${BUCKET_NAME}"
echo "👤 上傳者: ${UPLOADER_NAME}"
echo ""

UPLOAD_RESPONSE=$(curl -s -X POST \
  -F "file=@${FILENAME}" \
  -F "bucket=${BUCKET_NAME}" \
  -F "uploadedBy=${UPLOADER_NAME}" \
  "${API_BASE_URL}/api/files/upload")

# 檢查上傳是否成功
if echo "${UPLOAD_RESPONSE}" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ 檔案上傳成功！${NC}"
    echo ""
    echo -e "${BLUE}📋 上傳回應:${NC}"
    echo "${UPLOAD_RESPONSE}" | jq '.' 2>/dev/null || echo "${UPLOAD_RESPONSE}"
    
    # 取得上傳後的檔名以便後續操作
    UPLOADED_FILENAME=$(echo "${UPLOAD_RESPONSE}" | jq -r '.fileName' 2>/dev/null || echo "")
    
    if [ "${UPLOADED_FILENAME}" != "null" ] && [ "${UPLOADED_FILENAME}" != "" ]; then
        echo ""
        echo -e "${BLUE}🔗 此檔案的常用指令:${NC}"
        echo "📥 下載: curl -o downloaded_${FILENAME} '${API_BASE_URL}/api/files/download/${BUCKET_NAME}/${UPLOADED_FILENAME}'"
        echo "📋 取得中繼資料: curl '${API_BASE_URL}/api/files/metadata/${BUCKET_NAME}/${UPLOADED_FILENAME}'"
        echo "❓ 檢查是否存在: curl '${API_BASE_URL}/api/files/exists/${BUCKET_NAME}/${UPLOADED_FILENAME}'"
        echo "🗑️ 刪除: curl -X DELETE '${API_BASE_URL}/api/files/${BUCKET_NAME}/${UPLOADED_FILENAME}'"
        echo "📂 列出 bucket: curl '${API_BASE_URL}/api/files/list/${BUCKET_NAME}'"
else
    echo -e "${RED}❌ File upload failed!${NC}"
    echo -e "${RED}❌ 檔案上傳失敗！${NC}"
    echo -e "${RED}📋 Error Response:${NC}"
    echo -e "${RED}📋 錯誤回應:${NC}"
fi

# Clean up temporary file
# 清理暫存檔案
echo -e "${YELLOW}Cleaning up temporary file...${NC}"
echo -e "${YELLOW}正在清理暫存檔案...${NC}"
echo -e "${GREEN}✅ Cleanup completed${NC}"
echo -e "${GREEN}✅ 清理完成${NC}"
echo ""
echo -e "${BLUE}=== Test completed ===${NC}"
echo -e "${BLUE}=== 測試完成 ===${NC}"
echo -e "${YELLOW}💡 Tips:${NC}"
echo "• Run with custom bucket: ./scripts/test_file_upload.sh my-bucket"
echo "• Run with custom uploader: ./scripts/test_file_upload.sh my-bucket john.doe"
echo "• Check MinIO console: http://localhost:9003 (minioadmin/minioadmin)"
echo "• View all files in bucket: curl '${API_BASE_URL}/api/files/list/${BUCKET_NAME}'"