#!/bin/bash
echo "=== 业务功能测试（服务间调用验证）==="

echo "1. 获取课程列表..."
COURSES=$(curl -s http://localhost:8081/api/courses)
echo "课程列表: $COURSES"

# 提取第一个课程的ID
COURSE_ID=$(echo "$COURSES" | grep -o '"id":"[^"]*' | head -1 | cut -d'"' -f4)
echo "使用课程ID: $COURSE_ID"

echo "2. 获取学生列表..."
STUDENTS=$(curl -s http://localhost:8082/api/students)
echo "学生列表: $STUDENTS"

# 提取第一个学生的ID
STUDENT_ID=$(echo "$STUDENTS" | grep -o '"studentId":"[^"]*' | head -1 | cut -d'"' -f4)
echo "使用学生ID: $STUDENT_ID"

echo "3. 测试选课（服务间调用）..."
if [ -n "$COURSE_ID" ] && [ -n "$STUDENT_ID" ]; then
    ENROLL_RESPONSE=$(curl -s -X POST http://localhost:8082/api/enrollments \
      -H "Content-Type: application/json" \
      -d "{
        \"courseId\": \"$COURSE_ID\",
        \"studentId\": \"$STUDENT_ID\"
      }")
    echo "选课响应: $ENROLL_RESPONSE"
else
    echo "⚠️  无法获取课程ID或学生ID，跳过选课测试"
fi

echo "4. 验证选课记录..."
ENROLLMENTS=$(curl -s http://localhost:8082/api/enrollments)
echo "选课记录: $ENROLLMENTS"

echo "5. 测试课程不存在的情况..."
ERROR_RESPONSE=$(curl -s -X POST http://localhost:8082/api/enrollments \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": "non-existent-course-id",
    "studentId": "2024999"
  }')
echo "错误处理响应: $ERROR_RESPONSE"

echo "=== 业务功能测试完成 ==="