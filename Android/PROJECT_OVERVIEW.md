# 🦞 Claw Channel Android App

## 项目概述

Claw Channel 是一个现代化的 Android AI 助手应用，采用 Material Design 3 设计语言和 Kotlin 语言开发。应用支持多机器人切换、WebSocket 实时通信、推荐码登录等功能。

## 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose + Material Design 3
- **架构**: MVVM (Model-View-ViewModel)
- **网络**: Retrofit + OkHttp
- **实时通信**: WebSocket
- **本地存储**: Room Database + SharedPreferences
- **依赖注入**: 手动依赖管理（可升级为 Hilt/Koin）

## 项目结构

```
app/src/main/java/com/clawchannel/app/
├── MainActivity.kt                 # 主 Activity 和导航逻辑
├── ClawChannelApplication.kt      # Application 类
│
├── data/                          # 数据层
│   ├── local/                     # 本地数据
│   │   ├── ClawChannelDatabase.kt # Room 数据库
│   │   ├── MessageDao.kt          # 消息 DAO
│   │   ├── MessageEntity.kt       # 消息实体
│   │   ├── TokenStore.kt          # Token 存储
│   │   └── Converters.kt          # 类型转换器
│   │
│   ├── remote/                    # 远程数据
│   │   ├── ApiService.kt           # REST API 接口
│   │   └── WebSocketManager.kt     # WebSocket 管理器
│   │
│   └── repository/                # 数据仓库
│       ├── AuthRepository.kt       # 认证仓库
│       └── MessageRepository.kt    # 消息仓库
│
├── domain/                        # 领域层
│   └── model/                     # 数据模型
│       ├── Models.kt              # 通用模型
│       └── Robot.kt               # 机器人模型
│
├── ui/                            # UI 层
│   ├── theme/                     # 主题
│   │   └── Color.kt               # 颜色定义
│   │
│   ├── login/                     # 登录模块
│   │   ├── LoginScreen.kt         # 登录界面
│   │   └── LoginViewModel.kt      # 登录 ViewModel
│   │
│   ├── robots/                    # 机器人列表模块
│   │   └── RobotListScreen.kt     # 机器人列表界面
│   │
│   ├── chat/                      # 聊天模块
│   │   ├── ChatScreen.kt          # 聊天界面
│   │   └── ChatViewModel.kt        # 聊天 ViewModel
│   │
│   ├── settings/                  # 设置模块
│   │   └── SettingsScreen.kt      # 设置界面
│   │
│   └── admin/                     # 管理员模块
│       └── AdminScreen.kt         # 管理员界面
│
└── util/                          # 工具类
    └── NotificationHelper.kt      # 通知工具
```

## 功能特性

### 1. 用户认证
- ✅ 推荐码登录系统
- ✅ 自动登录（本地 Token 存储）
- ✅ Token 自动刷新
- ✅ 管理员密码入口

### 2. 机器人列表
- ✅ 多机器人展示
- ✅ 在线状态指示
- ✅ 未读消息计数
- ✅ 最后消息预览

### 3. 实时聊天
- ✅ WebSocket 实时通信
- ✅ 文本消息收发
- ✅ 图片消息支持
- ✅ 文件消息支持
- ✅ 消息状态追踪（发送中、已发送、失败）
- ✅ 连接状态显示

### 4. 设置功能
- ✅ 服务器地址配置
- ✅ 应用版本显示
- ✅ 退出登录

### 5. 管理员功能
- ✅ 推荐码管理
- ✅ 推荐码生成
- ✅ 使用状态查看

## 构建说明

### 环境要求
- Android Studio Hedgehog 或更高版本
- JDK 17+
- Android SDK 34
- Gradle 8.2+

### 构建命令
```bash
# Debug 版本
./gradlew assembleDebug

# Release 版本
./gradlew assembleRelease

# 安装到设备
./gradlew installDebug
```

### 配置文件
确保在 `app/build.gradle.kts` 中配置：
- `BASE_URL`: 后端服务器地址
- 签名配置（Release 版本）

## API 接口

### REST API
```
POST /api/auth/login          # 登录获取 Token
POST /api/auth/refresh        # 刷新 Token
POST /api/admin/code          # 生成推荐码
GET  /api/admin/codes         # 获取推荐码列表
GET  /api/robots              # 获取机器人列表
```

### WebSocket
```
ws://server/ws/chat           # 聊天 WebSocket 端点
```

## 配色方案

应用使用龙虾红作为主题色：
- Primary: `#DC143C` (Lobster Red)
- 支持深色模式

## 安全考虑

1. **Token 存储**: 使用 EncryptedSharedPreferences 加密存储
2. **网络安全**: 支持 HTTPS 和证书固定
3. **输入验证**: 前端和后端双重验证

## 后续开发建议

1. **依赖注入**: 引入 Hilt 或 Koin
2. **测试**: 添加单元测试和 UI 测试
3. **离线支持**: 完善离线消息缓存
4. **推送通知**: 集成 FCM 推送
5. **多语言**: 添加国际化支持
6. **主题定制**: 允许用户自定义主题色

## 版本历史

- **v1.0.0** (2026-03-19)
  - 初始版本发布
  - 实现核心聊天功能
  - 推荐码登录系统
  - 管理员后台

---

© 2026 Claw Channel. All rights reserved.