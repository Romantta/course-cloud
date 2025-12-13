package com.zjgsu.lyy.enrollment.client;

import com.zjgsu.lyy.enrollment.dto.CourseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "catalog-service",
        fallback = CatalogClientFallback.class
)
public interface CatalogClient {
    @GetMapping("/api/courses/{id}")
    CourseDto getCourse(@PathVariable("id") String id);

    @PutMapping("/api/courses/{id}/enrollment")
    void updateEnrollmentCount(@PathVariable("id") String id, @RequestBody Map<String, Integer> request);

    @GetMapping("/api/courses/ping")
    String ping();
}