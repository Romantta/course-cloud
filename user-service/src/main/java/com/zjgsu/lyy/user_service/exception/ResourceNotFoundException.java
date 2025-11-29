package com.zjgsu.lyy.user_service.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, String id) {
        super(resource + " 不存在: " + id);
    }
}