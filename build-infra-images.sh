#!/bin/bash

# 银翼智驭 - 基础设施镜像导出脚本
# 详细说明请参考 docs/DOCKER_DEPLOYMENT.md

set -e

# 基础设施镜像列表
declare -a IMAGES=(
    "docker.1ms.run/library/mysql:8.0.40"
    "docker.1ms.run/redis/redis-stack-server:6.2.6-v20"
    "docker.1ms.run/pgvector/pgvector:pg16"
    "docker.1ms.run/nacos/nacos-server:v2.4.3"
    "docker.1ms.run/library/rabbitmq:3.13.7-management"
    "docker.1ms.run/xuxueli/xxl-job-admin:2.4.2"
    "docker.1ms.run/library/nginx:1.26-alpine"
    "docker.1ms.run/rustfs/rustfs:1.0.0-beta.9"
)

BASE_IMAGE="silverwing/base:1.0.0"
OUTPUT_DIR="silverwing-infra-images"

# 1. 拉取镜像
echo "[1/5] 拉取镜像..."
for image in "${IMAGES[@]}"; do
    echo "  拉取：$image"
    docker pull "$image"
done

# 2. 构建银翼基础镜像（本地 Dockerfile，非 pull）
echo "[2/5] 构建银翼基础镜像..."
if [ ! -f "docker/base/Dockerfile" ]; then
    echo "错误：找不到 docker/base/Dockerfile" >&2
    exit 1
fi
if ! docker images --format "{{.Repository}}:{{.Tag}}" | grep -q "^${BASE_IMAGE}$"; then
    docker build -t "$BASE_IMAGE" -f docker/base/Dockerfile docker/base/
    echo "  构建完成：$BASE_IMAGE"
else
    echo "  已存在，跳过：$BASE_IMAGE"
fi

# 3. 导出镜像
echo "[3/5] 导出镜像..."
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"
for image in "${IMAGES[@]}"; do
    filename=$(echo "$image" | sed 's/\//_/g' | sed 's/:/-/g')
    echo "  导出：$image"
    docker save "$image" -o "${OUTPUT_DIR}/${filename}.tar"
done
echo "  导出：$BASE_IMAGE"
docker save "$BASE_IMAGE" -o "${OUTPUT_DIR}/silverwing-base-1.0.0.tar"

# 4. 复制配置文件并生成加载脚本
echo "[4/5] 复制配置文件..."
cp onepanel-infra-compose.yml "$OUTPUT_DIR/"
cp onepanel-infra.env "$OUTPUT_DIR/"
if [ -d "scripts" ]; then
    rm -rf "$OUTPUT_DIR/scripts"
    cp -r scripts "$OUTPUT_DIR/"
fi
if [ -d "docker" ]; then
    rm -rf "$OUTPUT_DIR/docker"
    cp -r docker "$OUTPUT_DIR/"
fi

# 生成加载脚本
cat > "$OUTPUT_DIR/load-infra-images.sh" << 'LOADSCRIPT'
#!/bin/bash
# 银翼智驭 - 基础设施镜像加载
set -e
cd "$(dirname "${BASH_SOURCE[0]}")"

total=$(ls -1 *.tar 2>/dev/null | wc -l)
if [ "$total" -eq 0 ]; then
    echo "错误：未找到 .tar 镜像文件" >&2
    exit 1
fi

current=0
for file in *.tar; do
    [ -f "$file" ] || continue
    current=$((current + 1))
    echo "[${current}/${total}] 加载：$file"
    docker load < "$file"
done

echo "完成。"
LOADSCRIPT
chmod +x "$OUTPUT_DIR/load-infra-images.sh"

# 生成版本信息
cat > "$OUTPUT_DIR/INFRA-IMAGES-VERSION" << EOF
银翼智驭 - 基础设施镜像版本
创建时间：$(date '+%Y-%m-%d %H:%M:%S')

包含镜像：
  - docker.1ms.run/library/mysql:8.0.40
  - docker.1ms.run/redis/redis-stack-server:6.2.6-v20
  - docker.1ms.run/pgvector/pgvector:pg16
  - docker.1ms.run/nacos/nacos-server:v2.4.3
  - docker.1ms.run/library/rabbitmq:3.13.7-management
  - docker.1ms.run/xuxueli/xxl-job-admin:2.4.2
  - docker.1ms.run/library/nginx:1.26-alpine
  - docker.1ms.run/rustfs/rustfs:1.0.0-beta.9
  - silverwing/base:1.0.0（本地构建）

注意：此目录内容由根目录脚本自动生成，请勿手动修改其中的配置文件。
EOF

# 5. 打包
echo "[5/5] 打包..."
PACKAGE_NAME="silverwing-infra-images.tar.gz"
tar -czf "$PACKAGE_NAME" "$OUTPUT_DIR"

echo ""
echo "完成：$PACKAGE_NAME ($(du -sh "$PACKAGE_NAME" | cut -f1))"
echo "部署说明请参考 docs/DOCKER_DEPLOYMENT.md"
