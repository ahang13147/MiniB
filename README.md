# 🎓 University–Enterprise Collaboration Platform (MiniB)

A full-stack university–enterprise collaboration management platform based on Vue 3 and Spring Boot. It supports multi-role access control, project collaboration, internship & employment management, achievement showcase, and more.
##  DEMO
https://github.com/user-attachments/assets/657bd716-b349-45cd-8182-41e91c55b43e

## 📋 Features
### 🔐 Access Control System

* **6 User Roles**

  * Super Administrator
  * University Administrator
  * Enterprise Administrator
  * Teacher
  * Student
  * Enterprise Mentor

* **Fine-Grained Permission Control**

  * Role-based functional and operation-level permissions

* **JWT Authentication**

  * Secure token-based identity authentication

### 🚀 Core Modules

* **🤝 Project Collaboration Management**

  * Project publishing, matching, full lifecycle management, funding management

* **💼 Internship & Employment Management**

  * Job posting, application workflow, internship record management

* **🏆 Achievement Showcase Center**

  * Student achievement display, validation & review, statistics & analysis

* **👨‍🏫 Dual-Mentor Classroom**

  * University–enterprise joint teaching, course selection, mentor management

* **📚 Resource Sharing Center**

  * Research resource sharing, course resource repository

* **🏢 University–Enterprise Information Management**

  * University and enterprise information display and management

## 🛠 Tech Stack

### Backend

* **Framework**: Spring Boot 3.3.2
* **Java Version**: JDK 17+
* **Authentication**: JWT Token
* **API Style**: RESTful API
* **Build Tool**: Maven

### Frontend

* **Framework**: Vue 3 + TypeScript
* **UI Library**: Element Plus
* **State Management**: Pinia
* **Routing**: Vue Router 4
* **Build Tool**: Vite
* **HTTP Client**: Axios

## 🚀 Getting Started

### Prerequisites

* **Java**: JDK 17 or higher
* **Node.js**: 16 or higher
* **Maven**: 3.6+ (optional if you use Maven Wrapper)
* **npm**: 8+ (comes with Node.js)

### ⚡ One-Click Start (Recommended)

#### For Windows Users

```bash
# Check environment
check-environment.bat

# Start all services
start-all.bat

# Or start separately
start-backend.bat    # Start backend
start-frontend.bat   # Start frontend
```

#### For Linux/Mac Users

```bash
# Add execution permission (first run only)
chmod +x *.sh

# Check environment
./check-environment.sh

# Start all services
./start-all.sh

# Or start separately
./start-backend.sh    # Start backend
./start-frontend.sh   # Start frontend
```

### 📋 Manual Start

#### 1. Start Backend Service

**Option 1: Run from project root (recommended)**

```bash
# Windows PowerShell
mvn -q -f backend/pom.xml spring-boot:run

# Linux/Mac
./mvnw -f backend/pom.xml spring-boot:run
```

**Option 2: cd into `backend` first**

```bash
# Windows PowerShell
cd backend
mvn -q spring-boot:run

# Linux/Mac
cd backend
./mvnw spring-boot:run
```

**Option 3: Use Maven Wrapper (recommended, Maven not required globally)**

```bash
# Windows
backend\mvnw.cmd spring-boot:run

# Linux/Mac
./backend/mvnw spring-boot:run
```

Backend service runs at: `http://localhost:8081`

#### 2. Start Frontend Service

**Install dependencies**

```bash
npm install
```

**Start dev server**

```bash
npm run dev
```

Frontend service runs at: `http://localhost:5173`

### 3. Access the System

1. Open your browser and visit: `http://localhost:5173`
2. Log in with one of the following test accounts:

| Username | Password | Role                | Description                                       |
| -------- | -------- | ------------------- | ------------------------------------------------- |
| admin    | admin    | Super Administrator | Full access to all features                       |
| uadmin   | uadmin   | University Admin    | Manage on-campus affairs, courses, students, etc. |
| eadmin   | eadmin   | Enterprise Admin    | Manage enterprise info, internships, projects     |
| teacher  | teacher  | Teacher             | Course management, student guidance, verification |
| student  | student  | Student             | Achievement management, internship applications   |
| mentor   | mentor   | Enterprise Mentor   | Dual-mentor courses, student guidance, projects   |

---

## 🔧 Troubleshooting

### 🚨 Quick Diagnostics

**Run environment check script**

```bash
# Windows
check-environment.bat

# Linux/Mac
./check-environment.sh
```

### Backend Startup Issues

**1. Java Version Issue**

```bash
# Check Java version
java -version

# If lower than 17, please install JDK 17+
# Download: https://adoptium.net/
```

**2. Maven Issues**

```bash
# If Maven is not installed, use the bundled Maven Wrapper
# Windows
backend\mvnw.cmd spring-boot:run

# Linux/Mac
./backend/mvnw spring-boot:run

# Or manually download Maven: https://maven.apache.org/download.cgi
```

**3. Port Conflict**

* Backend uses port `8081` by default. If it is already in use:

  * Modify `backend/src/main/resources/application.properties` and change `server.port=8081`, or
  * Specify port at runtime:

```bash
mvn -q -f backend/pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

**4. Compilation Errors**

```bash
# Clean and recompile
mvn clean -f backend/pom.xml
mvn compile -f backend/pom.xml

# If still failing, force dependency update
mvn clean compile -U -f backend/pom.xml
```

**5. Classpath Issues**

```bash
# Check target directory
ls backend/target/classes/com/minib/

# If classes directory is empty, recompile
mvn clean compile -f backend/pom.xml
```

### Frontend Startup Issues

**1. Node.js Version Issue**

```bash
# Check Node.js version
node --version
npm --version

# Recommended Node.js 16+, download: https://nodejs.org/
```

**2. Dependency Installation Issues**

```bash
# Clear cache and reinstall
npm cache clean --force
rm -rf node_modules package-lock.json
npm install

# If network issues occur, use mirror registry
npm config set registry https://registry.npmmirror.com
npm install
```

**3. Port Conflict**

* Frontend uses port `5173` by default.
* If the port is occupied, Vite will automatically switch to `5174` or another available port.
* You can also explicitly specify a port:

```bash
npm run dev -- --port 3000
```

**4. Permission Issues (Linux/Mac)**

```bash
# Add execution permission for scripts
chmod +x *.sh

# If npm install permission issues occur
sudo npm install
```

### Common Errors & Solutions

**1. "Cannot find module" Error**

```bash
# Remove node_modules and lock file, then reinstall
rm -rf node_modules package-lock.json
npm install
```

**2. "Port already in use" Error**

```bash
# Find process using the port
# Windows
netstat -ano | findstr :8081
netstat -ano | findstr :5173

# Linux/Mac
lsof -i :8081
lsof -i :5173

# Kill process (replace PID)
# Windows
taskkill /PID <PID> /F

# Linux/Mac
kill -9 <PID>
```

**3. "Java heap space" Error**

```bash
# Increase JVM memory
export MAVEN_OPTS="-Xmx1024m -Xms512m"
mvn spring-boot:run
```

**4. "CORS" Issues**

* Frontend has already configured a proxy: `/api` requests are proxied to the backend.
* If CORS issues persist, check the proxy configuration in `vite.config.ts`.

**5. Login Failure**

* Ensure backend service has started (`http://localhost:8081`)
* Check browser console for network errors
* Try another test account

### Performance Optimization

**1. Backend Optimization**

```bash
# Start with production profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Or package and run the jar
mvn clean package -f backend/pom.xml
java -jar backend/target/minib-backend-0.0.1-SNAPSHOT.jar
```

**2. Frontend Optimization**

```bash
# Production build
npm run build

# Preview production build
npm run preview
```

---

## 📱 Feature Demo

### Login

1. Visit `http://localhost:5173`
2. Log in with any test account
3. The system will display different menus and features based on the user role

### Main Features

* **Dashboard**: Role-based statistics and entry points
* **Project Collaboration**: Create and manage university–enterprise collaboration projects
* **Internship & Employment**: Post jobs, apply for internships, manage internship records
* **Achievement Showcase**: Display student achievements and perform validation/review
* **Dual-Mentor Classroom**: Manage university–enterprise joint courses
* **Resource Sharing**: Manage research equipment and course resources

---

## 🔍 API Endpoints

### Authentication

* `POST /api/auth/login` – User login
* `POST /api/auth/register` – User registration
* `POST /api/auth/forgot` – Forgot password
* `POST /api/auth/reset` – Reset password

### Project Management

* `GET /api/projects` – Get project list
* `POST /api/projects` – Create project
* `PUT /api/projects/{id}` – Update project
* `DELETE /api/projects/{id}` – Delete project

### Internship Management

* `GET /api/internships/positions` – Get internship positions
* `POST /api/internships/positions` – Post internship position
* `POST /api/internships/positions/{id}/apply` – Apply for internship

### Achievement Management

* `GET /api/achievements` – Get achievement list
* `POST /api/achievements` – Create achievement
* `POST /api/achievements/{id}/verify` – Verify achievement

### Mentor Management

* `GET /api/mentors/courses` – Get dual-mentor courses
* `POST /api/mentors/courses` – Create dual-mentor course
* `GET /api/mentors/enterprise-mentors` – Get enterprise mentors

---

## 📊 Database Design

The project includes a complete database design script `database_design.sql`, which covers all module schemas, including:

* User & permission management
* Project collaboration workflow
* Internship & employment management
* Achievement showcase & verification
* Dual-mentor classroom
* Resource sharing

---

## 🎯 Development Notes

### Project Structure

```text
MiniB/
├── backend/                      # Spring Boot backend
│   ├── src/main/java/com/minib/
│   │   ├── auth/                 # Authentication & authorization
│   │   ├── projects/             # Project collaboration
│   │   ├── internships/          # Internships & employment
│   │   ├── achievements/         # Achievement showcase
│   │   ├── mentors/              # Dual-mentor classroom
│   │   └── ...                   # Other modules
│   └── pom.xml                   # Maven configuration
├── src/                          # Vue 3 frontend
│   ├── views/                    # Page components
│   ├── api/                      # API layer
│   ├── stores/                   # State stores (Pinia)
│   └── router/                   # Route configuration
└── database_design.sql           # Database design
```

### Permission System

* Role-Based Access Control (RBAC)
* Fine-grained functional and operation-level permissions
* Frontend route guards and component-level permission control
* Backend API permission verification

---

## 📞 Support

If you encounter issues, please check:

1. Java version is 17+
2. Node.js version is 16+
3. Ports `8081` and `5173` are not occupied
4. Network connection is stable
5. Firewall rules allow access to these ports

---

## 🎉 Highlights

* **Modular Design**: Independent functional modules, easy to maintain and extend
* **Precise Access Control**: Role-based fine-grained permission management
* **Modern UI**: Responsive design and user-friendly interface
* **Complete Business Flow**: Covers all major aspects of university–enterprise collaboration
* **Data Visualization**: Rich charts and analytics for decision support
* **Extensible Architecture**: Designed for future feature extensions and customization
