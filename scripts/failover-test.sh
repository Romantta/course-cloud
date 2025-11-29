#!/bin/bash
echo "=== 故障转移测试 ==="

echo "1. 当前健康的catalog-service实例:"
curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service" | jq '.hosts[] | select(.healthy == true) | {ip: .ip, port: .port}'

echo "2. 停止一个catalog-service实例..."
docker stop catalog-service-2

echo "3. 等待Nacos检测实例下线（20秒）..."
sleep 20

echo "4. 检查Nacos实例状态（应该有一个不健康）..."
curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service" | jq '.hosts[] | {ip: .ip, port: .port, healthy: .healthy}'

echo "5. 测试故障转移 - 发送5个请求（应该路由到健康实例）..."
for i in {1..5}; do
    response=$(curl -s http://localhost:8082/api/enrollments/test-load-balancer)
    echo "请求 $i: $response"
    # 检查响应是否包含错误信息
    if echo "$response" | grep -q "ERROR"; then
        echo "⚠️  请求失败！"
    else
        echo "✅ 请求成功"
    fi
done

echo "6. 恢复停止的实例..."
docker start catalog-service-2
sleep 10

echo "7. 验证实例恢复..."
curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service" | jq '.hosts[] | {ip: .ip, port: .port, healthy: .healthy}'

echo "=== 故障转移测试完成 ==="