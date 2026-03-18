# Claw Channel 项目总览

## 🦞 项目简介

**Claw Channel** 是一个 Android 聊天客户端应用，用于与 **OpenClaw AI 个人助理**进行对话和交互。

- **定位**：OpenClaw 的一个 Channel（类似钉钉的 WebChat/WhatsApp 渠道）
- **用户规模**：5 人小团队
- **部署方式**：家庭服务器 + Cloudflare Tunnel
- **认证方式**：AppKey + Client Secret（服务端生成）
- **当前状态**：Phase 1 基础框架已完成 ✅（2025-03-18）

---

## 📁 项目结构

```
Claw Channel/
├── 用户需求.md              # 用户需求文档 (v4.0)
├── 系统架构.md              # 技术架构文档 (v1.0)
├── README.md               # 本文件
├── Server/                 # Go 服务端
│   ├── cmd/server/        # 主程序入口
│   ├── internal/          # 内部包
│   │   ├── config/        # 配置管理
│   │   ├── handler/       # HTTP/WebSocket 处理器
│   │   ├── service/       # 业务服务
│   │   ├── model/         # 数据模型
│   │   ├── repository/    # 数据持久化
│   │   ├── middleware/    # 中间件
│   │   └── websocket/     # WebSocket 管理
│   ├── pkg/               # 公共包
│   ├── configs/           # 配置文件
│   ├── migrations/        # 数据库迁移
│   ├── go.mod             # Go 模块定义
│   └── README.md          # 服务端文档
└── Android/               # Android 客户端
    ├── app/               # 应用代码
    │   ├── src/main/java/com/clawchannel/
    │   │   ├── ui/        # UI 层
    │   │   ├── data/      # 数据层
    │   │   ├── domain/    # 业务逻辑层
    │   │   └── di/        # 依赖注入
    │   └── build.gradle.kts
    ├── gradle/            # Gradle 配置
    └── README.md          # 客户端文档
```

---

## 🏗️ 系统架构

```
┌──────────────────┐          ┌──────────────────┐          ┌──────────────────┐
│   安卓客户端      │          │   Go 服务端       │          │   OpenClaw       │
│  (Claw Channel)  │          │  (家庭服务器)     │          │   Gateway        │
│  (5 个用户手机)   │          │                    │          │  (AI 服务)        │
│                  │          │  - 用户认证        │          │                  │
│  - AppKey/Secret │◄────────►│  - 消息转发        │◄────────►│  - AI 对话        │
│  - 聊天界面      │  WebSocket│  - 会话管理        │  WebSocket│  - Channel 管理   │
│  - 本地存储      │  :8080   │  - Token 管理      │  :18789  │                  │
└──────────────────┘          └──────────────────┘          └──────────────────┘
         │                            │
         │                            │
         └────────────────────────────┘
              Cloudflare Tunnel
            (公网访问 Go 服务端)
```

---

## 🚀 快速开始

### 服务端部署

1. **配置环境变量**
   ```bash
   export SERVER_PORT=8080
   export JWT_SECRET=your-secret-key
   export OPENCLAW_WS_URL=ws://localhost:18789/ws
   ```

2. **运行服务**
   ```bash
   cd Server
   go run cmd/server/main.go
   ```

3. **Docker 部署**（推荐）
   ```bash
   docker-compose up -d
   ```

详见：[Server/README.md](Server/README.md)

### 客户端开发

1. **打开项目**
   - 使用 Android Studio 打开 `Android/` 目录

2. **构建项目**
   ```bash
   cd Android
   ./gradlew build
   ```

3. **安装到设备**
   ```bash
   ./gradlew installDebug
   ```

详见：[Android/README.md](Android/README.md)

---

## 📋 核心功能

### Phase 1: 基础框架 ✅ 已完成

- [x] 用户需求文档 (v4.0)
- [x] 系统架构设计 (v1.0)
- [x] Go 服务端基础框架
  - [x] 配置管理
  - [x] HTTP 服务器
  - [x] WebSocket Hub
  - [x] 健康检查接口
  - [x] 登录接口（Mock）
- [x] Android 客户端基础框架
  - [x] Gradle 配置
  - [x] 依赖配置
- [x] Docker 配置
- [x] 文档编写（9 个文档）

### Phase 2: 核心功能 ⏳ 进行中

- [ ] Go 服务端认证模块（JWT）
- [ ] Go 服务端 WebSocket 转发
- [ ] Go 服务端 OpenClaw 对接
- [ ] Android 登录激活界面
- [ ] Android 聊天界面
- [ ] Android WebSocket 客户端
- [ ] Android 本地数据库
- [ ] 端到端测试

---

## 🔐 用户认证

### 默认用户（5 个）

| 用户 | AppKey | Client Secret |
|------|--------|---------------|
| 用户 1 | claw_user_001 | secret_001 |
| 用户 2 | claw_user_002 | secret_002 |
| 用户 3 | claw_user_003 | secret_003 |
| 用户 4 | claw_user_004 | secret_004 |
| 用户 5 | claw_user_005 | secret_005 |

**注意**：生产环境请修改为安全的随机值！

### 认证流程

1. 用户输入 AppKey + Client Secret
2. 客户端调用 `/api/auth/login` 接口
3. 服务端验证通过，返回 JWT Token
4. 客户端使用 Token 建立 WebSocket 连接
5. Token 过期前自动刷新

---

## 💬 消息协议

### 客户端 → 服务端

```json
{
  "type": "message",
  "content": "你好，龙虾！",
  "content_type": "text",
  "session_id": "uuid",
  "message_id": "uuid",
  "timestamp": 1710768000
}
```

### 服务端 → 客户端

```json
{
  "type": "message",
  "content": "你好！有什么我可以帮你的吗？",
  "content_type": "text",
  "session_id": "uuid",
  "message_id": "uuid",
  "reply_to": "uuid",
  "timestamp": 1710768000,
  "status": "complete"
}
```

---

## 🛠️ 技术栈

### 服务端

- **语言**: Go 1.21+
- **WebSocket**: gorilla/websocket
- **JWT**: golang-jwt/jwt/v5
- **数据库**: SQLite / PostgreSQL
- **部署**: Docker

### 客户端

- **语言**: Kotlin
- **UI**: Jetpack Compose
- **架构**: MVVM + Hilt
- **数据库**: Room (SQLite)
- **网络**: OkHttp + WebSocket

---

## 📊 性能指标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 消息延迟 | < 500ms | 客户端→服务端→客户端 |
| AI 响应延迟 | 2-10s | 取决于 OpenClaw |
| 并发连接 | ≥ 10 | 支持 5 用户 + 冗余 |
| 消息存储 | ≥ 1000 条/用户 | 本地 SQLite |

---

## 📖 文档

- [用户需求文档](用户需求.md) - 用户需求详细说明
- [系统架构文档](系统架构.md) - 技术架构详细说明
- [服务端文档](Server/README.md) - Go 服务端使用说明
- [客户端文档](Android/README.md) - Android 客户端使用说明

---

## 📝 开发计划

### Phase 1: 基础框架 (当前)
- [x] 需求分析
- [x] 架构设计
- [x] 工程创建
- [ ] 认证模块开发

### Phase 2: 核心功能
- [ ] WebSocket 通信
- [ ] 消息转发
- [ ] UI 界面开发
- [ ] 本地存储

### Phase 3: 测试优化
- [ ] 端到端测试
- [ ] 性能优化
- [ ] 安全加固
- [ ] 文档完善

### Phase 4: 部署发布
- [ ] 服务端部署
- [ ] APK 打包
- [ ] 用户培训
- [ ] 上线运行

---

## 🎯 验收标准

### 功能验收
- [ ] 可通过 AppKey + Secret 激活登录
- [ ] 可发送/接收文字消息
- [ ] 可与 OpenClaw AI 正常对话
- [ ] 支持 OpenClaw 命令（/status、/new 等）
- [ ] 聊天记录可查看
- [ ] Token 自动刷新正常
- [ ] 断线重连正常

### 性能验收
- [ ] 消息发送延迟 < 500ms
- [ ] 支持 5 人同时在线
- [ ] 应用启动时间 < 3 秒

### 安全验收
- [ ] Token 加密存储
- [ ] WSS 加密通信
- [ ] 防重放攻击

---

## 👥 团队

- 架构师：AI Assistant
- 开发：待定
- 测试：待定

---

## 📄 许可证

MIT License

---

**🦞 Claw Channel - 让你的 AI 助理无处不在！**
