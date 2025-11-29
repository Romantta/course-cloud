echo "=== Nacos 服务发现测试脚本 ==="

# 启动所有服务
echo "1. 启动所有服务..."
docker-compose up -d --build

echo "2. 等待服务启动..."
sleep 30

# 检查 Nacos 控制台
echo "3. 检查 Nacos 控制台..."
curl -f http://localhost:8848/nacos/ || echo "Nacos 控制台访问失败"

# 检查服务注册情况
echo "4. 检查服务注册情况..."
echo "catalog-service 实例列表:"
curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service" | jq . || echo "查询失败"

echo "enrollment-service 实例列表:"
curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=enrollment-service" | jq . || echo "查询失败"

# 测试服务功能
echo "5. 测试课程目录服务..."
curl -X POST http://localhost:8081/api/courses \
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
  }' || echo "创建课程失败"

echo "6. 获取所有课程:"
curl -s http://localhost:8081/api/courses | jq . || echo "获取课程失败"

# 测试学生创建
echo "7. 测试学生创建..."
curl -X POST http://localhost:8082/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": "2024001",
    "name": "张三",
    "major": "计算机科学与技术",
    "grade": 2024,
    "email": "zhangsan@example.edu.cn"
  }' || echo "创建学生失败"

# 测试服务间调用（选课）
echo "8. 测试服务间调用 - 学生选课..."
COURSE_ID=$(curl -s http://localhost:8081/api/courses | jq -r '.data[0].id')
echo "获取到课程ID: $COURSE_ID"

curl -X POST http://localhost:8082/api/enrollments \
  -H "Content-Type: application/json" \
  -d "{
    \"courseId\": \"$COURSE_ID\",
    \"studentId\": \"2024001\"
  }" || echo "选课失败"

echo "9. 查询选课记录:"
curl -s http://localhost:8082/api/enrollments | jq . || echo "查询选课记录失败"

# 测试负载均衡（模拟多次调用）
echo "10. 测试负载均衡效果..."
for i in {1..5}; do
  echo "第 $i 次调用课程服务:"
  curl -s http://localhost:8081/api/courses | jq -r '.data[0].title' || echo "调用失败"
done

echo "11. 查看容器状态..."
docker-compose ps

echo "=== 测试完成 ==="
echo "Nacos 控制台: http://localhost:8848/nacos (账号: nacos, 密码: nacos)"
echo "课程服务: http://localhost:8081/api/courses"
echo "选课服务: http://localhost:8082/api/students"