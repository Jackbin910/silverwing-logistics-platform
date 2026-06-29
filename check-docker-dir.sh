#!/bin/bash

echo "=========================================="
echo "银翼智驭 - 目录结构检查"
echo "=========================================="
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "当前目录: $(pwd)"
echo ""

echo "检查 docker/openresty 目录结构："
echo "--------------------------------"

if [ ! -d "docker" ]; then
    echo "❌ docker/ 目录不存在"
    exit 1
fi

if [ ! -d "docker/openresty" ]; then
    echo "❌ docker/openresty/ 目录不存在"
    exit 1
fi

if [ ! -d "docker/openresty/conf" ]; then
    echo "❌ docker/openresty/conf/ 目录不存在"
    exit 1
fi

if [ ! -d "docker/openresty/conf/conf.d" ]; then
    echo "❌ docker/openresty/conf/conf.d/ 目录不存在"
    exit 1
fi

if [ ! -f "docker/openresty/conf/nginx.conf" ]; then
    echo "❌ docker/openresty/conf/nginx.conf 文件不存在"
    echo "   可能 nginx.conf 被创建成了目录"
    ls -la docker/openresty/conf/
    exit 1
fi

echo "✅ docker/ 目录存在"
echo "✅ docker/openresty/ 目录存在"
echo "✅ docker/openresty/conf/ 目录存在"
echo "✅ docker/openresty/conf/conf.d/ 目录存在"
echo "✅ docker/openresty/conf/nginx.conf 文件存在"
echo ""

echo "文件详情："
echo "--------------------------------"
ls -lh docker/openresty/conf/nginx.conf
echo ""

echo "完整目录树："
echo "--------------------------------"
tree docker/openresty 2>/dev/null || find docker/openresty -type f -o -type d
echo ""

echo "=========================================="
echo "✅ 检查完成！"
echo "=========================================="
