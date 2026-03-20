# Claw Channel Android 构建脚本
# 用于 Windows 环境远程构建

Write-Host "🦞 开始构建 Claw Channel APK..." -ForegroundColor Cyan

# 设置环境变量
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "C:\Users\User\AppData\Local\Android\Sdk"

Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green
Write-Host "ANDROID_HOME: $env:ANDROID_HOME" -ForegroundColor Green

# 切换到项目目录
Set-Location -Path "C:\Users\User\Desktop\claw-channel\Android"

# 清理之前的构建
Write-Host "清理之前的构建..." -ForegroundColor Yellow
.\gradlew.bat clean

# 构建 Debug APK
Write-Host "构建 Debug APK..." -ForegroundColor Cyan
.\gradlew.bat assembleDebug

# 检查构建结果
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 构建成功！" -ForegroundColor Green
    
    # 显示 APK 信息
    $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
    if (Test-Path $apkPath) {
        $apkSize = (Get-Item $apkPath).Length
        Write-Host "APK 路径：$apkPath" -ForegroundColor Green
        Write-Host "APK 大小：$([math]::Round($apkSize / 1MB, 2)) MB" -ForegroundColor Green
    }
} else {
    Write-Host "❌ 构建失败！" -ForegroundColor Red
    exit 1
}
