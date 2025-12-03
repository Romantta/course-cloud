package com.zjgsu.lyy.catalog.Controller;

import com.zjgsu.lyy.catalog.model.ApiResponse;
import com.zjgsu.lyy.catalog.model.Course;
import com.zjgsu.lyy.catalog.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @Autowired
    private Environment env;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Course>>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> getCourseById(@PathVariable String id) {
        return courseService.getCourseById(id)
                .map(course -> ResponseEntity.ok(ApiResponse.success(course)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "课程不存在")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Course>> createCourse(@RequestBody Course course) {
        try {
            Course createdCourse = courseService.createCourse(course);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("✅课程创建成功", createdCourse));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> updateCourse(@PathVariable String id, @RequestBody Course course) {
        try {
            Course updatedCourse = courseService.updateCourse(id, course);
            return ResponseEntity.ok(ApiResponse.success("✅课程更新成功", updatedCourse));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable String id) {
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.ok(ApiResponse.success("✅课程删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @PutMapping("/{id}/enrollment")
    public ResponseEntity<ApiResponse<Course>> updateEnrollmentCount(@PathVariable String id, @RequestBody Map<String, Integer> request) {
        try {
            Integer enrolled = request.get("enrolled");
            if (enrolled == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "enrolled字段不能为空"));
            }

            Course course = courseService.getCourseById(id)
                    .orElseThrow(() -> new RuntimeException("课程不存在: " + id));

            course.setEnrolled(enrolled);
            Course updatedCourse = courseService.updateCourse(id, course);
            return ResponseEntity.ok(ApiResponse.success("选课人数更新成功", updatedCourse));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }
    //负载均衡
    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        String port = env.getProperty("local.server.port");
        String message = "catalog-service is running on port: " + port;
        return ResponseEntity.ok(ApiResponse.success(message, "Port: " + port));
    }
}