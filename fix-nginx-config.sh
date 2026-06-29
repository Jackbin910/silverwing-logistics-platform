#!/bin/bash

echo "=========================================="
echo "银翼智驭 - Nginx 配置问题诊断与修复"
echo "=========================================="
echo ""

# 检查并修复 docker/openresty 目录结构

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "当前目录: $(pwd)"
echo ""

# 检查 nginx.conf 是否为目录
if [ -d "docker/openresty/conf/nginx.conf" ]; then
    echo "❌ 发现问题：docker/openresty/conf/nginx.conf 是目录而不是文件"
    echo ""
    echo "正在修复..."
    rm -rf "docker/openresty/conf/nginx.conf"
    cat > "docker/openresty/conf/nginx.conf" << 'NGINXCONF'
user  nginx;
worker_processes  auto;

error_log  /var/log/nginx/error.log  notice;
pid        /var/run/nginx.pid;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;

    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';

    access_log  /var/log/nginx/access.log  main;

    sendfile        on;
    tcp_nopush      on;
    tcp_nodelay     on;
    keepalive_timeout  65;
    types_hash_max_size 2048;

    # Gzip Settings
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml text/javascript 
               application/json application/javascript application/xml+rss;

    # Include server configurations
    include /etc/nginx/conf.d/*.conf;
}
NGINXCONF
    echo "✅ 已修复：重新创建了 nginx.conf 文件"
else
    echo "✅ docker/openresty/conf/nginx.conf 状态正常"
fi

# 确保 conf.d 目录存在且包含配置文件
if [ ! -d "docker/openresty/conf/conf.d" ]; then
    echo "创建 docker/openresty/conf/conf.d 目录..."
    mkdir -p "docker/openresty/conf/conf.d"
fi

if [ ! -f "docker/openresty/conf/conf.d/default.conf" ]; then
    echo "创建 docker/openresty/conf/conf.d/default.conf..."
    cat > "docker/openresty/conf/conf.d/default.conf" << 'DEFAULTCONF'
upstream gateway {
    server gateway:8080;
}

upstream admin_web {
    server admin-web:8081;
}

server {
    listen       80;
    server_name  localhost;

    # Health check endpoint
    location /health {
        access_log off;
        return 200 "OK\n";
        add_header Content-Type text/plain;
    }

    # Gateway API
    location /api/ {
        proxy_pass http://gateway/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Admin Web
    location / {
        proxy_pass http://admin_web/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
DEFAULTCONF
    echo "✅ 已创建 default.conf"
else
    echo "✅ docker/openresty/conf/conf.d/default.conf 存在"
fi

# 确保 logs 目录存在
if [ ! -d "docker/openresty/logs" ]; then
    echo "创建 docker/openresty/logs 目录..."
    mkdir -p "docker/openresty/logs"
fi

# 确保 cert 目录存在
if [ ! -d "docker/openresty/cert" ]; then
    echo "创建 docker/openresty/cert 目录..."
    mkdir -p "docker/openresty/cert"
fi

echo ""
echo "=========================================="
echo "目录结构验证："
echo "=========================================="
echo ""

ls -la docker/openresty/
echo ""
ls -la docker/openresty/conf/
echo ""
ls -la docker/openresty/conf/conf.d/
echo ""

echo "=========================================="
echo "✅ 诊断和修复完成！"
echo "=========================================="
echo ""
echo "现在可以运行："
echo "  docker compose -f onepanel-infra-compose.yml --env-file onepanel-infra.env up -d"
