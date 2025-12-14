package com.zjgsu.lyy.user_service.controller;

import com.zjgsu.lyy.user_service.model.ApiResponse;
import com.zjgsu.lyy.user_service.model.Student;
import com.zjgsu.lyy.user_service.service.StudentService;
import com.zjgsu.lyy.user_service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            // 1. 查找用户（这里用email作为用户名，你也可以用学号）
            var studentOpt = studentService.getStudentByStudentId(request.getUsername());

            if (studentOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "用户名或密码错误"));
            }

            Student student = studentOpt.get();

            // 2. 验证密码（注意：实际项目中密码应该是加密存储的）
            // 这里简化处理，实际应该使用BCrypt等加密方式
            if (!"defaultPassword".equals(request.getPassword())) { // 临时密码
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "用户名或密码错误"));
            }

            // 3. 生成JWT Token
            String token = jwtUtil.generateToken(
                    student.getId(),
                    student.getName(),
                    "STUDENT" // 角色
            );

            // 4. 返回响应
            UserInfo userInfo = new UserInfo(
                    student.getId(),
                    student.getStudentId(),
                    student.getName(),
                    student.getEmail(),
                    "STUDENT"
            );

            LoginResponse response = new LoginResponse(token, userInfo);
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "登录失败: " + e.getMessage()));
        }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        // 无参构造函数
        public LoginRequest() {
        }

        // Getter 和 Setter 方法
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class LoginResponse {
        private String token;
        private UserInfo user;

        // 无参构造函数
        public LoginResponse() {
        }

        public LoginResponse(String token, UserInfo user) {
            this.token = token;
            this.user = user;
        }

        // Getter 和 Setter 方法
        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public UserInfo getUser() {
            return user;
        }

        public void setUser(UserInfo user) {
            this.user = user;
        }
    }

    public static class UserInfo {
        private String id;
        private String studentId;
        private String name;
        private String email;
        private String role;

        // 无参构造函数
        public UserInfo() {
        }

        public UserInfo(String id, String studentId, String name, String email, String role) {
            this.id = id;
            this.studentId = studentId;
            this.name = name;
            this.email = email;
            this.role = role;
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

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}