package com.minib.resources;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.minib.resources.ResourceDtos.*;
import static com.minib.resources.ResourceEnums.*;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {
    private static final Map<String, ResearchResource> RESEARCH = new ConcurrentHashMap<>();
    private static final Map<String, CourseResource> COURSES = new ConcurrentHashMap<>();

    static {
        // Seed research resources
        seedResearch("R-1001", ResearchResourceType.LAB_EQUIPMENT, "高精度示波器", "100MHz 4通道", Ownership.SCHOOL, "uadmin", BorrowStatus.AVAILABLE, null, "理科楼A-302");
        seedResearch("R-1002", ResearchResourceType.RESEARCH_DATA, "材料疲劳数据集", "包含 10w 条实验记录", Ownership.ENTERPRISE, "eadmin", BorrowStatus.BORROWED, "student", null);
        seedResearch("R-1003", ResearchResourceType.TECHNICAL_DOC, "5G 协议实现白皮书", "企业内部技术文档", Ownership.ENTERPRISE, "mentor", BorrowStatus.AVAILABLE, null, null);
        seedResearch("R-1004", ResearchResourceType.LAB_EQUIPMENT, "三坐标测量机", "精度 2μm", Ownership.SCHOOL, "teacher", BorrowStatus.AVAILABLE, null, "机电中心-一楼");
        seedResearch("R-1005", ResearchResourceType.RESEARCH_DATA, "交通流量原始数据", "含 30 天摄像头采集数据", Ownership.SCHOOL, "uadmin", BorrowStatus.AVAILABLE, null, null);
        seedResearch("R-1006", ResearchResourceType.TECHNICAL_DOC, "工业相机标定手册", "含标定流程与误差分析", Ownership.ENTERPRISE, "eadmin", BorrowStatus.BORROWED, "teacher", null);
        seedResearch("R-1007", ResearchResourceType.LAB_EQUIPMENT, "3D 打印机", "支持 ABS/PLA", Ownership.ENTERPRISE, "eadmin", BorrowStatus.AVAILABLE, null, "创新工场-创客区");
        seedResearch("R-1008", ResearchResourceType.LAB_EQUIPMENT, "无人车底盘", "四驱室内测试平台", Ownership.SCHOOL, "teacher", BorrowStatus.BORROWED, "student", "实验楼B-105");
        seedResearch("R-1009", ResearchResourceType.RESEARCH_DATA, "声纹识别数据集", "2w 人样本，脱敏", Ownership.ENTERPRISE, "mentor", BorrowStatus.AVAILABLE, null, null);
        seedResearch("R-1010", ResearchResourceType.TECHNICAL_DOC, "边缘计算网关接入规范", "v2.1", Ownership.ENTERPRISE, "mentor", BorrowStatus.AVAILABLE, null, null);

        // Seed course resources
        seedCourse("C-2001", CourseResourceType.QUALITY_COURSE, "计算机网络精品课", "含实验指导与测验", Ownership.SCHOOL, "teacher");
        seedCourse("C-2002", CourseResourceType.TEACHING_CASE, "产学合作案例：智能制造", "项目制教学案例", Ownership.ENTERPRISE, "eadmin");
        seedCourse("C-2003", CourseResourceType.COURSEWARE, "数据结构课件PPT", "覆盖链表/树/图", Ownership.SCHOOL, "teacher");
        seedCourse("C-2004", CourseResourceType.OTHER, "开源许可证指南", "课堂参考资料", Ownership.SCHOOL, "teacher");
        seedCourse("C-2005", CourseResourceType.QUALITY_COURSE, "操作系统实践课", "含进程/线程实验", Ownership.SCHOOL, "teacher");
        seedCourse("C-2006", CourseResourceType.TEACHING_CASE, "企业数字化转型案例", "含 ROI 与实施路径", Ownership.ENTERPRISE, "eadmin");
        seedCourse("C-2007", CourseResourceType.COURSEWARE, "机器学习课件", "监督/非监督/评估", Ownership.SCHOOL, "teacher");
        seedCourse("C-2008", CourseResourceType.OTHER, "Latex 排版模板", "论文与报告模板", Ownership.SCHOOL, "student");
        seedCourse("C-2009", CourseResourceType.QUALITY_COURSE, "云原生与容器化", "K8s 基础与实践", Ownership.ENTERPRISE, "mentor");
        seedCourse("C-2010", CourseResourceType.TEACHING_CASE, "车间产线调度优化", "数学建模案例", Ownership.ENTERPRISE, "mentor");
    }

    private static void seedResearch(String id, ResearchResourceType type, String name, String description, Ownership ownership, String publisher, BorrowStatus status, String borrower, String location) {
        ResearchResource r = new ResearchResource();
        r.id = id; r.type = type; r.name = name; r.description = description; r.ownership = ownership; r.publisher = publisher; r.status = status; r.borrower = borrower; r.location = location;
        RESEARCH.put(id, r);
    }
    private static void seedCourse(String id, CourseResourceType type, String name, String description, Ownership ownership, String publisher) {
        CourseResource c = new CourseResource();
        c.id = id; c.type = type; c.name = name; c.description = description; c.ownership = ownership; c.publisher = publisher;
        COURSES.put(id, c);
    }

    @GetMapping("/research")
    public ResponseEntity<PageResult<ResearchResource>> listResearch(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "type", required = false) ResearchResourceType type,
            @RequestParam(name = "ownership", required = false) Ownership ownership,
            @RequestParam(name = "status", required = false) BorrowStatus status,
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        List<ResearchResource> all = new ArrayList<>(RESEARCH.values());
        List<ResearchResource> filtered = all.stream().filter(r ->
                (type == null || r.type == type) &&
                (ownership == null || r.ownership == ownership) &&
                (status == null || r.status == status) &&
                (keyword == null || keyword.isBlank() || (r.name + " " + Optional.ofNullable(r.description).orElse("")).toLowerCase().contains(keyword.toLowerCase()))
        ).sorted(Comparator.comparing(r -> r.id)).collect(Collectors.toList());
        int from = Math.max(0, (page - 1) * pageSize);
        int to = Math.min(filtered.size(), from + pageSize);
        PageResult<ResearchResource> pr = new PageResult<>();
        pr.total = filtered.size();
        pr.items = from < to ? filtered.subList(from, to) : List.of();
        return ResponseEntity.ok(pr);
    }

    @PostMapping("/research/borrow")
    public ResponseEntity<ResearchResource> borrow(@Validated @RequestBody BorrowRequest req) {
        ResearchResource r = RESEARCH.get(req.id);
        if (r == null) return ResponseEntity.notFound().build();
        if (r.status == BorrowStatus.BORROWED) return ResponseEntity.badRequest().build();
        r.status = BorrowStatus.BORROWED;
        r.borrower = req.borrower;
        return ResponseEntity.ok(r);
    }

    @PostMapping("/research/return")
    public ResponseEntity<ResearchResource> returnBack(@Validated @RequestBody ReturnRequest req) {
        ResearchResource r = RESEARCH.get(req.id);
        if (r == null) return ResponseEntity.notFound().build();
        r.status = BorrowStatus.AVAILABLE;
        r.borrower = null;
        return ResponseEntity.ok(r);
    }

    @GetMapping("/courses")
    public ResponseEntity<PageResult<CourseResource>> listCourses(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "type", required = false) CourseResourceType type,
            @RequestParam(name = "ownership", required = false) Ownership ownership,
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        List<CourseResource> all = new ArrayList<>(COURSES.values());
        List<CourseResource> filtered = all.stream().filter(c ->
                (type == null || c.type == type) &&
                (ownership == null || c.ownership == ownership) &&
                (keyword == null || keyword.isBlank() || (c.name + " " + Optional.ofNullable(c.description).orElse("")).toLowerCase().contains(keyword.toLowerCase()))
        ).sorted(Comparator.comparing(c -> c.id)).collect(Collectors.toList());
        int from = Math.max(0, (page - 1) * pageSize);
        int to = Math.min(filtered.size(), from + pageSize);
        PageResult<CourseResource> pr = new PageResult<>();
        pr.total = filtered.size();
        pr.items = from < to ? filtered.subList(from, to) : List.of();
        return ResponseEntity.ok(pr);
    }
}


