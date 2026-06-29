#!/bin/bash
# Docker 镜像预拉取脚本 - Linux Bash
# 用于快速拉取所有基础设施镜像，加速部署流程

echo ""
echo "========================================"
echo "  SilverWing Docker 镜像预拉取工具"
echo "========================================"
echo ""

# 定义需要拉取的镜像列表（与 onepanel-docker-compose.yml 保持一致）
declare -a IMAGES=(
    "docker.1ms.run/library/mysql:8.0.40|MySQL 数据库"
    "docker.1ms.run/library/redis:7.0.15|Redis 缓存"
    "docker.1ms.run/nacos/nacos-server:v2.4.3|Nacos 服务注册中心"
    "docker.1ms.run/library/rabbitmq:3.13.7-management|RabbitMQ 消息队列"
    "docker.1ms.run/xuxueli/xxl-job-admin:2.4.2|XXL-JOB 分布式任务调度"
    "docker.1ms.run/library/nginx:1.26-alpine|Nginx 反向代理"
)

TOTAL_IMAGES=${#IMAGES[@]}
SUCCESS_COUNT=0
FAIL_COUNT=0

echo "📦 计划拉取 $TOTAL_IMAGES 个基础设施镜像..."
echo ""

# 检查 Docker 是否运行
if ! docker ps >/dev/null 2>&1; then
    echo "❌ 错误：Docker 未运行或无法连接"
    echo "请启动 Docker 后重试"
    exit 1
fi

echo "✅ Docker 连接正常"
echo ""

# 逐个拉取镜像
for image_info in "${IMAGES[@]}"; do
    IFS='|' read -r image desc <<< "$image_info"

    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "⏳ 正在拉取：$desc"
    echo "   镜像：$image"
    echo ""

    # 执行拉取并计时
    START_TIME=$(date +%s.%N)
    docker pull "$image"
    END_TIME=$(date +%s.%N)

    ELAPSED_TIME=$(echo "$END_TIME - $START_TIME" | bc 2>/dev/null || echo "0")
    ELAPSED_FORMATTED=$(printf "%.1f" $ELAPSED_TIME)

    if [ $? -eq 0 ]; then
        echo "✅ 成功 [$ELAPSED_FORMATTED 秒]"
        ((SUCCESS_COUNT++))
    else
        echo "❌ 失败"
        ((FAIL_COUNT++))
    fi

    echo ""
done

# 统计结果
echo ""
echo "========================================"
echo "  拉取完成统计"
echo "========================================"
echo ""
echo "总镜像数：$TOTAL_IMAGES"
echo "成功：$SUCCESS_COUNT"
echo "失败：$FAIL_COUNT"
echo ""

if [ $FAIL_COUNT -eq 0 ]; then
    echo "🎉 所有镜像拉取成功！可以开始部署了"
else
    echo "⚠️  部分镜像拉取失败，请检查网络连接"
    echo "提示：确认 docker.1ms.run 镜像加速源可达"
fi

echo ""
echo "下一步操作："
echo "1. 确认环境变量：cp onepanel.env.example onepanel.env && vi onepanel.env"
echo "2. 启动基础设施：docker compose -f onepanel-infra-compose.yml up -d"
echo "3. 启动微服务：docker compose -f onepanel-services-compose.yml up -d"
echo "（或一键部署：docker compose -f onepanel-docker-compose.yml up -d）"
echo ""
