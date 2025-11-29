package com.zjgsu.lyy.user_service.repository;

import com.zjgsu.lyy.user_service.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    Optional<Student> findByStudentId(String studentId);

    Optional<Student> findByEmail(String email);

    List<Student> findByMajor(String major);

    List<Student> findByGrade(Integer grade);

    boolean existsByStudentId(String studentId);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.email = :email AND s.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") String id);

    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.studentId = :studentId AND s.id != :id")
    boolean existsByStudentIdAndIdNot(@Param("studentId") String studentId, @Param("id") String id);
}