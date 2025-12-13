package com.zjgsu.lyy.enrollment.dto;

import lombok.Data;

@Data
public class StudentDto {
    private String id;
    private String studentId;
    private String name;
    private String major;
    private Integer grade;
    private String email;
}