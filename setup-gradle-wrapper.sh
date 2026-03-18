#!/bin/bash
# 在本地运行此脚本生成 gradle wrapper
cd Android
if command -v gradle &> /dev/null; then
    gradle wrapper --gradle-version 8.2
else
    echo "Gradle 未安装，请手动下载 gradle-wrapper.jar"
    echo "下载地址：https://services.gradle.org/distributions/gradle-8.2-bin.zip"
fi
