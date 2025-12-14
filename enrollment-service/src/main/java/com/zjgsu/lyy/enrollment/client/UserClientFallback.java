package com.zjgsu.lyy.enrollment.client;

import com.zjgsu.lyy.enrollment.dto.StudentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClientFallback.class);

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