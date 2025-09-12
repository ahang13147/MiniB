# MiniB

一个基于 Vue3 + Spring Boot 的校企协同管理演示项目，包含多角色（超级管理员/高校管理员/企业管理员/教师/学生/企业导师）登录与基于权限的菜单展示。

## 后端启动（Spring Boot）

1. 进入 `backend` 目录：
2. 执行构建并运行：

```bash
mvn -q -f backend/pom.xml spring-boot:run
```

服务默认运行在 `http://localhost:8081`。

演示登录用户名与对应角色：

- admin: SUPER_ADMIN
- uadmin: UNIVERSITY_ADMIN
- eadmin: ENTERPRISE_ADMIN
- teacher: TEACHER
- student: STUDENT
- mentor: ENTERPRISE_MENTOR

密码任意（演示环境）。

## 前端启动（Vite + Vue3）

1. 安装依赖（PowerShell 下命令以分号分隔）：

```bash
npm i
```

2. 启动开发服务器：

```bash
npm run dev
```

前端默认运行在 `http://localhost:5173`（若被占用会自动切到 `5174` 等），已通过代理把 `/api` 转发到后端 `http://localhost:8081`。

## Windows PowerShell 常见命令写法

- 在项目根目录启动后端（推荐）：

```powershell
mvn -q -f backend/pom.xml spring-boot:run
```

- 若已进入 `backend` 目录：

```powershell
mvn -q spring-boot:run
```

> 注意：不要在 `backend` 目录里再使用 `-f backend/pom.xml`，否则会指向不存在的 `backend/backend/pom.xml`。

## 端口占用说明

- 如果 8080 被占用，后端已配置使用 8081 端口（见 `backend/src/main/resources/application.properties`）。
- 如需修改端口，可在 `application.properties` 中更改 `server.port`，或在运行时追加参数：

```powershell
mvn -q -f backend/pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

## 功能说明

- 登录后返回 `token`、`用户信息`、`角色`、`权限列表`。
- 前端基于路由守卫校验 `token`，并按权限动态展示卡片菜单。

