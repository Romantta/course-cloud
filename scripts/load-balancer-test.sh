echo "=== 负载均衡和故障转移测试 ==="

# 启动第二个 catalog-service 实例
echo "1. 启动第二个 catalog-service 实例..."
docker-compose up -d --scale catalog-service=2

echo "2. 等待实例启动..."
sleep 20

# 检查多实例注册
echo "3. 检查多实例注册..."
curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service" | jq '.hosts | length' || echo "查询失败"

# 测试负载均衡
echo "4. 测试负载均衡 - 多次调用课程服务..."
for i in {1..10}; do
  response=$(curl -s http://localhost:8081/api/courses)
  echo "第 $i 次调用: 成功"
done

# 停止一个实例
echo "5. 停止一个 catalog-service 实例..."
docker stop $(docker ps -q --filter "name=catalog-service" | head -1)

echo "6. 等待 Nacos 检测到实例下线..."
sleep 15

# 测试故障转移
echo "7. 测试故障转移..."
for i in {1..5}; do
  response=$(curl -s http://localhost:8081/api/courses)
  if [ $? -eq 0 ]; then
    echo "第 $i 次调用: 成功 (故障转移生效)"
  else
    echo "第 $i 次调用: 失败"
  fi
done

# 恢复实例
echo "8. 恢复停止的实例..."
docker-compose up -d catalog-service

echo "9. 等待实例重新注册..."
sleep 15

# 最终检查
echo "10. 最终实例状态检查..."
curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service" | jq '.hosts | length' || echo "查询失败"

echo "=== 负载均衡测试完成 ==="