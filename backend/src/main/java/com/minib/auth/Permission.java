package com.minib.auth;

public enum Permission {
    // 基础权限
    DASHBOARD_VIEW("查看仪表盘"),
    PROFILE_MANAGE("管理个人资料"),
    
    // 用户管理权限
    USER_VIEW("查看用户"),
    USER_CREATE("创建用户"),
    USER_UPDATE("更新用户"),
    USER_DELETE("删除用户"),
    USER_MANAGE("用户管理"),
    
    // 企业管理权限
    COMPANY_VIEW("查看企业"),
    COMPANY_CREATE("创建企业"),
    COMPANY_UPDATE("更新企业"),
    COMPANY_DELETE("删除企业"),
    COMPANY_MANAGE("企业管理"),
    COMPANY_APPROVE("审批企业"),
    
    // 课程管理权限
    COURSE_VIEW("查看课程"),
    COURSE_CREATE("创建课程"),
    COURSE_UPDATE("更新课程"),
    COURSE_DELETE("删除课程"),
    COURSE_MANAGE("课程管理"),
    
    // 学生管理权限
    STUDENT_VIEW("查看学生"),
    STUDENT_CREATE("创建学生"),
    STUDENT_UPDATE("更新学生"),
    STUDENT_DELETE("删除学生"),
    STUDENT_MANAGE("学生管理"),
    
    // 教师管理权限
    TEACHER_VIEW("查看教师"),
    TEACHER_CREATE("创建教师"),
    TEACHER_UPDATE("更新教师"),
    TEACHER_DELETE("删除教师"),
    TEACHER_MANAGE("教师管理"),
    
    // 项目管理权限
    PROJECT_VIEW("查看项目"),
    PROJECT_CREATE("创建项目"),
    PROJECT_UPDATE("更新项目"),
    PROJECT_DELETE("删除项目"),
    PROJECT_MANAGE("项目管理"),
    PROJECT_APPROVE("审批项目"),
    
    // 实习管理权限
    INTERNSHIP_VIEW("查看实习"),
    INTERNSHIP_CREATE("创建实习"),
    INTERNSHIP_UPDATE("更新实习"),
    INTERNSHIP_DELETE("删除实习"),
    INTERNSHIP_MANAGE("实习管理"),
    INTERNSHIP_APPLY("申请实习"),
    
    // 资源管理权限
    RESOURCE_VIEW("查看资源"),
    RESOURCE_CREATE("创建资源"),
    RESOURCE_UPDATE("更新资源"),
    RESOURCE_DELETE("删除资源"),
    RESOURCE_MANAGE("资源管理"),
    RESOURCE_BORROW("借用资源"),
    
    // 成果管理权限
    ACHIEVEMENT_VIEW("查看成果"),
    ACHIEVEMENT_CREATE("创建成果"),
    ACHIEVEMENT_UPDATE("更新成果"),
    ACHIEVEMENT_DELETE("删除成果"),
    ACHIEVEMENT_MANAGE("成果管理"),
    ACHIEVEMENT_VERIFY("验证成果"),
    
    // 导师管理权限
    MENTOR_VIEW("查看导师"),
    MENTOR_CREATE("创建导师"),
    MENTOR_UPDATE("更新导师"),
    MENTOR_DELETE("删除导师"),
    MENTOR_MANAGE("导师管理");

    private final String displayName;
    
    Permission(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}




