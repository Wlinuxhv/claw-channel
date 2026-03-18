# 🦞 Claw Channel 部署说明（非 Docker 环境）

## 适用环境

- ✅ 容器环境
- ✅ 物理服务器
- ✅ 虚拟机
- ❌ 不支持 Docker

---

## 快速开始

### 方式一：使用启动脚本（推荐）

```bash
cd "/app/working/Claw Channel/Server"

# 启动服务
./start.sh

# 停止服务
./stop.sh
```

### 方式二：手动运行

```bash
cd "/app/working/Claw Channel/Server"

# 编译
go build -o claw-server cmd/server/main.go

# 运行
./claw-server
```

### 方式三：后台运行

```bash
cd "/app/working/Claw Channel/Server"

# 后台运行
nohup ./claw-server > server.log 2>&1 &

# 查看进程
ps aux | grep claw-server

# 查看日志
tail -f server.log
```

---

## 系统服务部署（Linux）

### 1. 安装服务

```bash
sudo cp claw-channel.service /etc/systemd/system/
sudo systemctl daemon-reload
```

### 2. 启动服务

```bash
sudo systemctl start claw-channel
sudo systemctl enable claw-channel  # 开机自启
```

### 3. 查看状态

```bash
sudo systemctl status claw-channel
```

### 4. 查看日志

```bash
sudo journalctl -u claw-channel -f
```

### 5. 停止服务

```bash
sudo systemctl stop claw-channel
```

---

## 配置说明

### 环境变量

| 变量名 | 说明 | 默认值 | 必填 |
|--------|------|--------|------|
| `SERVER_PORT` | 服务端端口 | 8080 | 否 |
| `JWT_SECRET` | JWT 密钥 | claw_channel_secret_key | 否 |
| `ADMIN_PASSWORD` | 管理员密码 | admin123 | 否 |
| `DATABASE_URL` | 数据库路径 | sqlite:///data/claw.db | 否 |

### 配置文件

创建 `.env` 文件（可选）：

```bash
# .env
SERVER_PORT=8080
JWT_SECRET=your-super-secret-jwt-key
ADMIN_PASSWORD=your-admin-password
```

---

## 端口说明

| 端口 | 用途 | 协议 |
|------|------|------|
| 8080 | HTTP API + WebSocket | TCP |

### 防火墙配置

```bash
# Ubuntu (UFW)
sudo ufw allow 8080/tcp

# CentOS (firewalld)
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload

# iptables
sudo iptables -A INPUT -p tcp --dport 8080 -j ACCEPT
```

---

## 数据管理

### 数据库位置

```
/app/working/Claw Channel/Server/data/claw.db
```

### 备份数据库

```bash
# 创建备份目录
mkdir -p backup

# 备份数据库
cp Server/data/claw.db backup/claw-$(date +%Y%m%d_%H%M%S).db

# 备份日志
cp Server/server.log backup/server-$(date +%Y%m%d_%H%M%S).log
```

### 恢复数据库

```bash
# 停止服务
pkill -f claw-server

# 恢复数据库
cp backup/claw-20260318_100000.db Server/data/claw.db

# 启动服务
./Server/claw-server
```

---

## 监控与维护

### 查看进程

```bash
ps aux | grep claw-server
```

### 查看端口

```bash
netstat -tlnp | grep 8080
# 或
ss -tlnp | grep 8080
```

### 查看日志

```bash
# 实时日志
tail -f Server/server.log

# 最近 100 行
tail -n 100 Server/server.log

# 搜索错误
grep "ERROR" Server/server.log
```

### 健康检查

```bash
curl http://localhost:8080/api/health
```

---

## API 测试

### 1. 健康检查

```bash
curl http://localhost:8080/api/health
```

### 2. 管理员登录

```bash
curl -X POST http://localhost:8080/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{"password":"admin123"}'
```

### 3. 生成推荐码

```bash
curl -X POST http://localhost:8080/api/admin/recommendation-codes \
  -H "Content-Type: application/json"
```

### 4. 用户登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"recommendation_code":"YOUR_CODE"}'
```

---

## 故障排查

### 问题 1：服务无法启动

```bash
# 检查 Go 环境
go version

# 检查端口占用
netstat -tlnp | grep 8080

# 查看日志
cat server.log
```

### 问题 2：端口被占用

```bash
# 查找占用进程
lsof -i :8080

# 停止进程
kill -9 <PID>
```

### 问题 3：数据库锁死

```bash
# 停止服务
pkill -f claw-server

# 删除锁文件
rm -f Server/data/claw.db-shm
rm -f Server/data/claw.db-wal

# 重启服务
./Server/claw-server
```

### 问题 4：内存不足

```bash
# 查看内存使用
free -h

# 查看进程内存
ps aux | grep claw-server
```

---

## 性能优化

### 1. 调整文件描述符限制

```bash
# 临时调整
ulimit -n 65535

# 永久调整（/etc/security/limits.conf）
echo "* soft nofile 65535" >> /etc/security/limits.conf
echo "* hard nofile 65535" >> /etc/security/limits.conf
```

### 2. 调整 TCP 参数

```bash
# /etc/sysctl.conf
net.core.somaxconn = 65535
net.ipv4.tcp_max_syn_backlog = 65535
```

应用配置：
```bash
sysctl -p
```

---

## 安全建议

### 1. 修改默认密码

```bash
# 编辑 .env 文件
ADMIN_PASSWORD=your-strong-password
JWT_SECRET=your-random-secret-key
```

### 2. 配置防火墙

```bash
# 只允许信任的 IP 访问
sudo ufw allow from 192.168.1.0/24 to any port 8080
```

### 3. 启用 HTTPS（可选）

使用 Nginx 反向代理：

```nginx
server {
    listen 443 ssl;
    server_name claw.yourdomain.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
}
```

### 4. 定期备份

```bash
# 添加到 crontab
0 2 * * * cp /app/working/Claw\ Channel/Server/data/claw.db /backup/claw-$(date +\%Y\%m\%d).db
```

---

## 升级指南

### 1. 备份数据

```bash
cp Server/data/claw.db backup/claw-backup.db
```

### 2. 停止服务

```bash
pkill -f claw-server
```

### 3. 更新代码

```bash
git pull
```

### 4. 重新编译

```bash
cd Server
go build -o claw-server cmd/server/main.go
```

### 5. 启动服务

```bash
./claw-server
```

---

## 联系支持

- [系统架构文档](../系统架构.md)
- [服务端文档](README.md)
- [部署指南](../部署指南.md)
- [测试报告](../Go 服务端测试报告.md)

---

**🦞 部署愉快！**

*最后更新：2026-03-18*
