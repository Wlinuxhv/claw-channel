#!/bin/bash
# 🦞 Claw Channel 快速部署脚本（简化版）
# 部署到 192.168.3.90 服务器

set -e

# 服务器配置
REMOTE_HOST="192.168.3.90"
REMOTE_USER="wlinuxhv"
REMOTE_PASS="Sxfpwd@3321"
REMOTE_DIR="/home/wlinuxhv/claw-channel"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

echo "🦞 Claw Channel 快速部署"
echo "========================"
echo ""

# 1. 编译服务端
print_info "步骤 1: 编译 Go 服务端..."
cd "/app/working/Claw Channel/Server"
GOPROXY=https://goproxy.cn,direct go build -o claw-server cmd/server/main.go
print_success "编译完成"

# 2. 上传文件
print_info "步骤 2: 上传文件到远程服务器..."
cd "/app/working/Claw Channel"

# 创建目录
sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST "mkdir -p $REMOTE_DIR"

# 上传二进制文件
sshpass -p "$REMOTE_PASS" scp -o StrictHostKeyChecking=no \
    ./Server/claw-server $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/

# 上传启动脚本
cat > /tmp/start-claw.sh << 'EOF'
#!/bin/bash
cd /home/wlinuxhv/claw-channel

# 停止旧容器
docker stop claw-server 2>/dev/null || true
docker rm claw-server 2>/dev/null || true

# 启动新容器
docker run -d \
  --name claw-server \
  --restart unless-stopped \
  -p 8080:8080 \
  -v $(pwd)/data:/app/data \
  -v $(pwd)/logs:/app/logs \
  -e SERVER_PORT=8080 \
  -e JWT_SECRET=claw_channel_secret_key_$(date +%s) \
  -e ADMIN_PASSWORD=admin123 \
  -e DATABASE_URL=sqlite:///data/claw.db \
  -w /app \
  alpine:latest ./claw-server

echo "服务已启动"
EOF

sshpass -p "$REMOTE_PASS" scp -o StrictHostKeyChecking=no \
    /tmp/start-claw.sh $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/start.sh

sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST \
    "chmod +x $REMOTE_DIR/start.sh"

print_success "文件上传完成"

# 3. 远程启动
print_info "步骤 3: 远程启动服务..."
sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST << 'ENDSSH'
cd /home/wlinuxhv/claw-channel

echo "[INFO] 停止旧容器..."
docker stop claw-server 2>/dev/null || true
docker rm claw-server 2>/dev/null || true

echo "[INFO] 创建数据目录..."
mkdir -p data logs

echo "[INFO] 拉取 Alpine 镜像..."
docker pull alpine:latest

echo "[INFO] 启动容器..."
docker run -d \
  --name claw-server \
  --restart unless-stopped \
  -p 8080:8080 \
  -v $(pwd)/data:/app/data \
  -v $(pwd)/logs:/app/logs \
  -e SERVER_PORT=8080 \
  -e JWT_SECRET=claw_channel_secret_key_$(date +%s) \
  -e ADMIN_PASSWORD=admin123 \
  -e DATABASE_URL=sqlite:///data/claw.db \
  -w /app \
  alpine:latest ./claw-server

echo "[INFO] 等待服务启动..."
sleep 3

echo "[INFO] 检查状态..."
docker ps | grep claw-server

echo ""
echo "[INFO] 测试健康检查..."
curl -s http://localhost:8080/api/health || echo "健康检查失败"
ENDSSH

print_success "部署完成!"

echo ""
echo "=== 访问信息 ==="
echo "API 地址：http://$REMOTE_HOST:8080"
echo "健康检查：http://$REMOTE_HOST:8080/api/health"
echo "管理员密码：admin123"
echo ""
echo "=== 常用命令 ==="
echo "查看状态：ssh $REMOTE_USER@$REMOTE_HOST 'docker ps | grep claw-server'"
echo "查看日志：ssh $REMOTE_USER@$REMOTE_HOST 'docker logs claw-server'"
echo "停止服务：ssh $REMOTE_USER@$REMOTE_HOST 'docker stop claw-server'"
echo "重启服务：ssh $REMOTE_USER@$REMOTE_HOST 'docker restart claw-server'"
echo ""
