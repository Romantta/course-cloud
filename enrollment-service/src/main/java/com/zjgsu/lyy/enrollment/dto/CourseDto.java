package com.zjgsu.lyy.enrollment.dto;

import java.util.Objects;

public class CourseDto {
    private String id;
    private String code;
    private String title;
    private Integer capacity;
    private Integer enrolled;

    // 无参构造函数
    public CourseDto() {
    }

    // 有参构造函数（可选）
    public CourseDto(String id, String code, String title, Integer capacity, Integer enrolled) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.capacity = capacity;
        this.enrolled = enrolled;
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(Integer enrolled) {
        this.enrolled = enrolled;
    }

    // toString 方法
    @Override
    public String toString() {
        return "CourseDto{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", capacity=" + capacity +
                ", enrolled=" + enrolled +
                '}';
    }

    // equals 和 hashCode 方法（可选，但推荐实现）
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseDto courseDto = (CourseDto) o;
        return Objects.equals(id, courseDto.id) &&
                Objects.equals(code, courseDto.code) &&
                Objects.equals(title, courseDto.title) &&
                Objects.equals(capacity, courseDto.capacity) &&
                Objects.equals(enrolled, courseDto.enrolled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, title, capacity, enrolled);
    }
}