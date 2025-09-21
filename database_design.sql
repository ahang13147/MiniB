-- 大学企业联盟平台数据库设计
-- 基于现有代码结构扩展的完整数据库表结构

-- ================================
-- 基础数据表 (扩展现有结构)
-- ================================

-- 用户表 (扩展现有User结构)
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    role ENUM('SUPER_ADMIN', 'UNIVERSITY_ADMIN', 'ENTERPRISE_ADMIN', 'TEACHER', 'STUDENT', 'ENTERPRISE_MENTOR') NOT NULL,
    department VARCHAR(100),
    position VARCHAR(100),
    avatar_url VARCHAR(500),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    -- 扩展字段
    university_id VARCHAR(36),
    company_id VARCHAR(36),
    profile_completed BOOLEAN DEFAULT FALSE,
    verification_status ENUM('PENDING', 'VERIFIED', 'REJECTED') DEFAULT 'PENDING'
);

-- 大学信息表 (新增)
CREATE TABLE universities (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    short_name VARCHAR(50),
    description TEXT,
    website VARCHAR(200),
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    logo_url VARCHAR(500),
    established_year INT,
    university_type ENUM('985', '211', 'DOUBLE_FIRST_CLASS', 'REGULAR', 'VOCATIONAL') DEFAULT 'REGULAR',
    status ENUM('ACTIVE', 'INACTIVE', 'PENDING') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 院系专业表 (新增)
CREATE TABLE departments (
    id VARCHAR(36) PRIMARY KEY,
    university_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    head_teacher_id VARCHAR(36),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (university_id) REFERENCES universities(id) ON DELETE CASCADE,
    FOREIGN KEY (head_teacher_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE majors (
    id VARCHAR(36) PRIMARY KEY,
    department_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE,
    description TEXT,
    degree_type ENUM('BACHELOR', 'MASTER', 'DOCTOR') DEFAULT 'BACHELOR',
    duration_years INT DEFAULT 4,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
);

-- 企业信息表 (扩展现有Company结构)
CREATE TABLE companies (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    short_name VARCHAR(50),
    description TEXT,
    industry VARCHAR(100),
    scale ENUM('STARTUP', 'SMALL', 'MEDIUM', 'LARGE', 'ENTERPRISE') DEFAULT 'MEDIUM',
    website VARCHAR(200),
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    logo_url VARCHAR(500),
    contact_person VARCHAR(100),
    contact_title VARCHAR(100),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    registration_number VARCHAR(100),
    established_year INT,
    employee_count INT,
    annual_revenue DECIMAL(15,2),
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED') DEFAULT 'PENDING',
    verification_status ENUM('PENDING', 'VERIFIED', 'REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ================================
-- 项目协作管理模块
-- ================================

-- 项目表
CREATE TABLE projects (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    project_type ENUM('RESEARCH', 'DEVELOPMENT', 'CONSULTING', 'TRAINING', 'INTERNSHIP') NOT NULL,
    initiator_type ENUM('UNIVERSITY', 'COMPANY') NOT NULL,
    initiator_id VARCHAR(36) NOT NULL, -- university_id 或 company_id
    partner_type ENUM('UNIVERSITY', 'COMPANY'),
    partner_id VARCHAR(36), -- 匹配的合作伙伴
    status ENUM('DRAFT', 'PUBLISHED', 'MATCHING', 'NEGOTIATING', 'APPROVED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'DRAFT',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    budget DECIMAL(15,2),
    currency VARCHAR(10) DEFAULT 'CNY',
    start_date DATE,
    end_date DATE,
    actual_start_date DATE,
    actual_end_date DATE,
    tags JSON, -- 项目标签，便于匹配
    requirements TEXT, -- 项目需求描述
    deliverables TEXT, -- 项目交付物
    progress_percentage INT DEFAULT 0,
    created_by VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 项目参与人员表
CREATE TABLE project_participants (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role ENUM('LEADER', 'MEMBER', 'MENTOR', 'SUPERVISOR') DEFAULT 'MEMBER',
    join_date DATE DEFAULT (CURRENT_DATE),
    status ENUM('ACTIVE', 'INACTIVE', 'LEFT') DEFAULT 'ACTIVE',
    contribution_percentage DECIMAL(5,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_project_user (project_id, user_id)
);

-- 项目里程碑表
CREATE TABLE project_milestones (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    planned_date DATE,
    actual_date DATE,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'DELAYED') DEFAULT 'PENDING',
    progress_percentage INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- 项目资金管理表
CREATE TABLE project_funds (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    fund_type ENUM('BUDGET', 'EXPENSE', 'INCOME') NOT NULL,
    category VARCHAR(100), -- 费用类别：人工费、设备费、材料费等
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'CNY',
    description TEXT,
    transaction_date DATE DEFAULT (CURRENT_DATE),
    status ENUM('PLANNED', 'APPROVED', 'PAID', 'CANCELLED') DEFAULT 'PLANNED',
    created_by VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- ================================
-- 人才培养与交流模块
-- ================================

-- 实习职位表
CREATE TABLE internship_positions (
    id VARCHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    department VARCHAR(100),
    location VARCHAR(200),
    position_type ENUM('FULL_TIME', 'PART_TIME', 'REMOTE') DEFAULT 'FULL_TIME',
    duration_months INT,
    salary_min DECIMAL(10,2),
    salary_max DECIMAL(10,2),
    currency VARCHAR(10) DEFAULT 'CNY',
    requirements TEXT, -- 岗位要求
    responsibilities TEXT, -- 工作职责
    skills_required JSON, -- 技能要求
    major_preferred JSON, -- 专业偏好
    grade_requirement ENUM('FRESHMAN', 'SOPHOMORE', 'JUNIOR', 'SENIOR', 'GRADUATE', 'ANY') DEFAULT 'ANY',
    positions_available INT DEFAULT 1,
    positions_filled INT DEFAULT 0,
    application_deadline DATE,
    start_date DATE,
    end_date DATE,
    status ENUM('DRAFT', 'PUBLISHED', 'CLOSED', 'CANCELLED') DEFAULT 'DRAFT',
    created_by VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 实习申请表
CREATE TABLE internship_applications (
    id VARCHAR(36) PRIMARY KEY,
    position_id VARCHAR(36) NOT NULL,
    student_id VARCHAR(36) NOT NULL,
    cover_letter TEXT,
    resume_url VARCHAR(500),
    portfolio_url VARCHAR(500),
    gpa DECIMAL(3,2),
    skills JSON, -- 学生技能
    experience TEXT, -- 相关经验
    status ENUM('SUBMITTED', 'UNDER_REVIEW', 'INTERVIEW_SCHEDULED', 'INTERVIEWED', 'ACCEPTED', 'REJECTED', 'WITHDRAWN') DEFAULT 'SUBMITTED',
    interview_date TIMESTAMP NULL,
    interview_feedback TEXT,
    rejection_reason TEXT,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (position_id) REFERENCES internship_positions(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_position_student (position_id, student_id)
);

-- 实习记录表
CREATE TABLE internship_records (
    id VARCHAR(36) PRIMARY KEY,
    application_id VARCHAR(36) NOT NULL,
    student_id VARCHAR(36) NOT NULL,
    company_id VARCHAR(36) NOT NULL,
    position_title VARCHAR(200),
    mentor_id VARCHAR(36), -- 企业导师
    supervisor_id VARCHAR(36), -- 学校指导老师
    start_date DATE,
    end_date DATE,
    actual_end_date DATE,
    department VARCHAR(100),
    location VARCHAR(200),
    salary DECIMAL(10,2),
    status ENUM('ONGOING', 'COMPLETED', 'TERMINATED', 'EXTENDED') DEFAULT 'ONGOING',
    performance_score DECIMAL(3,2), -- 实习表现评分
    mentor_feedback TEXT,
    supervisor_feedback TEXT,
    student_feedback TEXT,
    certificate_url VARCHAR(500), -- 实习证明
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES internship_applications(id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (mentor_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (supervisor_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 双导师课堂表
CREATE TABLE dual_mentor_courses (
    id VARCHAR(36) PRIMARY KEY,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    description TEXT,
    university_id VARCHAR(36) NOT NULL,
    company_id VARCHAR(36) NOT NULL,
    university_teacher_id VARCHAR(36) NOT NULL, -- 高校导师
    enterprise_mentor_id VARCHAR(36) NOT NULL, -- 企业导师
    department VARCHAR(100),
    major_id VARCHAR(36),
    semester VARCHAR(20),
    academic_year VARCHAR(20),
    credits INT DEFAULT 0,
    total_hours INT DEFAULT 0,
    theory_hours INT DEFAULT 0,
    practice_hours INT DEFAULT 0,
    max_students INT DEFAULT 30,
    enrolled_students INT DEFAULT 0,
    course_type ENUM('THEORY', 'PRACTICE', 'MIXED') DEFAULT 'MIXED',
    teaching_mode ENUM('ONLINE', 'OFFLINE', 'HYBRID') DEFAULT 'HYBRID',
    status ENUM('PLANNING', 'RECRUITING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'PLANNING',
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (university_id) REFERENCES universities(id),
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (university_teacher_id) REFERENCES users(id),
    FOREIGN KEY (enterprise_mentor_id) REFERENCES users(id),
    FOREIGN KEY (major_id) REFERENCES majors(id) ON DELETE SET NULL
);

-- 双导师课堂学生选课表
CREATE TABLE dual_course_enrollments (
    id VARCHAR(36) PRIMARY KEY,
    course_id VARCHAR(36) NOT NULL,
    student_id VARCHAR(36) NOT NULL,
    enrollment_date DATE DEFAULT (CURRENT_DATE),
    status ENUM('ENROLLED', 'COMPLETED', 'DROPPED', 'FAILED') DEFAULT 'ENROLLED',
    final_score DECIMAL(5,2),
    final_grade VARCHAR(10),
    theory_score DECIMAL(5,2),
    practice_score DECIMAL(5,2),
    attendance_rate DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES dual_mentor_courses(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_course_student (course_id, student_id)
);

-- 学生成果展示表
CREATE TABLE student_achievements (
    id VARCHAR(36) PRIMARY KEY,
    student_id VARCHAR(36) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    achievement_type ENUM('COMPETITION', 'RESEARCH', 'PROJECT', 'PATENT', 'PUBLICATION', 'CERTIFICATE', 'OTHER') NOT NULL,
    category VARCHAR(100), -- 具体类别
    level ENUM('SCHOOL', 'CITY', 'PROVINCE', 'NATIONAL', 'INTERNATIONAL') DEFAULT 'SCHOOL',
    award_rank VARCHAR(50), -- 获奖等级：一等奖、二等奖等
    organization VARCHAR(200), -- 颁发机构
    achievement_date DATE,
    certificate_url VARCHAR(500),
    attachment_urls JSON, -- 相关附件
    visibility ENUM('PRIVATE', 'UNIVERSITY', 'PUBLIC') DEFAULT 'UNIVERSITY',
    status ENUM('DRAFT', 'PUBLISHED', 'VERIFIED', 'REJECTED') DEFAULT 'DRAFT',
    verification_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ================================
-- 资源共享中心扩展
-- ================================

-- 科研资源表 (扩展现有结构)
CREATE TABLE research_resources (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    resource_type ENUM('LAB_EQUIPMENT', 'RESEARCH_DATA', 'TECHNICAL_DOC', 'SOFTWARE_TOOL', 'DATABASE') NOT NULL,
    category VARCHAR(100), -- 具体分类
    ownership ENUM('UNIVERSITY', 'COMPANY', 'SHARED') NOT NULL,
    owner_id VARCHAR(36) NOT NULL, -- university_id 或 company_id
    publisher_id VARCHAR(36) NOT NULL, -- 发布人user_id
    location VARCHAR(200), -- 物理位置（设备类）
    access_url VARCHAR(500), -- 访问链接（数据/文档类）
    specifications JSON, -- 技术规格
    usage_instructions TEXT, -- 使用说明
    booking_required BOOLEAN DEFAULT TRUE,
    max_booking_days INT DEFAULT 7,
    status ENUM('AVAILABLE', 'BORROWED', 'MAINTENANCE', 'RETIRED') DEFAULT 'AVAILABLE',
    current_borrower_id VARCHAR(36) NULL,
    borrow_start_date DATE NULL,
    borrow_end_date DATE NULL,
    booking_calendar JSON, -- 预约日历
    usage_fee DECIMAL(10,2) DEFAULT 0,
    currency VARCHAR(10) DEFAULT 'CNY',
    tags JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (publisher_id) REFERENCES users(id),
    FOREIGN KEY (current_borrower_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 资源借用申请表
CREATE TABLE resource_bookings (
    id VARCHAR(36) PRIMARY KEY,
    resource_id VARCHAR(36) NOT NULL,
    borrower_id VARCHAR(36) NOT NULL,
    purpose TEXT, -- 使用目的
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    actual_return_date DATE NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'IN_USE', 'RETURNED', 'OVERDUE', 'CANCELLED') DEFAULT 'PENDING',
    approval_notes TEXT,
    return_condition TEXT, -- 归还状态说明
    damage_report TEXT, -- 损坏报告
    fee_charged DECIMAL(10,2) DEFAULT 0,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP NULL,
    returned_at TIMESTAMP NULL,
    FOREIGN KEY (resource_id) REFERENCES research_resources(id) ON DELETE CASCADE,
    FOREIGN KEY (borrower_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 课程资源库表 (扩展现有结构)
CREATE TABLE course_resources (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    resource_type ENUM('QUALITY_COURSE', 'TEACHING_CASE', 'COURSEWARE', 'VIDEO', 'DOCUMENT', 'SOFTWARE', 'OTHER') NOT NULL,
    subject_area VARCHAR(100), -- 学科领域
    target_audience ENUM('UNDERGRADUATE', 'GRADUATE', 'TEACHER', 'PROFESSIONAL') DEFAULT 'UNDERGRADUATE',
    difficulty_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') DEFAULT 'INTERMEDIATE',
    ownership ENUM('UNIVERSITY', 'COMPANY', 'SHARED') NOT NULL,
    owner_id VARCHAR(36) NOT NULL,
    publisher_id VARCHAR(36) NOT NULL,
    file_url VARCHAR(500),
    file_size BIGINT, -- 文件大小(字节)
    file_type VARCHAR(50), -- 文件类型
    preview_url VARCHAR(500), -- 预览链接
    download_count INT DEFAULT 0,
    view_count INT DEFAULT 0,
    rating DECIMAL(3,2) DEFAULT 0, -- 评分
    rating_count INT DEFAULT 0,
    access_level ENUM('PUBLIC', 'UNIVERSITY', 'COMPANY', 'PRIVATE') DEFAULT 'UNIVERSITY',
    price DECIMAL(10,2) DEFAULT 0, -- 价格，0表示免费
    currency VARCHAR(10) DEFAULT 'CNY',
    tags JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (publisher_id) REFERENCES users(id)
);

-- 知识产权交易表
CREATE TABLE ip_transactions (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    ip_type ENUM('PATENT', 'COPYRIGHT', 'TRADEMARK', 'TRADE_SECRET', 'SOFTWARE_COPYRIGHT') NOT NULL,
    ip_number VARCHAR(100), -- 专利号、版权号等
    owner_type ENUM('UNIVERSITY', 'COMPANY', 'INDIVIDUAL') NOT NULL,
    owner_id VARCHAR(36) NOT NULL,
    publisher_id VARCHAR(36) NOT NULL,
    transaction_type ENUM('TRANSFER', 'LICENSE', 'COOPERATION') NOT NULL,
    price DECIMAL(15,2),
    currency VARCHAR(10) DEFAULT 'CNY',
    license_duration_years INT, -- 授权年限
    exclusive BOOLEAN DEFAULT FALSE, -- 是否独占
    application_date DATE, -- 申请日期
    grant_date DATE, -- 授权日期
    expiry_date DATE, -- 到期日期
    status ENUM('DRAFT', 'PUBLISHED', 'NEGOTIATING', 'COMPLETED', 'CANCELLED') DEFAULT 'DRAFT',
    contact_info TEXT,
    documents JSON, -- 相关文档
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (publisher_id) REFERENCES users(id)
);

-- ================================
-- 系统管理与审计
-- ================================

-- 权限表 (扩展现有Permission)
CREATE TABLE permissions (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(200),
    module VARCHAR(50) NOT NULL, -- 所属模块
    action VARCHAR(50) NOT NULL, -- 操作类型：VIEW, CREATE, UPDATE, DELETE, APPROVE等
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 角色权限关联表
CREATE TABLE role_permissions (
    id VARCHAR(36) PRIMARY KEY,
    role ENUM('SUPER_ADMIN', 'UNIVERSITY_ADMIN', 'ENTERPRISE_ADMIN', 'TEACHER', 'STUDENT', 'ENTERPRISE_MENTOR') NOT NULL,
    permission_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE KEY unique_role_permission (role, permission_id)
);

-- 用户自定义权限表 (动态权限分配)
CREATE TABLE user_permissions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    permission_id VARCHAR(36) NOT NULL,
    granted_by VARCHAR(36) NOT NULL,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    status ENUM('ACTIVE', 'EXPIRED', 'REVOKED') DEFAULT 'ACTIVE',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES users(id),
    UNIQUE KEY unique_user_permission (user_id, permission_id)
);

-- 系统操作日志表
CREATE TABLE system_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50), -- 操作的资源类型
    resource_id VARCHAR(36), -- 操作的资源ID
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_data JSON, -- 请求数据
    response_status INT, -- HTTP状态码
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_action (user_id, action),
    INDEX idx_created_at (created_at),
    INDEX idx_resource (resource_type, resource_id)
);

-- 消息通知表
CREATE TABLE notifications (
    id VARCHAR(36) PRIMARY KEY,
    recipient_id VARCHAR(36) NOT NULL,
    sender_id VARCHAR(36),
    title VARCHAR(200) NOT NULL,
    content TEXT,
    notification_type ENUM('SYSTEM', 'PROJECT', 'RESOURCE', 'COURSE', 'ACHIEVEMENT', 'MESSAGE') DEFAULT 'SYSTEM',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    status ENUM('UNREAD', 'READ', 'ARCHIVED') DEFAULT 'UNREAD',
    related_resource_type VARCHAR(50), -- 关联的资源类型
    related_resource_id VARCHAR(36), -- 关联的资源ID
    action_url VARCHAR(500), -- 操作链接
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_recipient_status (recipient_id, status),
    INDEX idx_created_at (created_at)
);

-- ================================
-- 初始化数据插入
-- ================================

-- 插入权限数据
INSERT INTO permissions (id, name, description, module, action) VALUES
-- 基础权限
(UUID(), 'DASHBOARD_VIEW', '查看仪表盘', 'dashboard', 'VIEW'),
(UUID(), 'PROFILE_MANAGE', '管理个人资料', 'profile', 'MANAGE'),

-- 用户管理权限
(UUID(), 'USER_VIEW', '查看用户', 'user', 'VIEW'),
(UUID(), 'USER_CREATE', '创建用户', 'user', 'CREATE'),
(UUID(), 'USER_UPDATE', '更新用户', 'user', 'UPDATE'),
(UUID(), 'USER_DELETE', '删除用户', 'user', 'DELETE'),
(UUID(), 'USER_MANAGE', '用户管理', 'user', 'MANAGE'),

-- 企业管理权限
(UUID(), 'COMPANY_VIEW', '查看企业', 'company', 'VIEW'),
(UUID(), 'COMPANY_CREATE', '创建企业', 'company', 'CREATE'),
(UUID(), 'COMPANY_UPDATE', '更新企业', 'company', 'UPDATE'),
(UUID(), 'COMPANY_DELETE', '删除企业', 'company', 'DELETE'),
(UUID(), 'COMPANY_MANAGE', '企业管理', 'company', 'MANAGE'),
(UUID(), 'COMPANY_APPROVE', '审批企业', 'company', 'APPROVE'),

-- 课程管理权限
(UUID(), 'COURSE_VIEW', '查看课程', 'course', 'VIEW'),
(UUID(), 'COURSE_CREATE', '创建课程', 'course', 'CREATE'),
(UUID(), 'COURSE_UPDATE', '更新课程', 'course', 'UPDATE'),
(UUID(), 'COURSE_DELETE', '删除课程', 'course', 'DELETE'),
(UUID(), 'COURSE_MANAGE', '课程管理', 'course', 'MANAGE'),

-- 学生管理权限
(UUID(), 'STUDENT_VIEW', '查看学生', 'student', 'VIEW'),
(UUID(), 'STUDENT_CREATE', '创建学生', 'student', 'CREATE'),
(UUID(), 'STUDENT_UPDATE', '更新学生', 'student', 'UPDATE'),
(UUID(), 'STUDENT_DELETE', '删除学生', 'student', 'DELETE'),
(UUID(), 'STUDENT_MANAGE', '学生管理', 'student', 'MANAGE'),

-- 项目管理权限
(UUID(), 'PROJECT_VIEW', '查看项目', 'project', 'VIEW'),
(UUID(), 'PROJECT_CREATE', '创建项目', 'project', 'CREATE'),
(UUID(), 'PROJECT_UPDATE', '更新项目', 'project', 'UPDATE'),
(UUID(), 'PROJECT_DELETE', '删除项目', 'project', 'DELETE'),
(UUID(), 'PROJECT_MANAGE', '项目管理', 'project', 'MANAGE'),
(UUID(), 'PROJECT_APPROVE', '审批项目', 'project', 'APPROVE'),

-- 实习管理权限
(UUID(), 'INTERNSHIP_VIEW', '查看实习', 'internship', 'VIEW'),
(UUID(), 'INTERNSHIP_CREATE', '创建实习', 'internship', 'CREATE'),
(UUID(), 'INTERNSHIP_UPDATE', '更新实习', 'internship', 'UPDATE'),
(UUID(), 'INTERNSHIP_DELETE', '删除实习', 'internship', 'DELETE'),
(UUID(), 'INTERNSHIP_MANAGE', '实习管理', 'internship', 'MANAGE'),
(UUID(), 'INTERNSHIP_APPLY', '申请实习', 'internship', 'APPLY'),

-- 资源管理权限
(UUID(), 'RESOURCE_VIEW', '查看资源', 'resource', 'VIEW'),
(UUID(), 'RESOURCE_CREATE', '创建资源', 'resource', 'CREATE'),
(UUID(), 'RESOURCE_UPDATE', '更新资源', 'resource', 'UPDATE'),
(UUID(), 'RESOURCE_DELETE', '删除资源', 'resource', 'DELETE'),
(UUID(), 'RESOURCE_MANAGE', '资源管理', 'resource', 'MANAGE'),
(UUID(), 'RESOURCE_BORROW', '借用资源', 'resource', 'BORROW'),

-- 成果管理权限
(UUID(), 'ACHIEVEMENT_VIEW', '查看成果', 'achievement', 'VIEW'),
(UUID(), 'ACHIEVEMENT_CREATE', '创建成果', 'achievement', 'CREATE'),
(UUID(), 'ACHIEVEMENT_UPDATE', '更新成果', 'achievement', 'UPDATE'),
(UUID(), 'ACHIEVEMENT_DELETE', '删除成果', 'achievement', 'DELETE'),
(UUID(), 'ACHIEVEMENT_MANAGE', '成果管理', 'achievement', 'MANAGE'),
(UUID(), 'ACHIEVEMENT_VERIFY', '验证成果', 'achievement', 'VERIFY');

-- 注意：实际部署时需要根据具体数据库系统调整语法
-- MySQL 8.0+ 支持 UUID() 函数
-- 其他数据库可能需要使用不同的UUID生成方式
