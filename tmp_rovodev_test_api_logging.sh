#!/bin/bash

# API 日誌測試腳本
# 用於測試 AOP 日誌功能是否正常工作

echo "=== API 日誌功能測試腳本 ==="
echo "請確保應用程式正在運行在 localhost:8080"
echo ""

BASE_URL="http://localhost:8080"

# 測試基本日誌功能
echo "1. 測試基本日誌功能..."
curl -s "$BASE_URL/api/log-test/basic" | jq '.' || echo "請求失敗"
echo ""

# 測試帶參數的日誌
echo "2. 測試帶參數的日誌..."
curl -s -X POST "$BASE_URL/api/log-test/with-params?name=張三&age=25" \
  -H "Content-Type: application/json" \
  -d '{"department": "IT", "role": "developer"}' | jq '.' || echo "請求失敗"
echo ""

# 測試慢查詢日誌
echo "3. 測試慢查詢日誌（延遲 800ms）..."
curl -s "$BASE_URL/api/log-test/slow?delay=800" | jq '.' || echo "請求失敗"
echo ""

# 測試錯誤日誌
echo "4. 測試錯誤日誌..."
curl -s "$BASE_URL/api/log-test/error?throwError=true" || echo "預期的錯誤"
echo ""

# 測試最小日誌
echo "5. 測試最小日誌..."
curl -s "$BASE_URL/api/log-test/minimal" | jq '.' || echo "請求失敗"
echo ""

# 測試現有 API
echo "6. 測試現有 API 日誌..."
curl -s "$BASE_URL/hello/測試用戶" | jq '.' || echo "請求失敗"
echo ""

curl -s "$BASE_URL/api/posts" | jq '.' || echo "請求失敗"
echo ""

echo "=== 測試完成 ==="
echo "請檢查應用程式日誌以確認日誌記錄是否正常工作"
echo "日誌應該包含以下信息："
echo "- API_REQUEST_START: 請求開始"
echo "- API_REQUEST_SUCCESS: 請求成功"
echo "- API_REQUEST_ERROR: 請求錯誤（如果有）"
echo "- SLOW_API_DETECTED: 慢查詢警告（如果有）"