#!/bin/bash

echo "========================================"
echo "启动大学企业联盟平台前端服务"
echo "========================================"

echo "检查Node.js环境..."
if ! command -v node &> /dev/null; then
    echo "错误: 未找到Node.js环境，请安装Node.js 16+"
    exit 1
fi

node --version

echo ""
echo "检查npm..."
if ! command -v npm &> /dev/null; then
    echo "错误: 未找到npm，请重新安装Node.js"
    exit 1
fi

npm --version

echo ""
echo "安装依赖包..."
npm install
if [ $? -ne 0 ]; then
    echo "错误: 依赖安装失败"
    exit 1
fi

echo ""
echo "启动前端开发服务器..."
echo "服务将运行在: http://localhost:5173"
echo ""

npm run dev
