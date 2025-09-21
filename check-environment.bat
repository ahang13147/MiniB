@echo off
echo ========================================
echo 大学企业联盟平台环境检查
echo ========================================

echo.
echo 1. 检查Java环境...
java -version 2>nul
if %errorlevel% equ 0 (
    echo [✓] Java环境正常
) else (
    echo [✗] Java环境异常 - 请安装JDK 17+
)

echo.
echo 2. 检查Maven环境...
mvn -version 2>nul
if %errorlevel% equ 0 (
    echo [✓] Maven环境正常
) else (
    echo [!] Maven未安装 - 将使用Maven Wrapper
)

echo.
echo 3. 检查Maven Wrapper...
if exist "backend\mvnw.cmd" (
    echo [✓] Maven Wrapper存在
) else (
    echo [✗] Maven Wrapper不存在
)

echo.
echo 4. 检查Node.js环境...
node --version 2>nul
if %errorlevel% equ 0 (
    echo [✓] Node.js环境正常
) else (
    echo [✗] Node.js环境异常 - 请安装Node.js 16+
)

echo.
echo 5. 检查npm环境...
npm --version 2>nul
if %errorlevel% equ 0 (
    echo [✓] npm环境正常
) else (
    echo [✗] npm环境异常
)

echo.
echo 6. 检查端口占用...
netstat -an | findstr ":8081" >nul
if %errorlevel% equ 0 (
    echo [!] 端口8081已被占用
) else (
    echo [✓] 端口8081可用
)

netstat -an | findstr ":5173" >nul
if %errorlevel% equ 0 (
    echo [!] 端口5173已被占用
) else (
    echo [✓] 端口5173可用
)

echo.
echo 7. 检查项目文件...
if exist "backend\src\main\java\com\minib\MiniBApplication.java" (
    echo [✓] 后端主文件存在
) else (
    echo [✗] 后端主文件不存在
)

if exist "src\main.ts" (
    echo [✓] 前端主文件存在
) else (
    echo [✗] 前端主文件不存在
)

if exist "package.json" (
    echo [✓] 前端配置文件存在
) else (
    echo [✗] 前端配置文件不存在
)

if exist "backend\pom.xml" (
    echo [✓] 后端配置文件存在
) else (
    echo [✗] 后端配置文件不存在
)

echo.
echo ========================================
echo 环境检查完成
echo ========================================

if %errorlevel% neq 0 (
    echo.
    echo 发现以下问题需要解决:
    echo 1. 请确保Java版本为17或更高
    echo 2. 请确保Node.js版本为16或更高
    echo 3. 如果端口被占用，请关闭相关程序或修改配置
    echo 4. 确保所有项目文件完整
)

pause
