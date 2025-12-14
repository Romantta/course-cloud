package com.zjgsu.lyy.enrollment.dto;

import java.util.Objects;

public class StudentDto {
    private String id;
    private String studentId;
    private String name;
    private String major;
    private Integer grade;
    private String email;

    // 无参构造函数
    public StudentDto() {
    }

    // 有参构造函数（可选）
    public StudentDto(String id, String studentId, String name, String major, Integer grade, String email) {
        this.id = id;
        this.studentId = studentId;
        this.name = name;
        this.major = major;
        this.grade = grade;
        this.email = email;
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // toString 方法
    @Override
    public String toString() {
        return "StudentDto{" +
                "id='" + id + '\'' +
                ", studentId='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", major='" + major + '\'' +
                ", grade=" + grade +
                ", email='" + email + '\'' +
                '}';
    }

    // equals 和 hashCode 方法（可选，但推荐实现）
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentDto that = (StudentDto) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(major, that.major) &&
                Objects.equals(grade, that.grade) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, studentId, name, major, grade, email);
    }
}