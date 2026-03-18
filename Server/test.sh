#!/bin/bash

# Claw Channel 服务端测试脚本

BASE_URL="http://localhost:8080"

echo "🦞 Claw Channel 服务端测试"
echo "=========================="
echo ""

# 测试 1: 健康检查
echo "1️⃣  测试健康检查..."
HEALTH_RESPONSE=$(curl -s "$BASE_URL/api/health")
echo "响应：$HEALTH_RESPONSE"
echo ""

# 测试 2: 用户登录（需要有效推荐码）
echo "2️⃣  测试用户登录..."
echo "注意：需要先生成推荐码"
echo ""

# 测试 3: 管理员登录
echo "3️⃣  测试管理员登录..."
ADMIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/admin/login" \
  -H "Content-Type: application/json" \
  -d '{"password":"admin123"}')
echo "响应：$ADMIN_RESPONSE"
echo ""

# 测试 4: 生成推荐码
echo "4️⃣  测试生成推荐码..."
CODE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/admin/recommendation-codes" \
  -H "Content-Type: application/json")
echo "响应：$CODE_RESPONSE"
echo ""

# 提取推荐码
RECOMMENDATION_CODE=$(echo $CODE_RESPONSE | grep -o '"code":"[^"]*"' | cut -d'"' -f4)
echo "生成的推荐码：$RECOMMENDATION_CODE"
echo ""

# 测试 5: 使用推荐码登录
if [ ! -z "$RECOMMENDATION_CODE" ]; then
    echo "5️⃣  测试使用推荐码登录..."
    LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
      -H "Content-Type: application/json" \
      -d "{\"recommendation_code\":\"$RECOMMENDATION_CODE\"}")
    echo "响应：$LOGIN_RESPONSE"
    echo ""
fi

# 测试 6: 获取统计信息
echo "6️⃣  测试获取统计信息..."
STATS_RESPONSE=$(curl -s "$BASE_URL/api/admin/stats")
echo "响应：$STATS_RESPONSE"
echo ""

echo "✅ 测试完成！"
echo ""
echo "提示：WebSocket 测试需要使用 WebSocket 客户端工具"
echo "例如：wscat -c ws://localhost:8080/ws/chat?token=YOUR_TOKEN"
