package com.zjgsu.lyy.enrollment.repository;

import com.zjgsu.lyy.enrollment.model.Enrollment;
import com.zjgsu.lyy.enrollment.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

    List<Enrollment> findByCourseId(String courseId);

    List<Enrollment> findByStudentId(String studentId);

    List<Enrollment> findByCourseIdAndStatus(String courseId, EnrollmentStatus status);

    List<Enrollment> findByStudentIdAndStatus(String studentId, EnrollmentStatus status);

    Optional<Enrollment> findByCourseIdAndStudentId(String courseId, String studentId);

    boolean existsByCourseIdAndStudentIdAndStatus(String courseId, String studentId, EnrollmentStatus status);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.courseId = :courseId AND e.status = 'ACTIVE'")
    long countActiveEnrollmentsByCourseId(@Param("courseId") String courseId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.studentId = :studentId AND e.status = 'ACTIVE'")
    long countActiveEnrollmentsByStudentId(@Param("studentId") String studentId);
}