#!/bin/bash

# 银翼智驭 - 监控栈镜像导出脚本
# 详细说明请参考 docs/DOCKER_DEPLOYMENT.md

set -e

# 监控栈镜像列表
declare -a IMAGES=(
    "docker.1ms.run/prom/prometheus:v2.55.1"
    "docker.1ms.run/grafana/grafana:11.4.0"
)

OUTPUT_DIR="silverwing-monitor-images"

# 1. 拉取镜像
echo "[1/4] 拉取镜像..."
for image in "${IMAGES[@]}"; do
    echo "  拉取：$image"
    docker pull "$image"
done

# 2. 导出镜像
echo "[2/4] 导出镜像..."
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"
for image in "${IMAGES[@]}"; do
    filename=$(echo "$image" | sed 's/\//_/g' | sed 's/:/-/g')
    echo "  导出：$image"
    docker save "$image" -o "${OUTPUT_DIR}/${filename}.tar"
done

# 3. 复制配置文件并生成加载脚本
echo "[3/4] 复制配置文件..."
cp onepanel-monitor-compose.yml "$OUTPUT_DIR/"
cp onepanel-monitor.env "$OUTPUT_DIR/"
if [ -d "docker/monitor" ]; then
    rm -rf "$OUTPUT_DIR/docker"
    mkdir -p "$OUTPUT_DIR/docker"
    cp -r docker/monitor "$OUTPUT_DIR/docker/"
fi

# 生成加载脚本
cat > "$OUTPUT_DIR/load-monitor-images.sh" << 'LOADSCRIPT'
#!/bin/bash
# 银翼智驭 - 监控栈镜像加载
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
chmod +x "$OUTPUT_DIR/load-monitor-images.sh"

# 生成版本信息
cat > "$OUTPUT_DIR/MONITOR-IMAGES-VERSION" << EOF
银翼智驭 - 监控栈镜像版本
创建时间：$(date '+%Y-%m-%d %H:%M:%S')

包含镜像：
  - docker.1ms.run/prom/prometheus:v2.55.1
  - docker.1ms.run/grafana/grafana:11.4.0

前提：基础设施应用（silverwing-network 网络）与微服务应用已部署。
注意：此目录内容由根目录脚本自动生成，请勿手动修改其中的配置文件。
EOF

# 4. 打包
echo "[4/4] 打包..."
PACKAGE_NAME="silverwing-monitor-images.tar.gz"
tar -czf "$PACKAGE_NAME" "$OUTPUT_DIR"

echo ""
echo "完成：$PACKAGE_NAME ($(du -sh "$PACKAGE_NAME" | cut -f1))"
echo "部署说明请参考 docs/DOCKER_DEPLOYMENT.md"
