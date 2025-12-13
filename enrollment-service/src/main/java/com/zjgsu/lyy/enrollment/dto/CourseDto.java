package com.zjgsu.lyy.enrollment.dto;

import lombok.Data;

@Data
public class CourseDto {
    private String id;
    private String code;
    private String title;
    private Integer capacity;
    private Integer enrolled;
}