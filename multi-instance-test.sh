#!/bin/bash

echo "=== 多实例负载均衡测试 ==="

# 启动多个服务实例
echo "启动 3 个 user-service 实例..."
docker-compose up -d --scale user-service=3

echo "启动 2 个 catalog-service 实例..."
docker-compose up -d --scale catalog-service=2

echo "等待实例启动..."
sleep 45

echo -e "\n1. 检查服务注册情况:"
echo "user-service 实例:"
curl -s -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=user-service" | jq '.hosts | length'

echo "catalog-service 实例:"
curl -s -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service" | jq '.hosts | length'

echo -e "\n2. 负载均衡测试 - user-service:"
for i in {1..15}; do
  response=$(curl -s http://localhost:8081/actuator/env | grep -o '"local.server.port":[^,]*' | head -1)
  echo "第 $i 次请求 -> $response"
done

echo -e "\n3. 负载均衡测试 - catalog-service:"
for i in {1..10}; do
  response=$(curl -s http://localhost:8082/api/courses | grep -o '"title":"[^"]*' | head -1)
  echo "第 $i 次请求 -> 课程: $response"
done

echo -e "\n4. 选课服务调用测试:"
for i in {1..5}; do
  echo "第 $i 次选课查询:"
  curl -s http://localhost:8083/api/enrollments | grep -o '"studentId":"[^"]*' | head -1
done

echo -e "\n=== 多实例测试完成 ==="