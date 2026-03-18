# 🦞 Claw Channel

**AI 个人助理聊天客户端** - Android APK + Go 服务端

[![Android Build](https://github.com/Wlinuxhv/claw-channel/actions/workflows/android-build.yml/badge.svg)](https://github.com/Wlinuxhv/claw-channel/actions/workflows/android-build.yml)
[![Go Server Build](https://github.com/Wlinuxhv/claw-channel/actions/workflows/go-build.yml/badge.svg)](https://github.com/Wlinuxhv/claw-channel/actions/workflows/go-build.yml)
[![Release Build](https://github.com/Wlinuxhv/claw-channel/actions/workflows/release.yml/badge.svg)](https://github.com/Wlinuxhv/claw-channel/actions/workflows/release.yml)

---

## 📱 项目简介

Claw Channel 是一个类钉钉聊天界面的 Android 客户端，用于与 **OpenClaw AI 个人助理** 进行对话和交互。

### 特性

- 🦞 **龙虾红主题** (#FF6B6B)
- 💬 **实时聊天** - WebSocket 低延迟通信
- 🔐 **推荐码认证** - 8 位 UUID，一次性使用
- 📱 **Android 8.0+** - 支持 API 26+
- 🤖 **多机器人管理** - 支持多个 OpenClaw 实例
- 📦 **消息类型** - 文字、图片、语音、文件
- 🔔 **推送通知** - 智能后台运行

---

## 🏗️ 项目结构

```
claw-channel/
├── Android/                    # Android 客户端
│   ├── app/
│   │   └── src/main/java/com/clawchannel/app/
│   │       ├── di/            # 依赖注入
│   │       ├── domain/        # 数据模型
│   │       ├── data/          # 数据层
│   │       ├── ui/            # UI 界面
│   │       └── MainActivity.kt
│   ├── build.gradle.kts
│   └── gradlew
├── Server/                     # Go 服务端
│   ├── cmd/server/
│   ├── internal/
│   │   ├── config/
│   │   ├── model/
│   │   ├── database/
│   │   ├── service/
│   │   ├── websocket/
│   │   └── handler/
│   └── go.mod
├── .github/workflows/          # GitHub Actions
│   ├── android-build.yml
│   ├── go-build.yml
│   └── release.yml
└── README.md
```

---

## 🚀 快速开始

### Android 客户端

#### 方式 1：下载预编译 APK

1. 访问 [Releases](https://github.com/Wlinuxhv/claw-channel/releases)
2. 下载最新版本的 APK
3. 安装到 Android 设备

#### 方式 2：本地编译

```bash
#  prerequisites
# - JDK 17
# - Android SDK (API 34)

cd Android

# 生成 Gradle Wrapper（首次）
chmod +x gradlew

# 编译 Debug 版本
./gradlew assembleDebug

# 编译 Release 版本
./gradlew assembleRelease

# APK 输出位置
# Debug: app/build/outputs/apk/debug/app-debug.apk
# Release: app/build/outputs/apk/release/app-release.apk
```

### Go 服务端

#### 方式 1：下载预编译二进制

1. 访问 [Releases](https://github.com/Wlinuxhv/claw-channel/releases)
2. 下载对应平台的二进制文件
3. 运行：
   ```bash
   chmod +x server-linux-amd64
   ./server-linux-amd64
   ```

#### 方式 2：本地编译

```bash
# prerequisites
# - Go 1.21+

cd Server

# 下载依赖
go mod download

# 运行
go run cmd/server/main.go

# 或编译
go build -o server ./cmd/server
./server
```

---

## ⚙️ 配置

### Go 服务端配置

创建 `.env` 文件（参考 `.env.example`）：

```bash
# 服务器配置
PORT=8080
HOST=0.0.0.0

# JWT 配置
JWT_SECRET=your-secret-key-change-in-production
JWT_EXPIRY=2h
JWT_REFRESH_EXPIRY=168h

# 管理员配置
ADMIN_PASSWORD=admin123

# 数据库配置
DATABASE_PATH=/data/claw.db

# OpenClaw 配置
OPENCLAW_ZEROTIER_NETWORK=your-zerotier-network-id
```

### Android 客户端配置

修改 `NetworkModule.kt` 中的服务器地址：

```kotlin
// 开发环境
baseUrl("http://192.168.3.90:8080/")

// 生产环境
baseUrl("https://your-domain.com/")
```

---

## 📖 文档

- [用户需求](用户需求.md) - 功能需求说明
- [系统需求规格](系统需求规格.md) - 详细技术规格
- [系统架构](系统架构.md) - 架构设计
- [部署指南](部署指南.md) - 部署说明
- [编码进度](编码进度.md) - 开发进度

---

## 🧪 测试

### Go 服务端测试

```bash
cd Server

# 运行测试
go test -v ./...

# 覆盖率
go test -cover ./...
```

### Android 客户端测试

```bash
cd Android

# 单元测试
./gradlew test

# UI 测试
./gradlew connectedAndroidTest
```

---

## 📦 GitHub Actions

### 自动构建

| Workflow | 触发条件 | 输出 |
|----------|----------|------|
| [Android Build](.github/workflows/android-build.yml) | Push to main/develop | Debug APK |
| [Go Server Build](.github/workflows/go-build.yml) | Push to main/develop | 多平台二进制 |
| [Release Build](.github/workflows/release.yml) | Push tag (v*) | Release + APK + 二进制 |

### 手动触发

1. 访问 [Actions](https://github.com/Wlinuxhv/claw-channel/actions)
2. 选择对应 Workflow
3. 点击 "Run workflow"
4. 选择分支/参数
5. 等待完成

### 下载构建产物

1. 进入 Workflow 运行页面
2. 滚动到底部 "Artifacts"
3. 点击下载对应文件

---

## 📱 使用指南

### 1. 获取推荐码

联系管理员获取 8 位推荐码（如：`a1b2c3d4`）

### 2. 登录

1. 打开 APP
2. 输入推荐码
3. 点击登录

### 3. 聊天

1. 在输入框输入消息
2. 点击发送按钮
3. 等待 AI 回复

### 4. 设置

1. 点击右上角退出登录
2. 可重新登录或切换账号

---

## 🛠️ 技术栈

### Android 客户端

- **语言**: Kotlin
- **UI**: Jetpack Compose
- **架构**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **网络**: OkHttp + WebSocket
- **HTTP**: Retrofit
- **本地存储**: Room + DataStore
- **导航**: Navigation Compose

### Go 服务端

- **语言**: Go 1.21+
- **Web 框架**: 标准库 + Gorilla WebSocket
- **数据库**: SQLite
- **认证**: JWT
- **加密**: bcrypt

---

## 📄 许可证

MIT License

---

## 👥 团队

5 人小团队

---

## 📞 联系

- GitHub: [@Wlinuxhv](https://github.com/Wlinuxhv)
- OpenClaw: https://github.com/Wlinuxhv/openclaw

---

**🦞 Happy Chatting!**
