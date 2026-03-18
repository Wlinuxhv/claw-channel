#!/bin/bash
# 🦞 Claw Channel Server 快速启动脚本

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "🦞 Claw Channel Server"
echo "======================"
echo ""

# 检查 Go 环境
if ! command -v go &> /dev/null; then
    echo "❌ Go 未安装，请先安装 Go 1.19+"
    exit 1
fi

echo "✅ Go 版本：$(go version)"

# 检查是否已编译
if [ ! -f "claw-server" ]; then
    echo ""
    echo "📦 首次运行，正在编译..."
    go mod download
    go build -o claw-server cmd/server/main.go
    echo "✅ 编译完成"
fi

# 检查是否已运行
if pgrep -f "claw-server" > /dev/null; then
    echo ""
    echo "⚠️  服务端已在运行"
    ps aux | grep claw-server | grep -v grep
    echo ""
    read -p "是否重启服务？(y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 0
    fi
    
    echo "🔄 停止服务..."
    pkill -f "claw-server"
    sleep 2
fi

# 创建数据目录
mkdir -p data

# 启动服务
echo ""
echo "🚀 启动服务端..."
./claw-server > server.log 2>&1 &
PID=$!

echo "✅ 服务端已启动"
echo "   PID: $PID"
echo "   端口：8080"
echo "   日志：server.log"
echo ""

# 等待服务启动
sleep 3

# 健康检查
echo "🔍 健康检查..."
if curl -s http://localhost:8080/api/health > /dev/null; then
    echo "✅ 服务运行正常"
    echo ""
    echo "📊 服务信息:"
    curl -s http://localhost:8080/api/health | python3 -m json.tool 2>/dev/null || curl -s http://localhost:8080/api/health
else
    echo "❌ 服务启动失败，请查看日志：server.log"
    exit 1
fi

echo ""
echo "📝 常用命令:"
echo "   查看日志：tail -f server.log"
echo "   停止服务：pkill -f claw-server"
echo "   重启服务：./start.sh"
echo ""
echo "🦞 服务端已就绪！"
