# 🎓 大学企业联盟平台 (MiniB)

一个基于 Vue3 + Spring Boot 的完整校企协同管理平台，支持多角色权限管理、项目协作、实习就业、成果展示等核心功能。

## 📋 系统功能

### 🔐 权限管理系统
- **6种用户角色**：超级管理员、高校管理员、企业管理员、教师、学生、企业导师
- **细粒度权限控制**：基于角色的功能权限和操作权限
- **JWT身份认证**：安全的token认证机制

### 🚀 核心功能模块
- **🤝 项目协作管理**：项目发布、匹配、全周期管理、资金管理
- **💼 实习就业管理**：职位发布、申请流程、实习记录管理
- **🏆 成果展示中心**：学生成果展示、验证审核、统计分析
- **👨‍🏫 双导师课堂**：校企合作教学、选课管理、导师管理
- **📚 资源共享中心**：科研资源共享、课程资源库
- **🏢 校企信息管理**：高校和企业信息展示管理

## 🛠 技术栈

### 后端
- **框架**：Spring Boot 3.3.2
- **Java版本**：JDK 17+
- **认证**：JWT Token
- **API**：RESTful API
- **构建工具**：Maven

### 前端
- **框架**：Vue 3 + TypeScript
- **UI组件**：Element Plus
- **状态管理**：Pinia
- **路由**：Vue Router 4
- **构建工具**：Vite
- **HTTP客户端**：Axios

## 🚀 快速启动

### 环境要求
- **Java**: JDK 17 或更高版本
- **Node.js**: 16 或更高版本
- **Maven**: 3.6+ (如果系统已安装，可选)
- **npm**: 8+ (随Node.js安装)

### ⚡ 一键启动（推荐）

**Windows用户**
```bash
# 检查环境
check-environment.bat

# 一键启动所有服务
start-all.bat

# 或分别启动
start-backend.bat    # 启动后端
start-frontend.bat   # 启动前端
```

**Linux/Mac用户**
```bash
# 添加执行权限（首次运行）
chmod +x *.sh

# 检查环境
./check-environment.sh

# 一键启动所有服务
./start-all.sh

# 或分别启动
./start-backend.sh    # 启动后端
./start-frontend.sh   # 启动前端
```

### 📋 手动启动

**1. 启动后端服务**

**方法一：在项目根目录启动（推荐）**
```bash
# Windows PowerShell
mvn -q -f backend/pom.xml spring-boot:run

# Linux/Mac
./mvnw -f backend/pom.xml spring-boot:run
```

**方法二：进入backend目录启动**
```bash
# Windows PowerShell
cd backend
mvn -q spring-boot:run

# Linux/Mac
cd backend
./mvnw spring-boot:run
```

**方法三：使用Maven Wrapper（推荐，无需安装Maven）**
```bash
# Windows
backend\mvnw.cmd spring-boot:run

# Linux/Mac
./backend/mvnw spring-boot:run
```

后端服务启动后运行在：`http://localhost:8081`

**2. 启动前端服务**

**安装依赖**
```bash
npm install
```

**启动开发服务器**
```bash
npm run dev
```

前端服务启动后运行在：`http://localhost:5173`

### 3. 访问系统

1. 打开浏览器访问：`http://localhost:5173`
2. 使用以下测试账号登录：

| 用户名 | 密码 | 角色 | 权限说明 |
|--------|------|------|----------|
| admin | admin | 超级管理员 | 拥有所有功能权限 |
| uadmin | uadmin | 高校管理员 | 管理校内事务、课程、学生等 |
| eadmin | eadmin | 企业管理员 | 管理企业信息、实习职位、项目等 |
| teacher | teacher | 教师 | 课程管理、学生指导、成果验证 |
| student | student | 学生 | 成果管理、实习申请、资源使用 |
| mentor | mentor | 企业导师 | 双导师课堂、学生指导、项目管理 |

## 🔧 常见问题解决

### 🚨 快速诊断

**运行环境检查脚本**
```bash
# Windows
check-environment.bat

# Linux/Mac
./check-environment.sh
```

### 后端启动问题

**1. Java版本问题**
```bash
# 检查Java版本
java -version

# 如果版本低于17，请安装JDK 17+
# 下载地址: https://adoptium.net/
```

**2. Maven问题**
```bash
# 如果没有安装Maven，使用项目自带的Maven Wrapper
# Windows
backend\mvnw.cmd spring-boot:run

# Linux/Mac
./backend/mvnw spring-boot:run

# 或者手动下载Maven: https://maven.apache.org/download.cgi
```

**3. 端口占用问题**
- 后端默认端口8081，如果被占用：
  - 修改 `backend/src/main/resources/application.properties` 中的 `server.port=8081`
  - 或者运行时指定端口：
```bash
mvn -q -f backend/pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

**4. 编译错误**
```bash
# 清理并重新编译
mvn clean -f backend/pom.xml
mvn compile -f backend/pom.xml

# 如果仍有问题，尝试强制更新依赖
mvn clean compile -U -f backend/pom.xml
```

**5. 类路径问题**
```bash
# 检查target目录
ls backend/target/classes/com/minib/

# 如果classes目录为空，重新编译
mvn clean compile -f backend/pom.xml
```

### 前端启动问题

**1. Node.js版本问题**
```bash
# 检查Node.js版本
node --version
npm --version

# 推荐使用Node.js 16+，下载地址: https://nodejs.org/
```

**2. 依赖安装问题**
```bash
# 清理缓存重新安装
npm cache clean --force
rm -rf node_modules package-lock.json
npm install

# 如果网络问题，使用国内镜像
npm config set registry https://registry.npmmirror.com
npm install
```

**3. 端口占用问题**
- 前端默认端口5173，如果被占用会自动切换到5174等
- 也可以手动指定端口：
```bash
npm run dev -- --port 3000
```

**4. 权限问题（Linux/Mac）**
```bash
# 给脚本添加执行权限
chmod +x *.sh

# 如果npm install权限问题
sudo npm install
```

### 常见错误及解决方案

**1. "Cannot find module" 错误**
```bash
# 删除node_modules和package-lock.json，重新安装
rm -rf node_modules package-lock.json
npm install
```

**2. "Port already in use" 错误**
```bash
# 查找占用端口的进程
# Windows
netstat -ano | findstr :8081
netstat -ano | findstr :5173

# Linux/Mac
lsof -i :8081
lsof -i :5173

# 杀死进程（替换PID）
# Windows
taskkill /PID <进程ID> /F

# Linux/Mac
kill -9 <进程ID>
```

**3. "Java heap space" 错误**
```bash
# 增加JVM内存
export MAVEN_OPTS="-Xmx1024m -Xms512m"
mvn spring-boot:run
```

**4. "CORS" 跨域问题**
- 前端已配置代理，`/api` 请求会自动转发到后端
- 如果仍有问题，检查 `vite.config.ts` 中的代理配置

**5. 登录失败**
- 确保后端服务已启动（http://localhost:8081）
- 检查浏览器控制台是否有网络错误
- 尝试使用不同的测试账号

### 性能优化

**1. 后端优化**
```bash
# 使用生产模式启动
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# 或者打包后运行
mvn clean package -f backend/pom.xml
java -jar backend/target/minib-backend-0.0.1-SNAPSHOT.jar
```

**2. 前端优化**
```bash
# 生产构建
npm run build

# 预览生产版本
npm run preview
```

## 📱 功能演示

### 登录系统
1. 访问 `http://localhost:5173`
2. 使用任意测试账号登录
3. 系统会根据角色显示不同的功能菜单

### 主要功能
- **仪表盘**：根据角色显示相关统计数据和功能入口
- **项目协作**：创建、管理校企合作项目
- **实习就业**：发布职位、申请实习、管理实习记录
- **成果展示**：展示学生成果、验证审核
- **双导师课堂**：管理校企合作课程
- **资源共享**：科研设备和课程资源管理

## 🔍 API接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/forgot` - 忘记密码
- `POST /api/auth/reset` - 重置密码

### 项目管理
- `GET /api/projects` - 获取项目列表
- `POST /api/projects` - 创建项目
- `PUT /api/projects/{id}` - 更新项目
- `DELETE /api/projects/{id}` - 删除项目

### 实习管理
- `GET /api/internships/positions` - 获取实习职位
- `POST /api/internships/positions` - 发布职位
- `POST /api/internships/positions/{id}/apply` - 申请实习

### 成果管理
- `GET /api/achievements` - 获取成果列表
- `POST /api/achievements` - 创建成果
- `POST /api/achievements/{id}/verify` - 验证成果

### 导师管理
- `GET /api/mentors/courses` - 获取双导师课程
- `POST /api/mentors/courses` - 创建课程
- `GET /api/mentors/enterprise-mentors` - 获取企业导师

## 📊 数据库设计

项目包含完整的数据库设计文档 `database_design.sql`，涵盖了所有功能模块的数据表结构，支持：
- 用户权限管理
- 项目协作流程
- 实习就业管理
- 成果展示验证
- 双导师课堂
- 资源共享

## 🎯 开发说明

### 项目结构
```
MiniB/
├── backend/                 # Spring Boot后端
│   ├── src/main/java/com/minib/
│   │   ├── auth/           # 认证授权
│   │   ├── projects/       # 项目协作
│   │   ├── internships/    # 实习就业
│   │   ├── achievements/   # 成果展示
│   │   ├── mentors/        # 双导师课堂
│   │   └── ...            # 其他模块
│   └── pom.xml            # Maven配置
├── src/                   # Vue3前端
│   ├── views/             # 页面组件
│   ├── api/               # API接口
│   ├── stores/            # 状态管理
│   └── router/            # 路由配置
└── database_design.sql    # 数据库设计
```

### 权限系统
- 基于角色的权限控制（RBAC）
- 细粒度的功能权限和操作权限
- 前端路由守卫和组件权限控制
- 后端API权限验证

## 📞 技术支持

如果遇到问题，请检查：
1. Java版本是否为17+
2. Node.js版本是否为16+
3. 端口8081和5173是否被占用
4. 网络连接是否正常
5. 防火墙设置是否允许访问

## 🎉 系统特色

- **模块化设计**：功能模块独立，易于维护和扩展
- **权限精确控制**：基于角色的细粒度权限管理
- **现代化UI**：响应式设计，用户体验优良
- **完整业务流程**：覆盖校企合作的各个环节
- **数据可视化**：丰富的统计图表和数据分析
- **可扩展架构**：支持后续功能扩展和定制开发


