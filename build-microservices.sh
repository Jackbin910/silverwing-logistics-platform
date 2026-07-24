#!/bin/bash

# 银翼智驭 - 微服务镜像打包脚本
# 前提：基础镜像 silverwing/base:1.0.0 已存在（由 build-infra-images.sh 构建并导出）
# 详细说明请参考 docs/DOCKER_DEPLOYMENT.md

set -e

# 配置参数（可环境变量覆盖）
: ${BUILD_VERSION:="1.0.0"}
: ${PROJECT_NAME:="silverwing"}
: ${SKIP_MAVEN:="false"}
# SERVICES: 逗号分隔的服务名列表，留空则构建全部
# 示例: SERVICES=auth,gateway BUILD_VERSION=1.0.1 ./build-microservices.sh
: ${SERVICES:=""}

ALL_SERVICES=('gateway' 'auth' 'core-service' 'digital-twin'
              'ai-service' 'ops-service' 'integration' 'admin-web')

# 解析 SERVICES 参数并校验
if [ -n "$SERVICES" ]; then
    IFS=',' read -ra services <<< "$SERVICES"
    for s in "${services[@]}"; do
        if [[ ! " ${ALL_SERVICES[@]} " =~ " ${s} " ]]; then
            echo "错误：未知服务名: $s" >&2
            echo "合法服务：${ALL_SERVICES[*]}" >&2
            exit 1
        fi
    done
else
    services=("${ALL_SERVICES[@]}")
fi

echo "版本：${BUILD_VERSION}"
echo "本次构建服务：${services[*]}"

# 0. 检查基础镜像（由 build-infra-images.sh 构建，本脚本不负责构建）
BASE_IMAGE="silverwing/base:1.0.0"
if ! docker images --format "{{.Repository}}:{{.Tag}}" | grep -q "^${BASE_IMAGE}$"; then
    echo "错误：未找到基础镜像 ${BASE_IMAGE}" >&2
    echo "请先运行：./build-infra-images.sh" >&2
    exit 1
fi

# 1. Maven 构建
if [ "$SKIP_MAVEN" = "true" ]; then
    echo "跳过 Maven 构建"
else
    echo "[1/5] Maven 构建..."
    mvn_modules=""
    for s in "${services[@]}"; do
        mvn_modules="${mvn_modules},silverwing-${s}"
    done
    mvn_modules="${mvn_modules#,}"
    mvn clean package -Pdocker -DskipTests -U -pl "$mvn_modules" -am
fi

# 2. 准备 Docker 构建目录
echo "[2/5] 准备 Docker 构建目录..."
for service in "${services[@]}"; do
    mkdir -p "docker/$service/target"
    # 兼容两种模块结构：
    #   单模块：silverwing-<svc>/target/silverwing-<svc>.jar
    #   多模块：silverwing-<svc>/silverwing-<svc>-start/target/silverwing-<svc>.jar
    jar_file="silverwing-$service/target/silverwing-$service.jar"
    if [ ! -f "$jar_file" ]; then
        jar_file="silverwing-$service/silverwing-$service-start/target/silverwing-$service.jar"
    fi
    if [ ! -f "$jar_file" ]; then
        echo "错误：找不到 JAR 文件：silverwing-$service 下无 silverwing-$service.jar" >&2
        exit 1
    fi
    cp "$jar_file" "docker/$service/target/"
done

# 3. 构建微服务 Docker 镜像
echo "[3/5] 构建微服务镜像..."
for service in "${services[@]}"; do
    echo "  构建：${service}"
    image_name="${PROJECT_NAME}/${service}:${BUILD_VERSION}"
    latest_image="${PROJECT_NAME}/${service}:latest"
    docker build -t "$image_name" -t "$latest_image" \
                 -f "docker/${service}/Dockerfile" "docker/${service}/"
done

# 4. 保存微服务镜像
echo "[4/5] 保存微服务镜像..."
OUTPUT_DIR="docker-microservices-${BUILD_VERSION}"
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"
for service in "${services[@]}"; do
    image_name="${PROJECT_NAME}/${service}:${BUILD_VERSION}"
    docker save "$image_name" > "${OUTPUT_DIR}/silverwing-${service}.tar"
done

# 5. 准备部署文件并打包
echo "[5/5] 准备部署文件并打包..."
cp onepanel-services-compose.yml "$OUTPUT_DIR/docker-compose.yml"
cp onepanel-services.env "$OUTPUT_DIR/.env"

# 版本信息
cat > "$OUTPUT_DIR/MICROSERVICES-VERSION" << EOF
银翼智驭 - 微服务镜像版本
版本号：${BUILD_VERSION}
构建时间：$(date '+%Y-%m-%d %H:%M:%S')
构建服务：${services[*]}

包含镜像：
EOF
for service in "${services[@]}"; do
    echo "  - silverwing/${service}:${BUILD_VERSION}" >> "$OUTPUT_DIR/MICROSERVICES-VERSION"
done
echo "" >> "$OUTPUT_DIR/MICROSERVICES-VERSION"
echo "注意：此目录内容由根目录脚本自动生成，请勿手动修改其中的配置文件。" >> "$OUTPUT_DIR/MICROSERVICES-VERSION"

# 加载脚本
cat > "$OUTPUT_DIR/load-microservices-images.sh" << 'LOADSCRIPT'
#!/bin/bash
# 银翼智驭 - 微服务镜像加载
set -e
cd "$(dirname "${BASH_SOURCE[0]}")"

# 检查基础镜像
#if ! docker images --format "{{.Repository}}:{{.Tag}}" | grep -q "silverwing/base:1.0.0"; then
#    echo "错误：缺少基础镜像 silverwing/base:1.0.0" >&2
#    echo "请先加载基础设施镜像包（silverwing-infra-images）" >&2
#    exit 1
#fi

total=$(ls -1 silverwing-*.tar 2>/dev/null | wc -l)
if [ "$total" -eq 0 ]; then
    echo "错误：未找到 silverwing-*.tar 镜像文件" >&2
    exit 1
fi

current=0
for file in silverwing-*.tar; do
    [ -f "$file" ] || continue
    current=$((current + 1))
    echo "[${current}/${total}] 加载：$file"
    docker load < "$file"
done

echo "完成。"
docker images | grep "silverwing"
LOADSCRIPT
chmod +x "$OUTPUT_DIR/load-microservices-images.sh"

# 打包
PACKAGE_NAME="silverwing-microservices-${BUILD_VERSION}.tar.gz"
tar -czf "$PACKAGE_NAME" "$OUTPUT_DIR"

echo ""
echo "完成：$PACKAGE_NAME ($(du -sh "$PACKAGE_NAME" | cut -f1))"
echo "部署说明请参考 docs/DOCKER_DEPLOYMENT.md"
