# Claw Channel Android

Android 聊天客户端 - 用于与 OpenClaw AI 个人助理进行对话

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose
- **架构**: MVVM
- **依赖注入**: Hilt
- **本地数据库**: Room (SQLite)
- **网络**: OkHttp + WebSocket

## 项目结构

```
app/
├── src/main/java/com/clawchannel/
│   ├── ui/              # UI 层 (Activity, Composable)
│   ├── data/            # 数据层 (Repository, DataSource)
│   ├── domain/          # 业务逻辑层 (UseCase, Model)
│   └── di/              # 依赖注入
├── src/main/res/        # 资源文件
└── build.gradle.kts     # 构建配置
```

## 快速开始

### 1. 打开项目

使用 Android Studio 打开项目

### 2. 配置本地属性

创建 `local.properties` 文件：

```properties
sdk.dir=/path/to/Android/sdk
```

### 3. 构建项目

```bash
./gradlew build
```

### 4. 安装到设备

```bash
./gradlew installDebug
```

## 功能特性

- ✅ AppKey + Secret 激活登录
- ✅ WebSocket 实时通信
- ✅ 聊天界面（类钉钉）
- ✅ 消息本地存储
- ✅ Token 自动刷新
- ✅ 断线重连
- ✅ 快捷命令（/status, /new, /reset）

## 系统要求

- Android 8.0 (API 26) 及以上
- 网络权限
- 后台运行权限（用于消息推送）

## 激活使用

1. 打开应用
2. 输入管理员提供的 AppKey 和 Client Secret
3. 激活成功后即可开始聊天

## 开发

### 构建变体

- `debug`: 开发版本
- `release`: 发布版本

### 运行测试

```bash
./gradlew test
```

### 代码风格

遵循 Kotlin 官方代码风格指南

## 依赖

主要依赖：

- Jetpack Compose
- Hilt
- Room
- OkHttp
- Coroutines
- Flow

详细依赖请查看 `app/build.gradle.kts`

## 许可证

MIT License
