package com.zjgsu.lyy.enrollment.client;

import com.zjgsu.lyy.enrollment.dto.StudentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        fallback = UserClientFallback.class
)
public interface UserClient {

    @GetMapping("/api/students/ping")
    String ping();

    @GetMapping("/api/students/studentId/{studentId}")
    StudentDto getStudent(@PathVariable("studentId") String studentId);
}