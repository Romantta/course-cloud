package com.zjgsu.lyy.enrollment.client;

import com.zjgsu.lyy.enrollment.dto.StudentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserClientFallback implements UserClient {

    @Override
    public String ping() {
        log.warn("UserClient ping fallback triggered");
        return "user-service fallback instance";
    }

    @Override
    public StudentDto getStudent(String studentId) {
        log.warn("UserClient fallback triggered for student: {}", studentId);
        throw new RuntimeException("用户服务暂时不可用，请稍后再试");
    }
}