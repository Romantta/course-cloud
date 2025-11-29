package com.zjgsu.lyy.catalog.service;

import com.zjgsu.lyy.catalog.model.Course;
import com.zjgsu.lyy.catalog.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Optional<Course> getCourseById(String id) {
        return courseRepository.findById(id);
    }

    public Course createCourse(Course course) {
        validateCourseFields(course);

        if (courseRepository.existsByCode(course.getCode())) {
            throw new RuntimeException("课程代码已存在: " + course.getCode());
        }

        return courseRepository.save(course);
    }

    public Course updateCourse(String id, Course course) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在: " + id));

        validateCourseFields(course);

        if (!existingCourse.getCode().equals(course.getCode()) &&
                courseRepository.existsByCodeAndIdNot(course.getCode(), id)) {
            throw new RuntimeException("课程代码已存在: " + course.getCode());
        }

        course.setId(id);
        course.setEnrolled(existingCourse.getEnrolled());
        course.setCreatedAt(existingCourse.getCreatedAt());
        return courseRepository.save(course);
    }

    public void deleteCourse(String id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在: " + id));
        courseRepository.delete(course);
    }

    public void incrementEnrolledCount(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在: " + courseId));
        course.setEnrolled(course.getEnrolled() + 1);
        courseRepository.save(course);
    }

    public void decrementEnrolledCount(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在: " + courseId));
        course.setEnrolled(Math.max(0, course.getEnrolled() - 1));
        courseRepository.save(course);
    }

    public List<Course> getCoursesWithAvailableSeats() {
        return courseRepository.findCoursesWithAvailableSeats();
    }

    private void validateCourseFields(Course course) {
        if (course.getCode() == null || course.getCode().trim().isEmpty()) {
            throw new RuntimeException("课程代码不能为空");
        }
        if (course.getTitle() == null || course.getTitle().trim().isEmpty()) {
            throw new RuntimeException("课程名称不能为空");
        }
        if (course.getInstructor() == null) {
            throw new RuntimeException("教师信息不能为空");
        }
        if (course.getInstructor().getId() == null || course.getInstructor().getId().trim().isEmpty()) {
            throw new RuntimeException("教师ID不能为空");
        }
        if (course.getInstructor().getName() == null || course.getInstructor().getName().trim().isEmpty()) {
            throw new RuntimeException("教师姓名不能为空");
        }
        if (course.getInstructor().getEmail() == null || course.getInstructor().getEmail().trim().isEmpty()) {
            throw new RuntimeException("教师邮箱不能为空");
        }
        if (!isValidEmail(course.getInstructor().getEmail())) {
            throw new RuntimeException("教师邮箱格式不正确: " + course.getInstructor().getEmail());
        }
        if (course.getSchedule() == null) {
            throw new RuntimeException("课程时间安排不能为空");
        }
        if (course.getSchedule().getDayOfWeek() == null || course.getSchedule().getDayOfWeek().trim().isEmpty()) {
            throw new RuntimeException("上课日期不能为空");
        }
        if (course.getSchedule().getStartTime() == null || course.getSchedule().getStartTime().trim().isEmpty()) {
            throw new RuntimeException("开始时间不能为空");
        }
        if (course.getSchedule().getEndTime() == null || course.getSchedule().getEndTime().trim().isEmpty()) {
            throw new RuntimeException("结束时间不能为空");
        }
        if (!isValidTimeFormat(course.getSchedule().getStartTime())) {
            throw new RuntimeException("开始时间格式不正确，应为 HH:mm 格式: " + course.getSchedule().getStartTime());
        }
        if (!isValidTimeFormat(course.getSchedule().getEndTime())) {
            throw new RuntimeException("结束时间格式不正确，应为 HH:mm 格式: " + course.getSchedule().getEndTime());
        }
        if (course.getCapacity() == null) {
            throw new RuntimeException("课程容量不能为空");
        }
        if (course.getCapacity() <= 0) {
            throw new RuntimeException("课程容量必须大于0");
        }
        if (course.getSchedule().getExpectedAttendance() == null) {
            throw new RuntimeException("预期出勤人数不能为空");
        }
        if (course.getSchedule().getExpectedAttendance() < 0) {
            throw new RuntimeException("预期出勤人数不能为负数");
        }
        if (course.getSchedule().getExpectedAttendance() > course.getCapacity()) {
            throw new RuntimeException("预期出勤人数不能超过课程容量");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(emailRegex);
    }

    private boolean isValidTimeFormat(String time) {
        String timeRegex = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";
        return time != null && time.matches(timeRegex);
    }
}