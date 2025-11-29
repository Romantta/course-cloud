package com.zjgsu.lyy.catalog.repository;

import com.zjgsu.lyy.catalog.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    Optional<Course> findByCode(String code);

    List<Course> findByTitleContainingIgnoreCase(String title);

    List<Course> findByInstructorId(String instructorId);

    @Query("SELECT c FROM Course c WHERE c.enrolled < c.capacity")
    List<Course> findCoursesWithAvailableSeats();

    @Query("SELECT COUNT(c) > 0 FROM Course c WHERE c.code = :code AND c.id != :id")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") String id);

    boolean existsByCode(String code);
}