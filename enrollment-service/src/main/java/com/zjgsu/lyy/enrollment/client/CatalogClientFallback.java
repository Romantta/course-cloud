package com.zjgsu.lyy.enrollment.client;

import com.zjgsu.lyy.enrollment.dto.CourseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CatalogClientFallback implements CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogClientFallback.class);

    @Override
    public String ping() {
        log.warn("CatalogClient ping fallback triggered");
        return "catalog-service fallback instance";
    }

    @Override
    public CourseDto getCourse(String id) {
        log.warn("CatalogClient fallback triggered for course: {}", id);
        throw new RuntimeException("课程服务暂时不可用，请稍后再试");
    }

    @Override
    public void updateEnrollmentCount(String id, Map<String, Integer> request) {
        log.warn("CatalogClient fallback triggered for updating enrollment count for course: {}", id);
    }
}