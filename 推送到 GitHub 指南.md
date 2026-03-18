# 📤 推送到 GitHub 指南

## 步骤 1: 创建 GitHub 仓库

1. 访问 https://github.com/new
2. 仓库名称：`claw-channel`
3. 描述：`🦞 AI 个人助理聊天客户端 - Android APK + Go 服务端`
4. 选择 **Public** 或 **Private**
5. **不要** 勾选 "Initialize this repository with a README"
6. 点击 "Create repository"

---

## 步骤 2: 添加远程仓库

```bash
cd /app/working/Claw\ Channel

# 添加远程仓库（替换为你的 GitHub 用户名）
git remote add origin https://github.com/Wlinuxhv/claw-channel.git

# 验证
git remote -v
```

---

## 步骤 3: 推送代码

```bash
# 推送到 main 分支
git push -u origin main

# 或使用 SSH（如果配置了 SSH key）
# git remote set-url origin git@github.com:Wlinuxhv/claw-channel.git
# git push -u origin main
```

---

## 步骤 4: 配置 GitHub Actions

### 4.1 启用 Actions

1. 访问你的仓库
2. 点击 **Actions** 标签
3. 如果是首次使用，点击 "I understand my workflows, go ahead and enable them"

### 4.2 验证 Workflow

推送后，Actions 会自动触发：

1. 访问 **Actions** 标签
2. 查看运行状态
   - 🟡 黄色 = 运行中
   - 🟢 绿色 = 成功
   - 🔴 红色 = 失败

### 4.3 下载构建产物

1. 点击成功的 Workflow 运行
2. 滚动到底部 **Artifacts**
3. 点击下载：
   - `claw-channel-debug` - Debug APK
   - `server-linux-amd64` - Linux 服务端
   - 等等

---

## 步骤 5: 发布 Release（可选）

### 方式 1: 打标签自动发布

```bash
# 创建标签
git tag v1.0.0

# 推送标签
git push origin v1.0.0

# 或推送所有标签
git push origin --tags
```

### 方式 2: 手动创建 Release

1. 访问 https://github.com/Wlinuxhv/claw-channel/releases
2. 点击 "Draft a new release"
3. 选择/创建标签
4. 填写发布说明
5. 上传构建产物
6. 点击 "Publish release"

---

## 步骤 6: 配置 Secrets（如果需要）

如果将来需要部署到服务器，配置以下 Secrets：

1. 访问 **Settings** → **Secrets and variables** → **Actions**
2. 点击 "New repository secret"
3. 添加：

| Name | Value |
|------|-------|
| `DEPLOY_HOST` | `192.168.3.90` |
| `DEPLOY_USER` | `wlinuxhv` |
| `DEPLOY_KEY` | (私钥) |

---

## 🔧 常见问题

### 问题 1: 推送失败 - Permission denied

**解决方案**: 使用 Personal Access Token

1. 访问 https://github.com/settings/tokens
2. 点击 "Generate new token (classic)"
3. 勾选 `repo` 权限
4. 生成 token
5. 推送时使用：
   ```bash
   git push https://<TOKEN>@github.com/Wlinuxhv/claw-channel.git main
   ```

### 问题 2: Gradle Wrapper 缺失

**解决方案**: 在本地生成

```bash
cd Android

# 如果已安装 Gradle
gradle wrapper --gradle-version 8.2

# 或手动下载 gradle-wrapper.jar
# 下载地址：https://services.gradle.org/distributions/gradle-8.2-bin.zip
```

### 问题 3: Actions 运行失败

**排查步骤**:

1. 查看 Workflow 日志
2. 检查错误信息
3. 常见问题：
   - JDK 版本不匹配 → 检查 `setup-java` 配置
   - SDK 缺失 → 检查 `setup-android` 配置
   - 依赖下载失败 → 检查网络连接

---

## 📊 Workflow 说明

### android-build.yml

**触发条件**:
- Push 到 main/develop 分支
- Pull Request
- 手动触发

**输出**:
- Debug APK
- 测试报告

### go-build.yml

**触发条件**:
- Push 到 main/develop 分支
- Pull Request
- 手动触发

**输出**:
- Linux 二进制
- macOS 二进制
- Windows 二进制

### release.yml

**触发条件**:
- Push 标签 (v*)
- 手动触发

**输出**:
- GitHub Release
- Release APK
- 所有平台二进制

---

## 🎯 下一步

1. ✅ 推送代码到 GitHub
2. ✅ 验证 Actions 运行
3. ✅ 下载并测试 APK
4. ⬜ 配置自动部署（可选）
5. ⬜ 设置分支保护规则

---

## 📖 相关文档

- [GitHub 配置指南.md](GitHub 配置指南.md)
- [部署指南.md](部署指南.md)
- [编码进度.md](编码进度.md)

---

**🦞 Happy Coding!**
