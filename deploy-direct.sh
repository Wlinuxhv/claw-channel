#!/bin/bash
# 🦞 Claw Channel 直接部署脚本（无 Docker）
# 部署到 192.168.3.90 服务器

set -e

# 服务器配置
REMOTE_HOST="192.168.3.90"
REMOTE_USER="wlinuxhv"
REMOTE_PASS="Sxfpwd@3321"
REMOTE_DIR="/home/wlinuxhv/claw-channel"

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

print_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }

echo "🦞 Claw Channel 直接部署（无 Docker）"
echo "===================================="
echo ""

# 1. 编译服务端
print_info "步骤 1: 编译 Go 服务端..."
cd "/app/working/Claw Channel/Server"
GOPROXY=https://goproxy.cn,direct go build -o claw-server cmd/server/main.go
print_success "编译完成 ($(ls -lh claw-server | awk '{print $5}'))"

# 2. 上传文件
print_info "步骤 2: 上传文件到远程服务器..."
cd "/app/working/Claw Channel"

# 创建目录
sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST "mkdir -p $REMOTE_DIR/data $REMOTE_DIR/logs"

# 上传二进制文件
sshpass -p "$REMOTE_PASS" scp -o StrictHostKeyChecking=no \
    ./Server/claw-server $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/

# 上传环境配置
cat > /tmp/.env << EOF
SERVER_PORT=8080
JWT_SECRET=claw_channel_secret_key_$(date +%s)_$(openssl rand -hex 8)
ADMIN_PASSWORD=admin123
DATABASE_URL=sqlite:///data/claw.db
EOF

sshpass -p "$REMOTE_PASS" scp -o StrictHostKeyChecking=no \
    /tmp/.env $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/.env

print_success "文件上传完成"

# 3. 远程启动
print_info "步骤 3: 远程启动服务..."
sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST << 'ENDSSH'
cd /home/wlinuxhv/claw-channel

echo "[INFO] 停止旧进程..."
pkill -f "claw-server" 2>/dev/null || true
sleep 1

echo "[INFO] 启动服务..."
nohup ./claw-server > server.log 2>&1 &
PID=$!

echo "[INFO] 进程 PID: $PID"
sleep 3

echo "[INFO] 检查进程..."
ps aux | grep claw-server | grep -v grep

echo ""
echo "[INFO] 测试健康检查..."
curl -s http://localhost:8080/api/health || echo "等待服务启动..."
ENDSSH

print_success "部署完成!"

echo ""
echo "=== 访问信息 ==="
echo "API 地址：http://$REMOTE_HOST:8080"
echo "健康检查：http://$REMOTE_HOST:8080/api/health"
echo "管理员密码：admin123"
echo ""
echo "=== 常用命令 ==="
echo "查看状态：ssh $REMOTE_USER@$REMOTE_HOST 'ps aux | grep claw-server'"
echo "查看日志：ssh $REMOTE_USER@$REMOTE_HOST 'tail -f $REMOTE_DIR/server.log'"
echo "停止服务：ssh $REMOTE_USER@$REMOTE_HOST 'pkill -f claw-server'"
echo "重启服务：ssh $REMOTE_USER@$REMOTE_HOST 'pkill -f claw-server && cd $REMOTE_DIR && nohup ./claw-server > server.log 2>&1 &'"
echo ""
