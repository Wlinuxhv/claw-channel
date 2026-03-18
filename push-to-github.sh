#!/bin/bash
# 快速推送到 GitHub 脚本

echo "🦞 Claw Channel - 推送到 GitHub 构建"
echo "======================================"
echo ""

# 检查远程仓库
REMOTE=$(git remote get-url origin 2>/dev/null)

if [ -z "$REMOTE" ]; then
    echo "❌ 远程仓库未配置"
    echo ""
    echo "请先在 GitHub 上创建仓库，然后执行："
    echo ""
    echo "  git remote add origin https://github.com/Wlinuxhv/claw-channel.git"
    echo ""
    exit 1
fi

echo "✅ 远程仓库：$REMOTE"
echo ""

# 显示当前提交
echo "📝 最新提交:"
git log --oneline -3
echo ""

# 推送
echo "🚀 开始推送..."
git push -u origin main

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 推送成功！"
    echo ""
    echo "📱 请访问 GitHub Actions 查看构建状态:"
    echo "   https://github.com/Wlinuxhv/claw-channel/actions"
    echo ""
    echo "⏱️  构建时间：约 5-10 分钟"
    echo ""
    echo "📦 构建完成后下载 APK:"
    echo "   1. 点击成功的 workflow 运行"
    echo "   2. 滚动到底部 Artifacts"
    echo "   3. 下载 claw-channel-debug"
    echo ""
else
    echo ""
    echo "❌ 推送失败！"
    echo ""
    echo "可能的原因:"
    echo "  1. 需要 GitHub Token 认证"
    echo "  2. 远程仓库不存在"
    echo "  3. 网络连接问题"
    echo ""
    echo "解决方案:"
    echo "  使用 Personal Access Token:"
    echo "  git push https://<TOKEN>@github.com/Wlinuxhv/claw-channel.git main"
    echo ""
fi
