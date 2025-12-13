package com.zjgsu.lyy.enrollment.controller;

import com.zjgsu.lyy.enrollment.model.ApiResponse;
import com.zjgsu.lyy.enrollment.model.Enrollment;
import com.zjgsu.lyy.enrollment.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import com.zjgsu.lyy.enrollment.client.UserClient;
import com.zjgsu.lyy.enrollment.client.CatalogClient;

@RestController
@RequestMapping("/api/enrollments")
@Slf4j
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    // 用于进行服务间调用（必须由@LoadBalanced标注的RestTemplate）
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserClient userClient;

    @Autowired
    private CatalogClient catalogClient;

    // 用于获取本服务端口
    @Autowired
    private Environment env;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Enrollment>>> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentService.getAllEnrollments();
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Enrollment>> enrollStudent(@RequestBody Map<String, String> request) {
        String courseId = request.get("courseId");
        String studentId = request.get("studentId");

        if (courseId == null || studentId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "courseId 和 studentId 不能为空"));
        }

        try {
            Enrollment enrollment = enrollmentService.enrollStudent(courseId, studentId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("✅选课成功", enrollment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelEnrollment(@PathVariable String id) {
        try {
            enrollmentService.cancelEnrollment(id);
            return ResponseEntity.ok(ApiResponse.success("选课记录删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<Enrollment>>> getEnrollmentsByCourseId(@PathVariable String courseId) {
        try {
            List<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourseId(courseId);
            return ResponseEntity.ok(ApiResponse.success(enrollments));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<Enrollment>>> getEnrollmentsByStudentId(@PathVariable String studentId) {
        try {
            List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudentId(studentId);
            return ResponseEntity.ok(ApiResponse.success(enrollments));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    //负载均衡
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<Map<String, String>>> testLoadBalancer(HttpServletRequest request) {
        Map<String, String> results = new HashMap<>();

        // 获取enrollment-service自身信息
        String selfPort = env.getProperty("local.server.port");
        String selfHostname = System.getenv("HOSTNAME");
        if (selfHostname == null) {
            selfHostname = request.getLocalName();
        }
        results.put("enrollmentService", "Instance: " + selfHostname + ", Port: " + selfPort);

        // 调用 user-service
        try {
            String userResponse = userClient.ping();
            results.put("userService", userResponse);
        } catch (Exception e) {
            results.put("userService", "调用失败: " + e.getMessage());
        }

        // 调用 catalog-service
        try {
            String catalogResponse = catalogClient.ping();
            results.put("catalogService", catalogResponse);
        } catch (Exception e) {
            results.put("catalogService", "调用失败: " + e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success("负载均衡测试结果", results));
    }
}