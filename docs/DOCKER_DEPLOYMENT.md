# 银翼智驭 - 内网环境部署指南

## 目录

- [部署流程概览](#部署流程概览)
- [外网构建](#外网构建)
- [内网部署](#内网部署)
- [更新流程](#更新流程)
- [脚本参数说明](#脚本参数说明)
- [常用操作](#常用操作)
- [故障排查](#故障排查)
- [部署目录结构](#部署目录结构)

---

## 部署流程概览

```
┌─────────────── 外网环境 ───────────────┐    ┌─────────────── 内网环境 ───────────────┐
│                                        │    │                                        │
│  ./build-infra-images.sh               │    │  1. 加载基础设施镜像                    │
│    → silverwing-infra-images.tar.gz    │ ──→│  2. 部署基础设施（MySQL/Redis/Nacos…）  │
│                                        │    │  3. 初始化数据库和 Nacos 配置           │
│  ./build-microservices.sh              │    │  4. 加载微服务镜像                      │
│    → silverwing-microservices-1.0.0.tar.gz │  5. 部署微服务                          │
│                                        │    │  6. 验证部署                            │
│  ./build-monitor-images.sh             │    │  7. 加载监控栈镜像                      │
│    → silverwing-monitor-images.tar.gz  │ ──→│  8. 部署监控栈（Prometheus/Grafana）    │
│                                        │    │  9. 验证监控                            │
└────────────────────────────────────────┘    └────────────────────────────────────────┘
```

### 关键设计

- **base 镜像走基础设施包**：`silverwing/base:1.0.0` 由 `build-infra-images.sh` 本地构建并导出到 infra 包中。这样时序有保障——infra 包一定先加载，base 镜像先就位，加载微服务包时检查 base 不会失败。
- **两个包解耦**：基础设施包与微服务包独立构建、独立传输、独立更新。
- **微服务增量更新**：可通过 `SERVICES` 参数只构建变更的服务，加载后只重启对应容器，不影响其他服务。

---

## 外网构建

### 1. 构建基础设施镜像包

```bash
./build-infra-images.sh
```

产物：`silverwing-infra-images.tar.gz`（解压后为 `silverwing-infra-images/` 目录）

目录内容：

| 文件/目录 | 说明 |
|----------|------|
| `*.tar` | 基础设施镜像（MySQL/Redis/Nacos/RabbitMQ/XXL-Job/Nginx） |
| `silverwing-base-1.0.0.tar` | 银翼基础镜像（本地构建） |
| `onepanel-infra-compose.yml` | 基础设施编排文件 |
| `onepanel-infra.env` | 环境变量模板 |
| `load-infra-images.sh` | 镜像加载脚本（自动生成） |
| `INFRA-IMAGES-VERSION` | 版本信息 |
| `scripts/` | 数据库初始化 SQL |
| `docker/` | Nginx 等服务配置文件 |

包含的镜像版本：

| 组件 | 镜像版本 |
|------|---------|
| MySQL | 8.0.40 |
| Redis | 6.2.18 |
| Nacos | v2.4.3 |
| RabbitMQ | 3.13.7-management |
| XXL-Job | 2.4.2 |
| Nginx | 1.26-alpine |
| 银翼基础镜像 | silverwing/base:1.0.0 |

### 2. 构建微服务镜像包

```bash
# 构建全部 8 个微服务
BUILD_VERSION=1.0.0 ./build-microservices.sh
```

> 前提：本地已存在 `silverwing/base:1.0.0` 镜像（由上一步 `build-infra-images.sh` 构建生成）。

产物：`silverwing-microservices-1.0.0.tar.gz`（解压后为 `docker-microservices-1.0.0/` 目录）

目录内容：

| 文件/目录 | 说明 |
|----------|------|
| `silverwing-*.tar` | 各微服务镜像 |
| `docker-compose.yml` | 微服务编排文件 |
| `.env` | 环境变量模板 |
| `nacos-config-templates/` | Nacos 配置模板 |
| `load-microservices-images.sh` | 镜像加载脚本（自动生成） |
| `MICROSERVICES-VERSION` | 版本信息 |

### 3. 构建监控栈镜像包

```bash
./build-monitor-images.sh
```

> 前提：监控栈依赖 `silverwing-network` 网络与微服务容器名抓取指标，需在基础设施和微服务应用部署完成后部署。

产物：`silverwing-monitor-images.tar.gz`（解压后为 `silverwing-monitor-images/` 目录）

目录内容：

| 文件/目录 | 说明 |
|----------|------|
| `*.tar` | 监控栈镜像（Prometheus/Grafana） |
| `onepanel-monitor-compose.yml` | 监控栈编排文件 |
| `onepanel-monitor.env` | 环境变量模板 |
| `docker/monitor/` | Prometheus 与 Grafana provisioning 配置 |
| `load-monitor-images.sh` | 镜像加载脚本（自动生成） |
| `MONITOR-IMAGES-VERSION` | 版本信息 |

包含的镜像版本：

| 组件 | 镜像版本 |
|------|---------|
| Prometheus | v2.55.1 |
| Grafana | 11.4.0 |

### 4. 传输到内网服务器

```bash
scp silverwing-infra-images.tar.gz user@server:/opt/
scp silverwing-microservices-1.0.0.tar.gz user@server:/opt/
scp silverwing-monitor-images.tar.gz user@server:/opt/
```

---

## 内网部署

### 1. 加载基础设施镜像

```bash
ssh user@server
cd /opt
tar -xzf silverwing-infra-images.tar.gz
cd silverwing-infra-images
./load-infra-images.sh

# 验证
docker images | grep -E "mysql|redis|nacos|rabbitmq|nginx|silverwing/base"
```

### 2. 配置并部署基础设施

```bash
# 编辑环境变量（修改密码等）
vi onepanel-infra.env
```

环境变量示例：

```env
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_DATABASE=silverwing
MYSQL_USER=silverwing
MYSQL_PASSWORD=silverwing_password
REDIS_PASSWORD=redis_secure_password
NACOS_PASSWORD=nacos_secure_password
```

```bash
# 创建网络（首次部署）
docker network create silverwing-network

# 启动基础设施（需在 silverwing-infra-images/ 目录下，依赖 ./docker 配置）
docker compose -f onepanel-infra-compose.yml --env-file onepanel-infra.env up -d

# 验证
docker ps | grep -E "mysql|redis|nacos|rabbitmq|nginx"
```

### 3. 初始化数据库

```bash
# 等待 MySQL 就绪（约 30 秒）
sleep 30

# 初始化主数据库
docker compose -f onepanel-infra-compose.yml exec -T mysql \
  mysql -uroot -p${MYSQL_ROOT_PASSWORD} silverwing < scripts/init.sql

# 初始化 AI 服务数据库
docker compose -f onepanel-infra-compose.yml exec -T mysql \
  mysql -uroot -p${MYSQL_ROOT_PASSWORD} silverwing < scripts/ai-service.sql
```

### 4. 初始化 Nacos 配置

1. 访问 Nacos：http://server:8848/nacos（用户名/密码：nacos/your_password）
2. 新建命名空间：ID = `silverwing-prod`，名称 = 银翼生产环境
3. 切换到 `silverwing-prod` 命名空间
4. 配置管理 → 配置列表 → 导入配置
5. 上传 `nacos-config-templates/` 目录下所有 `.yml` 文件
6. 修改配置中的数据库密码为实际值并发布

### 5. 加载微服务镜像

```bash
cd /opt
tar -xzf silverwing-microservices-1.0.0.tar.gz
cd docker-microservices-1.0.0
./load-microservices-images.sh

# 验证
docker images | grep silverwing
```

### 6. 部署微服务

```bash
# 编辑环境变量
vi .env
```

环境变量示例：

```env
NACOS_NAMESPACE=silverwing-prod
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos_secure_password
APP_VERSION=1.0.0
OLLAMA_BASE_URL=http://host.docker.internal:11434

# 每个微服务独立配置 JVM 参数（按负载调整堆内存）
GATEWAY_JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heap_dump.hprof
AUTH_JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heap_dump.hprof
CORE_SERVICE_JAVA_OPTS=-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heap_dump.hprof
DIGITAL_TWIN_JAVA_OPTS=-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heap_dump.hprof
AI_SERVICE_JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heap_dump.hprof
OPS_SERVICE_JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heap_dump.hprof
INTEGRATION_JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heap_dump.hprof
ADMIN_WEB_JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heap_dump.hprof
```

> 每个微服务通过 `XXX_JAVA_OPTS` 独立配置 JVM 参数，通用参数含 G1GC 和 OOM 堆转储。按服务负载调整堆内存：AI 服务最大（2g），核心/孪生服务次之（1g），其余轻量服务 512m。

```bash
# 确保网络已存在
docker network ls | grep silverwing-network || docker network create silverwing-network

# 启动微服务
docker compose -f docker-compose.yml --env-file .env up -d

# 验证
docker ps | grep silverwing
```

### 7. 验证部署

| 服务 | 端口 | 访问地址 |
|------|------|---------|
| 管理后台 | 8087 | http://server/admin/ |
| API 文档 | 8080 | http://server/doc.html |
| Nacos 控制台 | 8848 | http://server:8848/nacos |
| Prometheus | 9090 | http://server:9090 |
| Grafana | 3000 | http://server:3000 |

```bash
# 查看所有容器
docker ps --format "table {{.Names}}\t{{.Status}}"

# 访问管理后台
curl http://your-server/admin/
```

### 8. 加载监控栈镜像

```bash
cd /opt
tar -xzf silverwing-monitor-images.tar.gz
cd silverwing-monitor-images
./load-monitor-images.sh

# 验证
docker images | grep -E "prometheus|grafana"
```

### 9. 部署监控栈

> 前提：基础设施应用（创建 `silverwing-network` 网络）与微服务应用已部署。监控栈通过容器名抓取微服务的 `/actuator/prometheus` 端点。

```bash
# 编辑环境变量（修改 Grafana 管理员密码等）
vi onepanel-monitor.env
```

环境变量示例：

```env
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=grafana_secure_password
PROMETHEUS_PORT=9090
GRAFANA_PORT=3000
```

```bash
# 启动监控栈（需在 silverwing-monitor-images/ 目录下，依赖 ./docker/monitor 配置）
docker compose -f onepanel-monitor-compose.yml --env-file onepanel-monitor.env up -d

# 验证
docker ps | grep -E "prometheus|grafana"

# 检查 Prometheus 抓取目标是否全部 UP
# 访问 http://server:9090/targets
```

> **数据存储**：Prometheus 和 Grafana 的持久化数据使用 Docker named volume（`prometheus-data`、`grafana-data`），由 Docker 自动管理权限，无需手动 `chown`。容器内以非 root 用户运行，若改用 bind mount 会因宿主机目录权限不足导致 `permission denied` 启动失败。
>
> 查看数据卷位置：`docker volume ls | grep -E "prometheus|grafana"`
>
> 备份 Grafana 数据：`docker run --rm -v grafana-data:/data -v $(pwd):/backup alpine tar czf /backup/grafana-data-backup.tar.gz -C /data .`

---

## 更新流程

### 更新指定微服务（推荐）

```bash
# 外网：构建指定服务（多个用逗号分隔）
SERVICES=auth,gateway BUILD_VERSION=1.0.1 ./build-microservices.sh

# 传输
scp silverwing-microservices-1.0.1.tar.gz user@server:/opt/

# 内网：加载并重启
cd /opt
tar -xzf silverwing-microservices-1.0.1.tar.gz
cd docker-microservices-1.0.1
./load-microservices-images.sh

# 只重启更新的服务（关键命令）
docker compose -f docker-compose.yml up -d silverwing-auth silverwing-gateway
```

> `docker compose up -d <服务名>` 只重启指定容器，不影响其他服务。

### 全量更新

```bash
# 外网
BUILD_VERSION=1.0.1 ./build-microservices.sh

# 内网
cd /opt
tar -xzf silverwing-microservices-1.0.1.tar.gz
cd docker-microservices-1.0.1
./load-microservices-images.sh

# 修改 .env 中的 APP_VERSION
sed -i 's/APP_VERSION=.*/APP_VERSION=1.0.1/' .env

# 重新部署所有微服务
docker compose -f docker-compose.yml --env-file .env up -d
```

### 更新基础设施

```bash
# 外网重新构建
./build-infra-images.sh

# 内网加载并重启
cd /opt/silverwing-infra-images
./load-infra-images.sh
docker compose -f onepanel-infra-compose.yml --env-file onepanel-infra.env up -d
```

---

## 脚本参数说明

### build-infra-images.sh

无参数。脚本执行流程：

1. 拉取基础设施镜像列表
2. 构建 `silverwing/base:1.0.0`（若本地已存在则跳过）
3. 导出所有镜像为 `.tar`
4. 复制配置文件、生成加载脚本和版本信息
5. 打包为 `silverwing-infra-images.tar.gz`

强制重建 base 镜像：

```bash
docker rmi silverwing/base:1.0.0
./build-infra-images.sh
```

### build-microservices.sh

| 环境变量 | 默认值 | 说明 |
|---------|--------|------|
| `BUILD_VERSION` | `1.0.0` | 构建版本号，影响输出目录和镜像 tag |
| `SERVICES` | 空（构建全部） | 逗号分隔的服务名列表，如 `auth,gateway` |
| `SKIP_MAVEN` | `false` | 设为 `true` 跳过 Maven 编译（jar 已就绪时） |
| `PROJECT_NAME` | `silverwing` | 镜像名前缀 |

可选服务名：`gateway`、`auth`、`core-service`、`digital-twin`、`ai-service`、`ops-service`、`integration`、`admin-web`

示例：

```bash
# 只构建 auth 服务
SERVICES=auth BUILD_VERSION=1.0.1 ./build-microservices.sh

# 跳过 Maven（jar 已编译好）
SKIP_MAVEN=true SERVICES=auth ./build-microservices.sh
```

### build-monitor-images.sh

无参数。脚本执行流程：

1. 拉取监控栈镜像（Prometheus、Grafana）
2. 导出所有镜像为 `.tar`
3. 复制 compose、env、provisioning 配置，生成加载脚本和版本信息
4. 打包为 `silverwing-monitor-images.tar.gz`

### load-monitor-images.sh

镜像加载入口脚本，扫描当前目录下所有 `.tar` 文件并执行 `docker load` 导入本地 Docker。项目里存在两个同名脚本，用途不同：

| 脚本位置 | 生成方式 | 用途 |
|---------|---------|------|
| 根目录 `load-monitor-images.sh` | 手动维护 | 本地便捷入口：外网构建后验证镜像可正常加载，或开发机手动导入 |
| 产物目录 `silverwing-monitor-images/load-monitor-images.sh` | `build-monitor-images.sh` 自动生成 | 内网部署实际使用，随部署包传输到目标服务器 |

两者功能一致，均会遍历当前目录 `*.tar` 逐个加载。区别仅在使用场景：根目录脚本用于开发机本地验证；产物目录脚本用于内网部署。

> 内网部署时无需携带根目录脚本，解压产物包后在 `silverwing-monitor-images/` 目录内执行 `./load-monitor-images.sh` 即可。

使用示例（外网构建后本地验证）：

```bash
./build-monitor-images.sh
cd silverwing-monitor-images
./load-monitor-images.sh

# 验证镜像已加载
docker images | grep -E "prometheus|grafana"
```

---

## 常用操作

### 查看日志

```bash
# 微服务日志
docker logs -f silverwing-gateway

# 基础设施日志
docker logs -f mysql
```

### 重启服务

```bash
# 重启单个服务

# 重启基础设施
docker compose -f onepanel-infra-compose.yml --env-file onepanel-infra.env restart
```

### 备份数据

```bash
# MySQL 备份
docker exec mysql mysqldump -uroot -p${MYSQL_ROOT_PASSWORD} silverwing > backup.sql
```

### 资源监控

```bash
docker ps
docker stats
```

---

## 故障排查

### 容器无法启动

```bash
docker logs <container_name>
docker inspect <container_name>
```

### 网络问题

```bash
docker network ls
docker network inspect silverwing-network

# 重建网络
docker network rm silverwing-network
docker network create silverwing-network
```

### 数据库连接失败

```bash
docker ps | grep mysql
docker exec -it mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD}
docker logs mysql
```

### Nacos 配置问题

```bash
docker ps | grep nacos
docker logs nacos
# 访问 http://server:8848/nacos
```

### 微服务加载失败（缺少基础镜像）

```bash
# 检查基础镜像是否存在
docker images | grep silverwing/base

# 若缺失，说明基础设施包未先加载
# 请先解压并加载 silverwing-infra-images.tar.gz
```

### Prometheus 启动失败（permission denied）

```
Error opening query log file file=/prometheus/queries.active err="open /prometheus/queries.active: permission denied"
panic: Unable to create mmap-ed active query log
```

原因：Prometheus 容器以 `nobody` 用户运行，若数据目录权限不对则无法写入。当前 compose 已使用 named volume（`prometheus-data`）由 Docker 自动管理权限，正常不会出现此问题。

若仍出现（如手动改回 bind mount），修复方式：

```bash
# 将宿主机数据目录属主改为 nobody(65534)
sudo chown -R 65534:65534 ./data/prometheus
sudo chmod -R 755 ./data/prometheus
```

Grafana 同理，用户 UID 为 472：

```bash
sudo chown -R 472:472 ./data/grafana
```

---

## 部署目录结构

部署完成后的目录结构：

```
/opt/
├── silverwing-infra-images/         # 基础设施包（解压后）
│   ├── INFRA-IMAGES-VERSION
│   ├── load-infra-images.sh
│   ├── onepanel-infra-compose.yml
│   ├── onepanel-infra.env
│   ├── *.tar                        # 基础设施镜像
│   ├── silverwing-base-1.0.0.tar    # 银翼基础镜像
│   ├── scripts/                     # 数据库 SQL
│   └── docker/                      # Nginx 等配置
├── docker-microservices-1.0.0/      # 微服务包（解压后）
│   ├── MICROSERVICES-VERSION
│   ├── load-microservices-images.sh
│   ├── docker-compose.yml
│   ├── .env
│   ├── nacos-config-templates/
│   └── silverwing-*.tar             # 各微服务镜像
└── silverwing-monitor-images/       # 监控栈包（解压后）
    ├── MONITOR-IMAGES-VERSION
    ├── load-monitor-images.sh
    ├── onepanel-monitor-compose.yml
    ├── onepanel-monitor.env
    ├── *.tar                        # 监控栈镜像
    └── docker/monitor/              # Prometheus/Grafana provisioning 配置
```
