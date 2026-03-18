#!/bin/bash
# 🦞 Claw Channel 远程部署脚本
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
NC='\033[0m' # No Color

# 打印函数
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 sshpass
check_prerequisites() {
    print_info "检查依赖..."
    if ! command -v sshpass &> /dev/null; then
        print_error "sshpass 未安装，请先安装：apt-get install -y sshpass"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        print_warning "docker-compose 未安装，将使用 docker compose"
    fi
    
    print_success "依赖检查通过"
}

# 测试 SSH 连接
test_ssh_connection() {
    print_info "测试 SSH 连接到 $REMOTE_HOST..."
    
    if sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no -o ConnectTimeout=10 $REMOTE_USER@$REMOTE_HOST "echo '连接成功'" > /dev/null 2>&1; then
        print_success "SSH 连接成功"
    else
        print_error "SSH 连接失败，请检查网络和密码"
        exit 1
    fi
}

# 创建远程目录
create_remote_directory() {
    print_info "创建远程目录 $REMOTE_DIR..."
    
    sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST "mkdir -p $REMOTE_DIR"
    sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST "mkdir -p $REMOTE_DIR/data"
    sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST "mkdir -p $REMOTE_DIR/logs"
    sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST "mkdir -p $REMOTE_DIR/openclaw-config"
    sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST "mkdir -p $REMOTE_DIR/openclaw-data"
    
    print_success "远程目录创建完成"
}

# 上传文件
upload_files() {
    print_info "上传文件到远程服务器..."
    
    # 创建临时 tar 包
    print_info "打包 Server 目录..."
    tar -czf /tmp/claw-server.tar.gz -C "./Server" .
    
    # 上传 tar 包
    sshpass -p "$REMOTE_PASS" scp -o StrictHostKeyChecking=no \
        /tmp/claw-server.tar.gz $REMOTE_USER@$REMOTE_HOST:/tmp/
    
    # 远程解压
    sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST \
        "mkdir -p $REMOTE_DIR/Server && tar -xzf /tmp/claw-server.tar.gz -C $REMOTE_DIR/Server"
    
    # 上传 docker-compose.yml
    sshpass -p "$REMOTE_PASS" scp -o StrictHostKeyChecking=no \
        docker-compose.yml $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/
    
    # 上传 .env.example
    sshpass -p "$REMOTE_PASS" scp -o StrictHostKeyChecking=no \
        .env.example $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/.env
    
    # 清理临时文件
    rm -f /tmp/claw-server.tar.gz
    sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST \
        "rm -f /tmp/claw-server.tar.gz"
    
    print_success "文件上传完成"
}

# 远程部署
remote_deploy() {
    print_info "开始远程部署..."
    
    # 执行远程命令
    sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST << 'ENDSSH'
cd /home/wlinuxhv/claw-channel

print_info() {
    echo "[INFO] $1"
}

print_success() {
    echo "[SUCCESS] $1"
}

print_info "停止旧容器..."
docker compose down 2>/dev/null || true

print_info "构建镜像..."
docker compose build --no-cache

print_info "启动服务..."
docker compose up -d

print_info "等待服务启动..."
sleep 5

print_info "检查服务状态..."
docker compose ps

print_success "部署完成!"
ENDSSH
    
    print_success "远程部署完成"
}

# 检查服务状态
check_status() {
    print_info "检查服务状态..."
    
    sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST << 'ENDSSH'
cd /home/wlinuxhv/claw-channel

echo "=== 容器状态 ==="
docker compose ps

echo ""
echo "=== 端口监听 ==="
ss -tlnp | grep -E "8080|18789" || echo "未监听端口"

echo ""
echo "=== 健康检查 ==="
curl -s http://localhost:8080/api/health || echo "健康检查失败"
ENDSSH
}

# 查看日志
view_logs() {
    print_info "查看服务日志..."
    
    sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST << 'ENDSSH'
cd /home/wlinuxhv/claw-channel

echo "=== 最近日志 ==="
docker compose logs --tail=50
ENDSSH
}

# 显示帮助
show_help() {
    echo "🦞 Claw Channel 远程部署脚本"
    echo ""
    echo "用法：$0 [命令]"
    echo ""
    echo "命令:"
    echo "  deploy    部署到远程服务器（默认）"
    echo "  status    查看服务状态"
    echo "  logs      查看服务日志"
    echo "  restart   重启服务"
    echo "  stop      停止服务"
    echo "  clean     清理所有容器和数据"
    echo "  help      显示帮助"
    echo ""
}

# 主函数
main() {
    case "${1:-deploy}" in
        deploy)
            check_prerequisites
            test_ssh_connection
            create_remote_directory
            upload_files
            remote_deploy
            check_status
            ;;
        status)
            check_status
            ;;
        logs)
            view_logs
            ;;
        restart)
            print_info "重启服务..."
            sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST \
                "cd $REMOTE_DIR && docker compose restart"
            print_success "服务已重启"
            ;;
        stop)
            print_info "停止服务..."
            sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST \
                "cd $REMOTE_DIR && docker compose down"
            print_success "服务已停止"
            ;;
        clean)
            print_warning "清理所有容器和数据..."
            read -p "确定要清理所有数据吗？(y/N) " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                sshpass -p "$REMOTE_PASS" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST \
                    "cd $REMOTE_DIR && docker compose down -v"
                print_success "清理完成"
            else
                print_info "已取消"
            fi
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "未知命令：$1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
