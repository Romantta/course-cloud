package com.zjgsu.lyy.enrollment.service;

import com.zjgsu.lyy.enrollment.model.Enrollment;
import com.zjgsu.lyy.enrollment.model.EnrollmentStatus;
import com.zjgsu.lyy.enrollment.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private RestTemplate restTemplate;

    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    public Enrollment enrollStudent(String courseId, String studentId) {
        // 1. 使用服务名调用用户服务验证学生是否存在
        String userUrl = "http://user-service/api/students/studentId/" + studentId;
        Map<String, Object> studentResponse;
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(userUrl, Map.class);
            studentResponse = response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("学生不存在: " + studentId);
        } catch (Exception e) {
            throw new RuntimeException("调用用户服务失败: " + e.getMessage());
        }

        // 2. 使用服务名调用课程目录服务验证课程是否存在
        String catalogUrl = "http://catalog-service/api/courses/" + courseId;
        Map<String, Object> courseResponse;
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(catalogUrl, Map.class);
            courseResponse = response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("课程不存在: " + courseId);
        } catch (Exception e) {
            throw new RuntimeException("调用课程服务失败: " + e.getMessage());
        }

        // 3. 从响应中提取课程信息
        if (courseResponse == null || !courseResponse.containsKey("data")) {
            throw new RuntimeException("课程服务响应格式错误");
        }

        Map<String, Object> courseData = (Map<String, Object>) courseResponse.get("data");
        Integer capacity = (Integer) courseData.get("capacity");
        Integer enrolled = (Integer) courseData.get("enrolled");

        // 4. 检查课程容量
        if (enrolled >= capacity) {
            throw new RuntimeException("课程容量已满");
        }

        // 5. 检查重复选课
        if (enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(courseId, studentId, EnrollmentStatus.ACTIVE)) {
            throw new RuntimeException("学生已选过该课程");
        }

        // 6. 创建选课记录
        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // 7. 更新课程的已选人数（调用catalog-service）
        updateCourseEnrolledCount(courseId, enrolled + 1);

        return savedEnrollment;
    }

    private void updateCourseEnrolledCount(String courseId, int newCount) {
        String url = "http://catalog-service/api/courses/" + courseId + "/enrollment";
        Map<String, Object> updateData = Map.of("enrolled", newCount);
        try {
            restTemplate.put(url, updateData);
        } catch (Exception e) {
            // 记录日志但不影响主流程
            System.err.println("更新课程选课人数失败: " + e.getMessage());
        }
    }

    public void cancelEnrollment(String enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("选课记录不存在: " + enrollmentId));

        String courseId = enrollment.getCourseId();

        // 软删除：将状态改为DROPPED
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);

        // 更新课程选课人数
        updateCourseEnrolledCount(courseId, getCurrentEnrolledCount(courseId) - 1);
    }

    private int getCurrentEnrolledCount(String courseId) {
        String url = "http://catalog-service/api/courses/" + courseId;
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> courseResponse = response.getBody();
            if (courseResponse != null && courseResponse.containsKey("data")) {
                Map<String, Object> courseData = (Map<String, Object>) courseResponse.get("data");
                return (Integer) courseData.get("enrolled");
            }
        } catch (Exception e) {
            System.err.println("获取课程选课人数失败: " + e.getMessage());
        }
        return 0;
    }

    public List<Enrollment> getEnrollmentsByCourseId(String courseId) {
        // 验证课程存在
        String url = "http://catalog-service/api/courses/" + courseId;
        try {
            restTemplate.getForEntity(url, Map.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("课程不存在: " + courseId);
        }

        return enrollmentRepository.findByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);
    }

    public List<Enrollment> getEnrollmentsByStudentId(String studentId) {
        // 验证学生存在
        String userUrl = "http://user-service/api/students/studentId/" + studentId;
        try {
            restTemplate.getForEntity(userUrl, Map.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("学生不存在: " + studentId);
        }

        return enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE);
    }
}