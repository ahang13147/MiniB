# 🚀 快速启动指南

## 最简单启动方式

### Windows用户
```bash
# 1. 双击运行
start-all.bat

# 2. 等待两个窗口都启动完成
# 3. 访问 http://localhost:5173
```

### 手动启动（如果自动脚本有问题）

**启动后端**
```bash
# 方法1: 使用Maven（推荐）
mvn -q -f backend/pom.xml spring-boot:run

# 方法2: 使用Maven Wrapper
backend\mvnw.cmd spring-boot:run
```

**启动前端**
```bash
# 1. 安装依赖
npm install

# 2. 启动服务
npm run dev
```

## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin | 超级管理员 |
| student | student | 学生 |
| teacher | teacher | 教师 |
| eadmin | eadmin | 企业管理员 |

## 常见问题

**1. 端口被占用**
- 后端8081端口：修改 `backend/src/main/resources/application.properties`
- 前端5173端口：会自动切换到5174

**2. Java版本问题**
- 需要JDK 17+，下载地址：https://adoptium.net/

**3. Node.js版本问题**
- 需要Node.js 16+，下载地址：https://nodejs.org/

**4. 依赖安装失败**
```bash
# 清理重新安装
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

## 访问地址

- 前端：http://localhost:5173
- 后端：http://localhost:8081

## 功能说明

登录后可以看到不同的功能菜单：
- 超级管理员：所有功能
- 学生：成果管理、实习申请、资源使用
- 教师：课程管理、学生指导、成果验证
- 企业管理员：项目管理、实习职位、企业信息
