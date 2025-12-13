package com.zjgsu.lyy.enrollment.service;

import com.zjgsu.lyy.enrollment.client.CatalogClient;
import com.zjgsu.lyy.enrollment.client.UserClient;
import com.zjgsu.lyy.enrollment.dto.CourseDto;
import com.zjgsu.lyy.enrollment.dto.StudentDto;
import com.zjgsu.lyy.enrollment.model.Enrollment;
import com.zjgsu.lyy.enrollment.model.EnrollmentStatus;
import com.zjgsu.lyy.enrollment.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private CatalogClient catalogClient;

    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    public Enrollment enrollStudent(String courseId, String studentId) {
        // 1. 使用Feign客户端调用用户服务验证学生是否存在
        try {
            StudentDto studentDto = userClient.getStudent(studentId);
            if (studentDto == null) {
                throw new RuntimeException("学生不存在: " + studentId);
            }
        } catch (RuntimeException e) {
            // 降级处理已经在Fallback中完成
            throw new RuntimeException("验证学生信息失败: " + e.getMessage());
        }

        // 2. 使用Feign客户端调用课程目录服务验证课程是否存在
        CourseDto courseDto;
        try {
            courseDto = catalogClient.getCourse(courseId);
            if (courseDto == null) {
                throw new RuntimeException("课程不存在: " + courseId);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("验证课程信息失败: " + e.getMessage());
        }

        // 3. 检查课程容量
        if (courseDto.getEnrolled() >= courseDto.getCapacity()) {
            throw new RuntimeException("课程容量已满");
        }

        // 4. 检查重复选课
        if (enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(courseId, studentId, EnrollmentStatus.ACTIVE)) {
            throw new RuntimeException("学生已选过该课程");
        }

        // 5. 创建选课记录
        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // 6. 更新课程的已选人数（使用Feign客户端）
        try {
            Map<String, Integer> updateData = Map.of("enrolled", courseDto.getEnrolled() + 1);
            catalogClient.updateEnrollmentCount(courseId, updateData);
        } catch (RuntimeException e) {
            // 记录日志但不影响主流程
            System.err.println("更新课程选课人数失败: " + e.getMessage());
        }

        return savedEnrollment;
    }

    public void cancelEnrollment(String enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("选课记录不存在: " + enrollmentId));

        String courseId = enrollment.getCourseId();

        // 软删除：将状态改为DROPPED
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);

        // 获取当前选课人数并更新
        try {
            CourseDto courseDto = catalogClient.getCourse(courseId);
            if (courseDto != null && courseDto.getEnrolled() > 0) {
                Map<String, Integer> updateData = Map.of("enrolled", courseDto.getEnrolled() - 1);
                catalogClient.updateEnrollmentCount(courseId, updateData);
            }
        } catch (RuntimeException e) {
            System.err.println("获取课程选课人数失败: " + e.getMessage());
        }
    }

    public List<Enrollment> getEnrollmentsByCourseId(String courseId) {
        // 验证课程存在
        try {
            CourseDto courseDto = catalogClient.getCourse(courseId);
            if (courseDto == null) {
                throw new RuntimeException("课程不存在: " + courseId);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("验证课程失败: " + e.getMessage());
        }

        return enrollmentRepository.findByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);
    }

    public List<Enrollment> getEnrollmentsByStudentId(String studentId) {
        // 验证学生存在
        try {
            StudentDto studentDto = userClient.getStudent(studentId);
            if (studentDto == null) {
                throw new RuntimeException("学生不存在: " + studentId);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("验证学生失败: " + e.getMessage());
        }

        return enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE);
    }
}