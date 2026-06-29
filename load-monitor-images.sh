#!/bin/bash

# 银翼智驭 - 监控栈镜像加载脚本
# 用途：加载监控栈镜像到本地 Docker
# 注意：此脚本会在 silverwing-monitor-images/ 目录中自动生成
#       如果需要手动使用，请确保在包含镜像 .tar 文件的目录中运行

echo "=========================================="
echo "银翼智驭 - 监控栈镜像加载"
echo "=========================================="

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 加载所有镜像
total=$(ls -1 *.tar 2>/dev/null | wc -l)
current=0

if [ "$total" -eq 0 ]; then
    echo "❌ 错误：未找到 .tar 镜像文件"
    echo "请确保在包含镜像文件的目录中运行此脚本"
    exit 1
fi

echo "开始加载监控栈镜像..."
for file in *.tar; do
    if [ -f "$file" ]; then
        current=$((current + 1))
        echo "[${current}/${total}] 加载：$file"
        docker load < "$file"
    fi
done

echo ""
echo "=========================================="
echo "✅ 监控栈镜像加载完成！"
echo "=========================================="
echo ""
echo "验证镜像："
docker images | grep -E "prometheus|grafana"
