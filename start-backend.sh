#!/bin/bash

echo "========================================"
echo "启动大学企业联盟平台后端服务"
echo "========================================"

echo "检查Java环境..."
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请安装JDK 17+"
    exit 1
fi

java -version

echo ""
echo "启动Spring Boot应用..."
echo "服务将运行在: http://localhost:8081"
echo ""

cd backend

# 尝试使用Maven Wrapper
if [ -f "./mvnw" ]; then
    echo "使用Maven Wrapper启动..."
    ./mvnw spring-boot:run
else
    echo "使用系统Maven启动..."
    mvn spring-boot:run
fi

if [ $? -ne 0 ]; then
    echo ""
    echo "启动失败，请检查Maven是否正确安装"
    exit 1
fi
