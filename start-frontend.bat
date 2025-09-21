@echo off
echo ========================================
echo 启动大学企业联盟平台前端服务
echo ========================================

echo 检查Node.js环境...
node --version
if %errorlevel% neq 0 (
    echo 错误: 未找到Node.js环境，请安装Node.js 16+
    pause
    exit /b 1
)

echo.
echo 检查npm...
npm --version
if %errorlevel% neq 0 (
    echo 错误: 未找到npm，请重新安装Node.js
    pause
    exit /b 1
)

echo.
echo 安装依赖包...
npm install
if %errorlevel% neq 0 (
    echo 错误: 依赖安装失败
    pause
    exit /b 1
)

echo.
echo 启动前端开发服务器...
echo 服务将运行在: http://localhost:5173
echo.

npm run dev

pause
