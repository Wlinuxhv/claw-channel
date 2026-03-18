# Claw Channel Server

Go 服务端 - Claw Channel Android 客户端与 OpenClaw AI 之间的消息转发中心

## 🦞 功能特性

- ✅ 推荐码认证系统
- ✅ JWT Token 管理
- ✅ WebSocket 实时通信
- ✅ 多机器人管理
- ✅ 管理员后台
- ✅ SQLite 数据库
- ✅ Docker 部署支持

## 🚀 快速开始

### 方式一：直接运行（需要 Go 环境）

```bash
cd Server

# 下载依赖
go mod download

# 运行服务
go run cmd/server/main.go
```

### 方式二：Docker 部署（推荐）

```bash
cd ..
docker-compose up -d
```

## 📋 API 接口

### 认证接口

#### POST /api/auth/login

用户使用推荐码登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"recommendation_code":"a1b2c3d4"}'
```

响应：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIs...",
    "token_type": "Bearer",
    "expires_in": 7200,
    "refresh_token": "dGhpcyBpcyBhIHJlZnJl..."
  }
}
```

#### POST /api/auth/refresh

刷新 Token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refresh_token":"dGhpcyBpcyBhIHJlZnJl..."}'
```

### WebSocket 接口

#### WS /ws/chat

聊天 WebSocket 连接

```
ws://localhost:8080/ws/chat?token=ACCESS_TOKEN
```

### 管理员接口

#### POST /api/admin/login

管理员登录

```bash
curl -X POST http://localhost:8080/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{"password":"admin123"}'
```

#### POST /api/admin/recommendation-codes

生成推荐码

```bash
curl -X POST http://localhost:8080/api/admin/recommendation-codes \
  -H "Content-Type: application/json"
```

#### GET /api/admin/robots

获取机器人列表

```bash
curl http://localhost:8080/api/admin/robots
```

#### POST /api/admin/robots

创建机器人

```bash
curl -X POST http://localhost:8080/api/admin/robots \
  -H "Content-Type: application/json" \
  -d '{
    "name": "AI Assistant 1",
    "app_key": "claw_bot_001",
    "secret": "bot_secret_001",
    "openclaw_url": "ws://10.147.20.1:18789/ws"
  }'
```

#### GET /api/admin/stats

获取统计信息

```bash
curl http://localhost:8080/api/admin/stats
```

## 🔧 配置

### 环境变量

| 变量名 | 说明 | 默认值 | 必需 |
|--------|------|--------|------|
| `SERVER_PORT` | 服务端监听端口 | `8080` | 否 |
| `JWT_SECRET` | JWT 密钥 | `claw-channel-secret-key` | 是（生产环境） |
| `DATABASE_URL` | 数据库连接字符串 | `sqlite:///data/claw.db` | 否 |
| `ADMIN_PASSWORD` | 管理员密码 | `admin123` | 是（生产环境） |
| `ZEROTIER_NETWORK_ID` | ZeroTier 网络 ID | `` | 否 |

### 示例配置

创建 `.env` 文件：

```bash
SERVER_PORT=8080
JWT_SECRET=your-super-secret-jwt-key-change-in-production
DATABASE_URL=sqlite:///data/claw.db
ADMIN_PASSWORD=your-admin-password-change-in-production
ZEROTIER_NETWORK_ID=your-zerotier-network-id
```

## 🧪 测试

运行测试脚本：

```bash
./test.sh
```

## 📊 监控

### 健康检查

```bash
curl http://localhost:8080/api/health
```

响应：
```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "status": "ok",
    "timestamp": 1710768000,
    "clients": 5
  }
}
```

## 🐳 Docker 部署

### 构建镜像

```bash
docker build -t claw-channel-server .
```

### 运行容器

```bash
docker run -d \
  --name claw-server \
  -p 8080:8080 \
  -v ./data:/data \
  -e JWT_SECRET=your-secret \
  -e ADMIN_PASSWORD=your-admin-password \
  claw-channel-server
```

### Docker Compose

```bash
docker-compose up -d
```

## 📁 项目结构

```
Server/
├── cmd/
│   └── server/
│       └── main.go          # 程序入口
├── internal/
│   ├── config/              # 配置管理
│   ├── database/            # 数据库
│   ├── handler/             # HTTP/WebSocket 处理器
│   ├── model/               # 数据模型
│   ├── service/             # 业务服务
│   │   ├── auth_service.go  # 认证服务
│   │   ├── jwt_service.go   # JWT 服务
│   │   └── robot_service.go # 机器人服务
│   └── websocket/           # WebSocket 管理
│       ├── hub.go           # 连接中心
│       └── client.go        # 客户端连接
├── pkg/                     # 公共包
├── go.mod                   # Go 模块定义
├── Dockerfile               # Docker 构建
└── test.sh                  # 测试脚本
```

## 🔐 安全建议

1. **修改默认密码**：生产环境必须修改 `ADMIN_PASSWORD`
2. **使用强 JWT 密钥**：`JWT_SECRET` 使用随机字符串
3. **启用 HTTPS/WSS**：通过 Cloudflare Tunnel 自动启用
4. **定期备份数据库**：`/data/claw.db`

## 📖 相关文档

- [系统需求规格](../系统需求规格.md)
- [系统架构](../系统架构.md)
- [部署指南](../部署指南.md)

## 🦞 许可证

MIT License
