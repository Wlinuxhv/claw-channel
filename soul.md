# 🦞 Claw Channel - 项目灵魂文档

## ⚠️ 重要约束

**禁止在本地构建安卓和 Windows 应用**

**禁止在本地构建安卓和 Windows 应用**

**禁止在本地构建安卓和 Windows 应用**

### 🚫 绝对禁止

**禁止在本地构建安卓和 Windows 应用** - 这是项目的核心规则！

---

## 原因说明

**禁止在本地构建安卓和 Windows 应用** - 当前环境为容器环境，无法构建 Android APK 或 Windows 应用。

### 限制条件

- 🚫 没有完整的 Android SDK
- 🚫 没有 JDK 环境
- 🚫 没有 Gradle 完整配置
- 🚫 没有 Windows 构建工具
- 🚫 缺少 gradle-wrapper.jar

---

## 解决方案

### ✅ 使用 GitHub Actions 构建

所有 Android 构建必须通过 GitHub Actions 完成：

1. 推送代码到 GitHub
2. 自动触发 `android-build.yml` Workflow
3. 在 GitHub 云端构建 APK
4. 下载构建产物（Artifact）

### 构建流程

```
本地代码 → Git Push → GitHub → Actions → 构建 APK → 下载
```

---

## 操作指南

### 1. 推送代码到 GitHub

```bash
cd /app/working/Claw\ Channel

# 添加远程仓库
git remote add origin https://github.com/Wlinuxhv/claw-channel.git

# 推送到 GitHub
git push -u origin main
```

### 2. 触发构建

**自动触发**:
- Push 到 main/develop 分支自动构建

**手动触发**:
1. 访问 GitHub 仓库 → Actions
2. 选择 "🤖 Android Build"
3. 点击 "Run workflow"

### 3. 下载 APK

1. 等待 Workflow 完成（🟢 绿色）
2. 滚动到底部 "Artifacts"
3. 点击 `claw-channel-debug`
4. 下载 ZIP 文件
5. 解压获取 APK

---

## 项目定位

**Claw Channel** - AI 个人助理聊天客户端

- 📱 Android 客户端（通过 GitHub Actions 构建）
- 🐹 Go 服务端（可在本地构建）
- 🤖 OpenClaw AI 集成

---

## 构建矩阵

| 组件 | 本地构建 | GitHub Actions |
|------|----------|----------------|
| Android APK | 🚫 禁止 | ✅ 可以 |
| Windows 应用 | 🚫 禁止 | ✅ 可以 |
| Go Server | ✅ 可以 | ✅ 可以 |
| 文档 | ✅ 可以 | ✅ 可以 |

---

## 记住

**禁止在本地构建安卓和 Windows 应用**

**禁止在本地构建安卓和 Windows 应用**

**禁止在本地构建安卓和 Windows 应用**

---

*最后更新：2026-03-18*
