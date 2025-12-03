#!/bin/bash

echo "=== Nacos 服务发现测试 ==="

# 启动所有服务
echo "启动所有服务..."
docker-compose up -d --build

echo "等待服务启动..."
sleep 60

# 检查 Nacos 控制台
echo -e "\n1. 检查 Nacos 控制台..."
echo "Nacos 控制台地址: http://localhost:8848/nacos"
echo "默认账号: nacos"
echo "默认密码: nacos"

# 检查服务注册情况
echo -e "\n2. 检查服务注册情况..."
echo "检查 user-service 注册:"
curl -s -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=user-service" | jq .

echo -e "\n检查 catalog-service 注册:"
curl -s -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service" | jq .

echo -e "\n检查 enrollment-service 注册:"
curl -s -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=enrollment-service" | jq .

# 测试基础功能
echo -e "\n3. 测试基础功能..."

echo -e "\n创建学生:"
curl -X POST http://localhost:8081/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": "2024001",
    "name": "张三",
    "major": "计算机科学与技术",
    "grade": 2024,
    "email": "zhangsan@example.edu.cn"
  }'

echo -e "\n\n创建课程:"
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

echo -e "\n\n测试选课（服务间调用）:"
COURSE_ID=$(curl -s http://localhost:8082/api/courses | grep -o '"id":"[^"]*' | head -1 | cut -d'"' -f4)
curl -X POST http://localhost:8083/api/enrollments \
  -H "Content-Type: application/json" \
  -d "{
    \"courseId\": \"$COURSE_ID\",
    \"studentId\": \"2024001\"
  }"

echo -e "\n\n4. 负载均衡测试 - 启动多个 user-service 实例"
echo "启动第二个 user-service 实例..."
docker-compose up -d --scale user-service=2

echo "等待实例启动..."
sleep 30

echo -e "\n检查多实例注册:"
curl -s -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=user-service" | jq .

echo -e "\n5. 多次调用测试负载均衡:"
for i in {1..10}; do
  echo "第 $i 次请求 user-service:"
  curl -s http://localhost:8081/api/students | grep -o '"name":"[^"]*' | head -1
done

echo -e "\n6. 故障转移测试"
echo "停止一个 user-service 实例..."
docker stop user-service_2

echo "等待 Nacos 检测故障..."
sleep 20

echo -e "\n检查服务状态:"
curl -s -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=user-service" | jq .

echo -e "\n测试服务是否仍然可用:"
curl -s http://localhost:8081/api/students

echo -e "\n7. 查看容器状态:"
docker-compose ps

echo -e "\n=== 测试完成 ==="