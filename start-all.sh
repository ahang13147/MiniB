#!/bin/bash

echo "========================================"
echo "启动大学企业联盟平台完整服务"
echo "========================================"

echo "启动后端服务..."
gnome-terminal -- bash -c "./start-backend.sh; exec bash" 2>/dev/null || \
xterm -e "./start-backend.sh" 2>/dev/null || \
osascript -e 'tell app "Terminal" to do script "./start-backend.sh"' 2>/dev/null || \
echo "请在新的终端窗口中手动运行: ./start-backend.sh"

echo "等待后端服务启动..."
sleep 5

echo "启动前端服务..."
gnome-terminal -- bash -c "./start-frontend.sh; exec bash" 2>/dev/null || \
xterm -e "./start-frontend.sh" 2>/dev/null || \
osascript -e 'tell app "Terminal" to do script "./start-frontend.sh"' 2>/dev/null || \
echo "请在新的终端窗口中手动运行: ./start-frontend.sh"

echo ""
echo "========================================"
echo "服务启动完成！"
echo "========================================"
echo "后端服务: http://localhost:8081"
echo "前端服务: http://localhost:5173"
echo ""
echo "测试账号:"
echo "admin/admin - 超级管理员"
echo "student/student - 学生"
echo "teacher/teacher - 教师"
echo "eadmin/eadmin - 企业管理员"
echo ""
echo "请等待前端服务完全启动后访问 http://localhost:5173"
echo "========================================"
