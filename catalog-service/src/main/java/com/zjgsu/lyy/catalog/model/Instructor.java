package com.zjgsu.lyy.catalog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Instructor {
    @Column(name = "instructor_id", nullable = false, length = 50)
    private String id;

    @Column(name = "instructor_name", nullable = false, length = 100)
    private String name;

    @Column(name = "instructor_email", nullable = false, length = 100)
    private String email;

    // 构造方法
    public Instructor() {
    }

    public Instructor(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getter和Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}