@echo off
echo ========================================
echo 启动大学企业联盟平台后端服务
echo ========================================

echo 检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请安装JDK 17+
    pause
    exit /b 1
)

echo.
echo 启动Spring Boot应用...
echo 服务将运行在: http://localhost:8081
echo.

cd backend
mvn spring-boot:run

if %errorlevel% neq 0 (
    echo.
    echo 启动失败，尝试使用Maven Wrapper...
    if exist mvnw.cmd (
        mvnw.cmd spring-boot:run
    ) else (
        echo 错误: 无法启动后端服务
        echo 请检查Maven是否正确安装，或使用IDE启动
        pause
        exit /b 1
    )
)

pause
