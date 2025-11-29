# 🏫 校园选课系统（微服务版）

## 📖 项目简介

- **项目名称**：course-cloud
- **版本号**：v1.0.0
- **基于版本**：基于 hw04b 的单体选课系统拆分
- **项目阶段**：微服务架构（初次拆分）
- **项目描述**：将单体选课系统拆分为三个独立的微服务，实现服务间通信和容器化部署

## 🏗️ 架构说明

### 微服务拆分架构

```
客户端
    |
    ├── user-service (8081) ──────────> user_db (3306)
    │       └── 学生/用户管理
    │
    ├── catalog-service (8082) ────────> catalog_db (3307)
    │       ├── 课程管理
    │       ├── 教师信息管理
    │       └── 课程时间安排
    │
    └── enrollment-service (8083) ─────> enrollment_db (3308)
            ├── 选课管理
            └── HTTP调用 ──────────────┐
                                      ├── user-service（验证学生）
                                      └── catalog-service（验证课程）
```

### 服务职责划分

| 服务 | 端口 | 数据库 | 主要功能 |
|------|------|--------|----------|
| user-service | 8081 | user_db | 学生信息管理、用户认证 |
| catalog-service | 8082 | catalog_db | 课程信息管理、教师信息、课程时间安排 |
| enrollment-service | 8083 | enrollment_db | 选课管理、服务间通信、业务逻辑处理 |

## 🛠️ 技术栈

- **后端框架**：Spring Boot 3.2.0
- **编程语言**：Java 17
- **数据持久化**：Spring Data JPA + Hibernate
- **数据库**：MySQL 8.0
- **容器化**：Docker + Docker Compose
- **服务通信**：RestTemplate（HTTP）
- **构建工具**：Maven 3.8+
- **服务发现**：Spring Cloud（预留扩展）

## ⚙️ 环境要求

### 开发环境
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Git

### 生产环境
- Docker 20.10+
- Docker Compose 2.0+
- 内存：至少 2GB
- 存储：至少 10GB

## 🚀 构建和运行步骤

### 方式一：Docker 容器化部署（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/你的用户名/course-cloud.git
cd course-cloud

# 2. 切换到发布版本
git checkout v1.0.0

# 3. 启动所有服务
docker-compose up -d --build

# 4. 检查服务状态
docker-compose ps

# 5. 等待服务完全启动（约60秒）
sleep 60

# 6. 运行测试
chmod +x test-services.sh
./test-services.sh
```

### 方式二：本地开发环境运行

```bash
# 1. 启动用户服务
cd user-service
mvn spring-boot:run

# 2. 新终端启动课程目录服务
cd catalog-service
mvn spring-boot:run

# 3. 新终端启动选课服务
cd enrollment-service
mvn spring-boot:run

# 4. 测试服务
curl http://localhost:8081/api/students
curl http://localhost:8082/api/courses
curl http://localhost:8083/api/enrollments
```

### 常用 Docker 命令

```bash
# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f user-service
docker-compose logs -f catalog-service
docker-compose logs -f enrollment-service

# 停止服务
docker-compose down

# 停止服务并删除数据卷
docker-compose down -v

# 重启服务
docker-compose restart

# 查看服务资源使用
docker stats
```

## 📡 API 文档

### 用户服务 (user-service:8081)

#### 学生管理
- `GET /api/students` - 获取所有学生
- `GET /api/students/{id}` - 获取单个学生
- `GET /api/students/studentId/{studentId}` - 按学号查询学生
- `POST /api/students` - 创建学生
- `PUT /api/students/{id}` - 更新学生
- `DELETE /api/students/{id}` - 删除学生

#### 示例请求

**创建学生：**
```bash
curl -X POST http://localhost:8081/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": "2024001",
    "name": "张三",
    "major": "计算机科学与技术",
    "grade": 2024,
    "email": "zhangsan@example.edu.cn"
  }'
```

### 课程目录服务 (catalog-service:8082)

#### 课程管理
- `GET /api/courses` - 获取所有课程
- `GET /api/courses/{id}` - 获取单个课程
- `GET /api/courses/code/{code}` - 按课程代码查询
- `POST /api/courses` - 创建课程
- `PUT /api/courses/{id}` - 更新课程
- `DELETE /api/courses/{id}` - 删除课程
- `PUT /api/courses/{id}/enrollment` - 更新选课人数

#### 示例请求

**创建课程：**
```bash
curl -X POST http://localhost:8082/api/courses \
  -H "Content-Type: application/json" \
  -d '{
    "code": "CS101",
    "title": "计算机科学导论",
    "instructor": {
      "id": "T001",
      "name": "张教授",
      "email": "zhang@example.edu.cn"
    },
    "schedule": {
      "dayOfWeek": "MONDAY",
      "startTime": "08:00",
      "endTime": "10:00",
      "expectedAttendance": 50
    },
    "capacity": 60,
    "enrolled": 0
  }'
```

### 选课服务 (enrollment-service:8083)

#### 选课管理
- `GET /api/enrollments` - 获取所有选课记录
- `POST /api/enrollments` - 学生选课
- `DELETE /api/enrollments/{id}` - 学生退课
- `GET /api/enrollments/course/{courseId}` - 按课程查询选课
- `GET /api/enrollments/student/{studentId}` - 按学生查询选课

#### 示例请求

**学生选课：**
```bash
curl -X POST http://localhost:8083/api/enrollments \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": "课程ID",
    "studentId": "2024001"
  }'
```

## 🧪 测试说明

### 自动化测试
项目提供了完整的测试脚本：

```bash
# 运行完整测试
./test-services.sh

# 或者运行详细测试
./complete-test.sh
```

### 测试覆盖范围
1. ✅ 学生创建、查询、更新、删除
2. ✅ 课程创建、查询、更新、删除
3. ✅ 选课功能（验证服务间通信）
4. ✅ 重复选课错误处理
5. ✅ 课程容量已满错误处理
6. ✅ 学生/课程不存在错误处理
7. ✅ 数据格式验证

### 手动测试验证

```bash
# 验证用户服务
curl http://localhost:8081/api/students

# 验证课程服务
curl http://localhost:8082/api/courses

# 验证选课服务
curl http://localhost:8083/api/enrollments

# 验证服务健康状态
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

### Postman 测试集合
项目提供了完整的 Postman 测试集合，包含 20+ 个测试用例，涵盖所有 API 接口和错误场景。


## 📊 项目结构

```
course-cloud/
├── README.md
├── docker-compose.yml
├── test-services.sh
├── complete-test.sh
├── .gitignore
├── user-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/zjgsu/lyy/user/
│           │   ├── model/           # 实体类
│           │   ├── repository/      # 数据访问层
│           │   ├── service/         # 业务逻辑层
│           │   ├── controller/      # 控制层
│           │   └── exception/       # 异常处理
│           └── resources/
│               ├── application.yml       # 开发环境配置
│               └── application-docker.yml # Docker环境配置
├── catalog-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/zjgsu/lyy/catalog/
│           │   ├── model/           # 实体类
│           │   ├── repository/      # 数据访问层
│           │   ├── service/         # 业务逻辑层
│           │   ├── controller/      # 控制层
│           │   └── exception/       # 异常处理
│           └── resources/
│               ├── application.yml       # 开发环境配置
│               └── application-docker.yml # Docker环境配置
└── enrollment-service/
    ├── Dockerfile
    ├── pom.xml
    └── src/
        └── main/
            ├── java/com/zjgsu/lyy/enrollment/
            │   ├── model/           # 实体类
            │   ├── repository/      # 数据访问层
            │   ├── service/         # 业务逻辑层
            │   ├── controller/      # 控制层
            │   └── exception/       # 异常处理
            └── resources/
                ├── application.yml       # 开发环境配置
                └── application-docker.yml # Docker环境配置
```

## 🎯 功能特性

### 核心功能
- ✅ 完整的学生 CRUD 操作（user-service）
- ✅ 完整的课程 CRUD 操作（catalog-service）
- ✅ 选课管理（包含容量检查和重复选课检查）
- ✅ 服务间 HTTP 通信验证学生和课程存在性
- ✅ 课程人数自动更新

### 业务验证
- ✅ 邮箱格式验证
- ✅ 时间格式验证
- ✅ 课程容量验证
- ✅ 重复选课验证
- ✅ 数据完整性验证
- ✅ 学生唯一性验证

### 技术特性
- ✅ 统一的 API 响应格式
- ✅ 全局异常处理
- ✅ 容器化部署
- ✅ 健康检查
- ✅ 服务发现准备
- ✅ 多环境配置支持
- ✅ 自动化测试脚本

