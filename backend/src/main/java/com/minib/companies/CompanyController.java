package com.minib.companies;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.minib.companies.CompanyDtos.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private static final Map<String, Company> COMPANIES = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> COMPANY_MENTORS = new ConcurrentHashMap<>();

    static {
        // Seed demo companies
        seedCompany("阿里巴巴集团", "电商与云计算", "互联网", "LARGE", "https://www.alibaba.com", "contact@alibaba.com", "400-800-1688", "浙江省杭州市余杭区文一西路969号", "张先生", "合作总监");
        seedCompany("腾讯科技", "社交与游戏", "互联网", "LARGE", "https://www.tencent.com", "contact@tencent.com", "0755-86013388", "广东省深圳市南山区科技园", "李女士", "技术经理");
        seedCompany("百度科技", "搜索引擎与AI", "互联网", "LARGE", "https://www.baidu.com", "contact@baidu.com", "400-800-8888", "北京市海淀区上地十街10号", "王先生", "产品总监");
        seedCompany("字节跳动", "内容与推荐", "互联网", "LARGE", "https://www.bytedance.com", "contact@bytedance.com", "400-123-4567", "北京市海淀区知春路63号", "赵女士", "研发经理");
        seedCompany("华为技术", "通信与智能终端", "通信设备", "LARGE", "https://www.huawei.com", "contact@huawei.com", "400-822-9999", "广东省深圳市龙岗区坂田华为基地", "刘先生", "技术专家");
        seedCompany("小米科技", "智能硬件与IoT", "消费电子", "LARGE", "https://www.mi.com", "contact@mi.com", "400-100-5678", "北京市海淀区清河中街68号", "陈女士", "产品经理");
        seedCompany("美团", "本地生活服务", "互联网", "LARGE", "https://www.meituan.com", "contact@meituan.com", "400-050-7777", "北京市朝阳区望京东路6号", "杨先生", "技术总监");
        seedCompany("滴滴出行", "出行与物流", "互联网", "LARGE", "https://www.didiglobal.com", "contact@didiglobal.com", "400-000-0999", "北京市海淀区中关村软件园", "孙女士", "算法专家");
        seedCompany("京东集团", "电商与物流", "互联网", "LARGE", "https://www.jd.com", "contact@jd.com", "400-606-5500", "北京市朝阳区北辰世纪中心A座", "周先生", "技术负责人");
        seedCompany("网易科技", "游戏与教育", "互联网", "MEDIUM", "https://www.163.com", "contact@163.com", "020-85105163", "广东省广州市天河区科韵路16号", "吴女士", "研发总监");
    }

    private static void seedCompany(String name, String description, String industry, String scale, 
                                   String website, String email, String phone, String address, 
                                   String contactPerson, String contactTitle) {
        Company company = new Company();
        company.id = UUID.randomUUID().toString();
        company.name = name;
        company.description = description;
        company.industry = industry;
        company.scale = scale;
        company.website = website;
        company.email = email;
        company.phone = phone;
        company.address = address;
        company.contactPerson = contactPerson;
        company.contactTitle = contactTitle;
        company.status = "APPROVED";
        company.createdAt = LocalDateTime.now().minusDays((int)(Math.random() * 365));
        company.updatedAt = LocalDateTime.now().minusDays((int)(Math.random() * 30));
        
        // Random mentors
        List<String> mentors = new ArrayList<>();
        if (Math.random() > 0.3) mentors.add("mentor");
        if (Math.random() > 0.5) mentors.add("mentor2");
        company.mentors = mentors;
        company.mentorCount = mentors.size();
        
        // Random projects
        List<String> projects = new ArrayList<>();
        if (Math.random() > 0.2) projects.add("项目-" + name.substring(0, 2));
        if (Math.random() > 0.4) projects.add("合作-" + name.substring(0, 2));
        company.projects = projects;
        company.projectCount = projects.size();
        
        COMPANIES.put(company.id, company);
        COMPANY_MENTORS.put(company.id, new ArrayList<>(mentors));
    }

    @GetMapping
    public ResponseEntity<PageResult<Company>> listCompanies(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "industry", required = false) String industry,
            @RequestParam(name = "scale", required = false) String scale,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        List<Company> all = new ArrayList<>(COMPANIES.values());
        List<Company> filtered = all.stream().filter(c ->
                (industry == null || industry.isEmpty() || Objects.equals(c.industry, industry)) &&
                (scale == null || scale.isEmpty() || Objects.equals(c.scale, scale)) &&
                (status == null || status.isEmpty() || Objects.equals(c.status, status)) &&
                (keyword == null || keyword.isEmpty() || 
                 (c.name + " " + Objects.toString(c.description, "") + " " + Objects.toString(c.contactPerson, "")).toLowerCase().contains(keyword.toLowerCase()))
        ).sorted(Comparator.comparing(c -> c.name)).collect(Collectors.toList());
        
        int from = Math.max(0, (page - 1) * pageSize);
        int to = Math.min(filtered.size(), from + pageSize);
        PageResult<Company> result = new PageResult<>();
        result.total = filtered.size();
        result.items = from < to ? filtered.subList(from, to) : List.of();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompany(@PathVariable String id) {
        Company company = COMPANIES.get(id);
        return company != null ? ResponseEntity.ok(company) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Company> createCompany(@Validated @RequestBody CreateCompanyRequest req) {
        Company company = new Company();
        company.id = UUID.randomUUID().toString();
        company.name = req.name;
        company.description = req.description;
        company.industry = req.industry;
        company.scale = req.scale;
        company.website = req.website;
        company.email = req.email;
        company.phone = req.phone;
        company.address = req.address;
        company.contactPerson = req.contactPerson;
        company.contactTitle = req.contactTitle;
        company.status = "PENDING";
        company.createdAt = LocalDateTime.now();
        company.updatedAt = LocalDateTime.now();
        company.mentors = new ArrayList<>();
        company.mentorCount = 0;
        company.projects = new ArrayList<>();
        company.projectCount = 0;

        COMPANIES.put(company.id, company);
        COMPANY_MENTORS.put(company.id, new ArrayList<>());
        return ResponseEntity.ok(company);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Company> updateCompany(@PathVariable String id, @Validated @RequestBody UpdateCompanyRequest req) {
        Company company = COMPANIES.get(id);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }

        if (req.name != null) company.name = req.name;
        if (req.description != null) company.description = req.description;
        if (req.industry != null) company.industry = req.industry;
        if (req.scale != null) company.scale = req.scale;
        if (req.website != null) company.website = req.website;
        if (req.email != null) company.email = req.email;
        if (req.phone != null) company.phone = req.phone;
        if (req.address != null) company.address = req.address;
        if (req.contactPerson != null) company.contactPerson = req.contactPerson;
        if (req.contactTitle != null) company.contactTitle = req.contactTitle;
        if (req.status != null) company.status = req.status;

        company.updatedAt = LocalDateTime.now();

        return ResponseEntity.ok(company);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable String id) {
        Company removed = COMPANIES.remove(id);
        COMPANY_MENTORS.remove(id);
        return removed != null ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/mentors")
    public ResponseEntity<Void> assignMentor(@PathVariable String id, @Validated @RequestBody AssignMentorRequest req) {
        Company company = COMPANIES.get(id);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }

        List<String> mentors = COMPANY_MENTORS.getOrDefault(id, new ArrayList<>());
        if (!mentors.contains(req.mentorId)) {
            mentors.add(req.mentorId);
            company.mentors = new ArrayList<>(mentors);
            company.mentorCount = mentors.size();
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/mentors")
    public ResponseEntity<Void> removeMentor(@PathVariable String id, @Validated @RequestBody RemoveMentorRequest req) {
        Company company = COMPANIES.get(id);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }

        List<String> mentors = COMPANY_MENTORS.getOrDefault(id, new ArrayList<>());
        mentors.remove(req.mentorId);
        company.mentors = new ArrayList<>(mentors);
        company.mentorCount = mentors.size();

        return ResponseEntity.ok().build();
    }

    @GetMapping("/industries")
    public ResponseEntity<List<String>> getIndustries() {
        List<String> industries = COMPANIES.values().stream()
                .map(c -> c.industry)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return ResponseEntity.ok(industries);
    }

    @GetMapping("/scales")
    public ResponseEntity<List<String>> getScales() {
        return ResponseEntity.ok(List.of("SMALL", "MEDIUM", "LARGE"));
    }
}

