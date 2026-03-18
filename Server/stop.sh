#!/bin/bash
# 🦞 Claw Channel Server 停止脚本

echo "🦞 Claw Channel Server 停止脚本"
echo "=============================="
echo ""

# 查找进程
PIDS=$(pgrep -f "claw-server")

if [ -z "$PIDS" ]; then
    echo "⚠️  服务端未运行"
    exit 0
fi

echo "📋 运行中的进程:"
ps aux | grep claw-server | grep -v grep
echo ""

# 停止进程
echo "🛑 停止服务..."
pkill -f "claw-server"

# 等待进程结束
sleep 2

# 检查是否已停止
if pgrep -f "claw-server" > /dev/null; then
    echo "⚠️  强制停止..."
    pkill -9 -f "claw-server"
fi

echo "✅ 服务端已停止"
