# 银翼物流平台 - 部署架构演进指南

> 从单机 All-in-One 到高可用集群的 4 阶段演进路线

---

## 当前现状基线

```
┌─────────── 服务器 A（单机）──────────┐
│  CPU 16C / 内存 32GB / 磁盘 200GB    │
│                                       │
│  ① 基础设施 compose（8 容器）         │
│     MySQL · Redis · PGVector · RustFS │
│     Nacos(单机) · RabbitMQ · XXL-Job  │
│     OpenResty                          │
│                                       │
│  ② 微服务 compose（8 容器）           │
│     Gateway · Auth · Core · Twin      │
│     AI · Ops · Integration · Admin    │
│                                       │
│  ③ 监控 compose（2 容器）             │
│     Prometheus · Grafana              │
│                                       │
│  ④ 宿主机进程                         │
│     Ollama (Qwen2.5 7B)               │
│     Docker daemon                     │
│                                       │
│  合计 18 容器 + 1 宿主机进程          │
│  内存峰值 ~20GB（含 Ollama CPU 推理） │
└───────────────────────────────────────┘
```

**核心痛点**：数据库和应用抢内存，Ollama 和 JVM 抢内存，任何一环挂了全站不可用。

---

## 演进路线总览

```
阶段 1：拆 2 台（应用机 + 数据库机）
  ↓
阶段 2：Nacos 集群（3 节点），MySQL 主从，Redis 哨兵
  ↓
阶段 3：K8s + Helm，微服务 HPA 自动扩缩
  ↓
阶段 4：Ollama 独立 GPU 节点，AI 服务独立部署
```

| 阶段 | 新增机器 | 总机器数 | 核心收益 | 适合时机 |
|------|---------|---------|---------|---------|
| 当前 | - | 1 | PoC 验证 | 开发/演示 |
| 阶段 1 | +1 | 2 | 解除内存争抢，数据独立 | 小规模上线 |
| 阶段 2 | +1 | 3 | 消除单点，故障自愈 | 正式生产 |
| 阶段 3 | +1~2 | 4~5 | 弹性扩缩，滚动更新 | 用户增长期 |
| 阶段 4 | +1(GPU) | 5~6 | AI 推理加速 5-10 倍 | AI 高频使用 |

---

## 阶段 1：拆 2 台（应用机 + 数据库机）

### 目标

把有状态中间件从应用机剥离，应用机专注跑微服务，数据库机专注存数据。解决内存争抢，为后续高可用打基础。

### 架构

```
┌─────────── 服务器 A「应用机」─────────┐  ┌──────── 服务器 B「数据库机」───────┐
│  CPU 16C / 内存 16GB / 磁盘 100GB     │  │  CPU 8C / 内存 16GB / 磁盘 500GB   │
│                                       │  │                                     │
│  ② 微服务 compose（8 容器）           │  │  ① 基础设施 compose（精简版）       │
│     Gateway · Auth · Core · Twin      │  │     MySQL 8.0    ← 业务库          │
│     AI · Ops · Integration · Admin    │  │     PGVector pg16 ← 向量库          │
│                                       │  │     Redis 6.2    ← 缓存            │
│  ③ 监控 compose（2 容器）             │  │     RabbitMQ     ← 消息队列        │
│     Prometheus · Grafana              │  │     XXL-Job      ← 任务调度        │
│                                       │  │     RustFS       ← RAG 对象存储    │
│                                       │  │     Nacos(单机)  ← 配置/注册       │
│  OpenResty（入口代理）                  │  │                                     │
│                                       │  │  数据卷：                          │
│  ─────────────────────────────────── │  │  mysql-data / pgvector-data        │
│  宿主机进程：无 Ollama（移到阶段4）   │  │  redis-data / rabbitmq-data        │
└───────────────┬───────────────────────┘  └───────────────┬─────────────────────┘
                │                                           │
                └──────── 内网 / VPN（10.0.x.x）────────────┘
```

### 机器配置

| 机器 | 角色 | CPU | 内存 | 磁盘 | 说明 |
|------|------|-----|------|------|------|
| 服务器 A | 应用机 | 16C | 16GB | 100GB SSD | 跑 8 微服务 + 监控 + OpenResty |
| 服务器 B | 数据库机 | 8C | 16GB | 500GB SSD | 跑 MySQL/Redis/PGVector/Nacos/RabbitMQ/XXL-Job/RustFS |

> 应用机 16GB 足够：8 微服务 JVM 堆合计 ~8.6GB + 监控 ~0.5GB + OpenResty/OS ~2GB = ~11GB，留 5GB 余量。

### 具体改动

#### 1）数据库机（服务器 B）：新建 `onepanel-db-compose.yml`

从现有 `onepanel-infra-compose.yml` 中**移除 Nginx**，其余保留，修改点：

```yaml
services:
  mysql:
    image: mysql:8.0.40
    container_name: silverwing-mysql
    # 端口只对内网开放
    ports:
      - "10.0.0.2:3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: silverwing_logistics
      TZ: Asia/Shanghai
    volumes:
      - mysql-data:/var/lib/mysql
      - ./scripts/sql:/docker-entrypoint-initdb.d:ro
    networks:
      - silverwing-db-net
    restart: always

  redis:
    image: redis/redis-stack-server:6.2.6-v20
    container_name: silverwing-redis
    ports:
      - "10.0.0.2:6379:6379"
    command: redis-stack-server --appendonly yes --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis-data:/data
    networks:
      - silverwing-db-net
    restart: always

  pgvector:
    image: pgvector/pgvector:pg16
    container_name: silverwing-pgvector
    ports:
      - "10.0.0.2:5432:5432"
    environment:
      POSTGRES_USER: ${PG_USER}
      POSTGRES_PASSWORD: ${PG_PASSWORD}
      POSTGRES_DB: silverwing_ai
      TZ: Asia/Shanghai
    volumes:
      - pgvector-data:/var/lib/postgresql/data
    networks:
      - silverwing-db-net
    restart: always

  nacos:
    image: nacos/nacos-server:v2.4.3
    container_name: silverwing-nacos
    ports:
      - "10.0.0.2:8848:8848"
      - "10.0.0.2:9848:9848"
    environment:
      - MODE=standalone
      - PREFER_HOST_MODE=hostname
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=mysql
      - MYSQL_SERVICE_DB_NAME=nacos_config
      - MYSQL_SERVICE_USER=${NACOS_DB_USER}
      - MYSQL_SERVICE_PASSWORD=${NACOS_DB_PASSWORD}
      - NACOS_AUTH_ENABLE=true
      - NACOS_AUTH_IDENTITY_KEY=serverIdentity
      - NACOS_AUTH_IDENTITY_VALUE=security
      - NACOS_AUTH_TOKEN=SecretKey012345678901234567890123456789012345678901234567890123456789
    depends_on:
      - mysql
    networks:
      - silverwing-db-net
    restart: always

  rabbitmq:
    image: rabbitmq:3.13.7-management
    container_name: silverwing-rabbitmq
    ports:
      - "10.0.0.2:5672:5672"
      - "10.0.0.2:15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_USER}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASSWORD}
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
    networks:
      - silverwing-db-net
    restart: always

  xxl-job-admin:
    image: xuxueli/xxl-job-admin:2.4.2
    container_name: silverwing-xxl-job
    ports:
      - "10.0.0.2:19080:8080"
    environment:
      PARAMS: "--spring.datasource.url=jdbc:mysql://mysql:3306/xxl_job?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai
        --spring.datasource.username=root
        --spring.datasource.password=${MYSQL_ROOT_PASSWORD}
        --xxl.job.accessToken=silverwing_token"
    depends_on:
      - mysql
    networks:
      - silverwing-db-net
    restart: always

  # RustFS 对象存储（S3 协议，RAG 原始文件持久化）
  rustfs:
    image: rustfs/rustfs:1.0.0-beta.9
    container_name: silverwing-rustfs
    ports:
      - "10.0.0.2:9000:9000"
      - "10.0.0.2:9001:9001"
    environment:
      RUSTFS_ACCESS_KEY: ${RUSTFS_ACCESS_KEY}
      RUSTFS_SECRET_KEY: ${RUSTFS_SECRET_KEY}
      RUSTFS_ADDRESS: ":9000"
      RUSTFS_CONSOLE_ENABLE: "true"
    command: ["/data"]
    volumes:
      - rustfs-data:/data
    networks:
      - silverwing-db-net
    restart: always

  # 不再包含 openresty 服务，openresty 移到应用机

volumes:
  mysql-data:
  pgvector-data:
  redis-data:
  rabbitmq-data:
  rustfs-data:

networks:
  silverwing-db-net:
    driver: bridge
```

#### 2）应用机（服务器 A）：修改 `onepanel-services-compose.yml`

```yaml
services:
  silverwing-gateway:
    image: silverwing/gateway:latest
    container_name: silverwing-gateway
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      JAVA_OPTS: ${GATEWAY_JAVA_OPTS}
      NACOS_SERVER_ADDR: 10.0.0.2:8848       # ← 改：指向数据库机
    networks:
      - silverwing-app-net
    restart: always

  silverwing-auth:
    image: silverwing/auth:latest
    container_name: silverwing-auth
    ports:
      - "8081:8081"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      JAVA_OPTS: ${AUTH_JAVA_OPTS}
      NACOS_SERVER_ADDR: 10.0.0.2:8848       # ← 改
    networks:
      - silverwing-app-net
    restart: always

  silverwing-core-service:
    image: silverwing/core-service:latest
    container_name: silverwing-core-service
    ports:
      - "8082:8082"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      JAVA_OPTS: ${CORE_SERVICE_JAVA_OPTS}
      NACOS_SERVER_ADDR: 10.0.0.2:8848       # ← 改
    networks:
      - silverwing-app-net
    restart: always

  silverwing-digital-twin:
    image: silverwing/digital-twin:latest
    container_name: silverwing-digital-twin
    ports:
      - "8083:8083"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      JAVA_OPTS: ${DIGITAL_TWIN_JAVA_OPTS}
      NACOS_SERVER_ADDR: 10.0.0.2:8848       # ← 改
    networks:
      - silverwing-app-net
    restart: always

  silverwing-ai-service:
    image: silverwing/ai-service:latest
    container_name: silverwing-ai-service
    ports:
      - "8084:8084"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      JAVA_OPTS: ${AI_SERVICE_JAVA_OPTS}
      NACOS_SERVER_ADDR: 10.0.0.2:8848       # ← 改
    networks:
      - silverwing-app-net
    restart: always

  silverwing-ops-service:
    image: silverwing/ops-service:latest
    container_name: silverwing-ops-service
    ports:
      - "8085:8085"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      JAVA_OPTS: ${OPS_SERVICE_JAVA_OPTS}
      NACOS_SERVER_ADDR: 10.0.0.2:8848       # ← 改
    networks:
      - silverwing-app-net
    restart: always

  silverwing-integration:
    image: silverwing/integration:latest
    container_name: silverwing-integration
    ports:
      - "8086:8086"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      JAVA_OPTS: ${INTEGRATION_JAVA_OPTS}
      NACOS_SERVER_ADDR: 10.0.0.2:8848       # ← 改
    networks:
      - silverwing-app-net
    restart: always

  silverwing-admin-web:
    image: silverwing/admin-web:latest
    container_name: silverwing-admin-web
    ports:
      - "8087:8087"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      JAVA_OPTS: ${ADMIN_WEB_JAVA_OPTS}
      NACOS_SERVER_ADDR: 10.0.0.2:8848       # ← 改
    networks:
      - silverwing-app-net
    restart: always

  # 新增 OpenResty（从基础设施层移过来）
  nginx:
    image: docker.1ms.run/openresty/openresty:1.25.3.2-alpine
    container_name: silverwing-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./docker/nginx-conf/conf/nginx.conf:/usr/local/openresty/nginx/conf/nginx.conf:ro
      - ./docker/nginx-conf/conf/conf.d:/usr/local/openresty/nginx/conf/conf.d:ro
    depends_on:
      - silverwing-gateway
    networks:
      - silverwing-app-net
    restart: always

networks:
  silverwing-app-net:
    driver: bridge
```

#### 3）Nacos 配置中心：修改中间件连接地址

这是**最关键的改动点**。当前 Nacos 里的配置项写的是容器名（`mysql:3306`），要改成数据库机 IP。

| Nacos 配置项 | 现值 | 改为 |
|-------------|------|------|
| `common-redis.yml` → `spring.data.redis.host` | `redis` | `10.0.0.2` |
| `common-redis.yml` → `spring.data.redis.password` | 空 | `${REDIS_PASSWORD}` |
| `common-pgvector.yml` → `langchain4j.vector-store.pgvector.host` | `pgvector` | `10.0.0.2` |
| `common-rabbitmq.yml` → `spring.rabbitmq.host` | `rabbitmq` | `10.0.0.2` |
| `common-mysql.yml` → `spring.datasource.url` | `jdbc:mysql://mysql:3306/...` | `jdbc:mysql://10.0.0.2:3306/...` |
| `silverwing-ai-service.yml` → `silverwing.storage.endpoint` | `http://rustfs:9000` | `http://10.0.0.2:9000` |
| `silverwing-ai-service.yml` → `silverwing.storage.bucket` | `silverwing` | `silverwing` |

#### 4）代码改动

**零代码改动**。所有中间件地址都通过 `${环境变量}` 或 Nacos 配置注入，改配置即可。

### 验证清单

```bash
# 1. 数据库机：确认所有中间件就绪
docker ps --format "table {{.Names}}\t{{.Status}}"
curl http://10.0.0.2:8848/nacos/v1/console/health/readiness

# 2. 应用机：确认微服务能连上数据库机
docker exec silverwing-auth curl -s http://10.0.0.2:8848/nacos/v1/console/health/readiness
docker exec silverwing-auth nc -zv 10.0.0.2 3306   # MySQL 连通性
docker exec silverwing-auth nc -zv 10.0.0.2 6379   # Redis 连通性

# 3. 端到端：登录测试
curl -X POST http://<服务器A>/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"xxx"}'
```

### 风险与回滚

| 风险 | 应对 |
|------|------|
| 内网延迟增加 | 同机房 <1ms，可忽略 |
| 数据库机单点 | 阶段 2 解决 |
| 回滚 | Nacos 配置改回容器名，服务迁回单机，compose 还原 |

---

## 阶段 2：核心中间件高可用

### 目标

消除单点故障。Nacos 集群、MySQL 主从、Redis 哨兵，任一节点宕机不影响业务。

### 架构

```
┌─────── 服务器 A「应用机」──────┐  ┌──── 服务器 B「数据主」────┐  ┌──── 服务器 C「数据从」────┐
│  16C/16GB/100GB                │  │  8C/16GB/500GB            │  │  8C/16GB/500GB            │
│                                │  │                            │  │                            │
│  8 微服务 + Nginx + 监控       │  │  Nacos-1  MySQL-Master    │  │  Nacos-2  MySQL-Slave     │
│                                │  │  Redis-Master  RabbitMQ-1 │  │  Nacos-3  Redis-Slave     │
│                                │  │  PGVector  XXL-Job        │  │  Redis-Sentinel           │
│                                │  │  RabbitMQ-2               │  │  RabbitMQ-3               │
└────────────────────────────────┘  └────────────┬───────────────┘  └────────────┬───────────────┘
                                     │   主从同步 / 哨兵心跳   │                │
                                     └────────────────────────┘────────────────┘
```

### 机器配置

| 机器 | 角色 | CPU | 内存 | 磁盘 | 新增 |
|------|------|-----|------|------|------|
| 服务器 A | 应用机 | 16C | 16GB | 100GB SSD | 不变 |
| 服务器 B | 数据主 | 8C | 16GB | 500GB SSD | 不变 |
| **服务器 C** | **数据从** | **8C** | **16GB** | **500GB SSD** | **新增** |

### 具体改动

#### 1）Nacos 3 节点集群

Nacos 集群模式需要 3 个节点（奇数防脑裂），分布在 3 台机器上：

```yaml
# 服务器 B 上的 nacos-1（onepanel-db-compose.yml）
nacos-1:
  image: nacos/nacos-server:v2.4.3
  container_name: silverwing-nacos-1
  ports:
    - "10.0.0.2:8848:8848"
    - "10.0.0.2:9848:9848"
  environment:
    - MODE=cluster
    - NACOS_SERVERS=10.0.0.2:8848 10.0.0.3:8848 10.0.0.4:8848
    - PREFER_HOST_MODE=ip
    - SPRING_DATASOURCE_PLATFORM=mysql
    - MYSQL_SERVICE_HOST=10.0.0.2
    - MYSQL_SERVICE_DB_NAME=nacos_config
    - MYSQL_SERVICE_USER=${NACOS_DB_USER}
    - MYSQL_SERVICE_PASSWORD=${NACOS_DB_PASSWORD}
    - NACOS_AUTH_ENABLE=true
    - NACOS_AUTH_IDENTITY_KEY=serverIdentity
    - NACOS_AUTH_IDENTITY_VALUE=security
    - NACOS_AUTH_TOKEN=SecretKey012345678901234567890123456789012345678901234567890123456789
  networks:
    - silverwing-db-net
  restart: always

# 服务器 C 上的 nacos-2（新增）
# 配置同上，只改容器名和端口绑定为 10.0.0.3
```

**微服务 Nacos 地址改为 3 个节点**：

```yaml
# onepanel-services-compose.yml
NACOS_SERVER_ADDR: 10.0.0.2:8848,10.0.0.3:8848,10.0.0.4:8848
```

#### 2）MySQL 主从复制

**主库配置（服务器 B）**：

```ini
# my.cnf 新增
[mysqld]
server-id=1
log-bin=mysql-bin
binlog-format=ROW
gtid-mode=ON
enforce-gtid-consistency=ON
binlog-ignore-db=mysql
binlog-ignore-db=information_schema
binlog-ignore-db=performance_schema
```

**创建复制账号**：

```sql
-- 主库执行
CREATE USER 'repl'@'10.0.0.%' IDENTIFIED BY 'repl_password_2024';
GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'repl'@'10.0.0.%';
FLUSH PRIVILEGES;
```

**从库配置（服务器 C）**：

```ini
# my.cnf
[mysqld]
server-id=2
relay-log=relay-bin
read_only=ON
```

**建立复制关系**：

```sql
-- 从库执行
CHANGE MASTER TO
  MASTER_HOST='10.0.0.2',
  MASTER_PORT=3306,
  MASTER_USER='repl',
  MASTER_PASSWORD='repl_password_2024',
  MASTER_AUTO_POSITION=1;

START SLAVE;
SHOW SLAVE STATUS\G
-- 检查 Slave_IO_Running 和 Slave_SQL_Running 均为 Yes
```

**应用层读写分离**：

引入 `dynamic-datasource-spring-boot-starter` 依赖：

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>dynamic-datasource-spring-boot-starter</artifactId>
    <version>4.3.1</version>
</dependency>
```

Nacos 配置 `common-mysql.yml`：

```yaml
spring:
  datasource:
    dynamic:
      primary: master
      strict: false
      datasource:
        master:
          url: jdbc:mysql://10.0.0.2:3306/silverwing_logistics?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
          username: root
          password: ${MYSQL_ROOT_PASSWORD}
        slave:
          url: jdbc:mysql://10.0.0.3:3306/silverwing_logistics?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
          username: root
          password: ${MYSQL_ROOT_PASSWORD}
```

只读方法标注 `@DS("slave")`：

```java
@DS("slave")
public List<Order> queryOrderList(OrderQuery query) {
    return orderMapper.selectList(query.toWrapper());
}
```

#### 3）Redis Sentinel 哨兵

```
Redis-Master (10.0.0.2:6379)  ←──主从复制──→  Redis-Slave (10.0.0.3:6379)
       ↑                                              ↑
       └──────── Sentinel × 3 监控投票 ──────────────┘
       (10.0.0.2:26379, 10.0.0.3:26379, 10.0.0.4:26379)
```

**Redis 实例配置**：

```conf
# redis-master.conf（服务器 B）
port 6379
requirepass ${REDIS_PASSWORD}
masterauth ${REDIS_PASSWORD}
appendonly yes

# redis-slave.conf（服务器 C）
port 6379
requirepass ${REDIS_PASSWORD}
replicaof 10.0.0.2 6379
masterauth ${REDIS_PASSWORD}
appendonly yes
```

**Sentinel 配置（每台机器一个）**：

```conf
# sentinel.conf
port 26379
sentinel monitor silverwing-master 10.0.0.2 6379 2
sentinel auth-pass silverwing-master ${REDIS_PASSWORD}
sentinel down-after-milliseconds silverwing-master 3000
sentinel failover-timeout silverwing-master 10000
sentinel parallel-syncs silverwing-master 1
```

**微服务 Redis 连接改为 Sentinel 模式**：

```yaml
# Nacos: common-redis.yml
spring:
  data:
    redis:
      sentinel:
        master: silverwing-master
        nodes: 10.0.0.2:26379,10.0.0.3:26379,10.0.0.4:26379
      password: ${REDIS_PASSWORD}
```

> `spring-boot-starter-data-redis` 原生支持 Sentinel，代码零改动。

#### 4）RabbitMQ 镜像队列

```yaml
# RabbitMQ 集群配置（三节点）
# 服务器 B
rabbitmq-1:
  image: rabbitmq:3.13.7-management
  container_name: silverwing-rabbitmq-1
  hostname: rabbitmq-1
  environment:
    - RABBITMQ_ERLANG_COOKIE=silverwing_cluster_cookie
    - RABBITMQ_DEFAULT_USER=${RABBITMQ_USER}
    - RABBITMQ_DEFAULT_PASS=${RABBITMQ_PASSWORD}
  ports:
    - "10.0.0.2:5672:5672"
    - "10.0.0.2:15672:15672"
  networks:
    - silverwing-db-net
  restart: always

# 服务器 C
rabbitmq-2:
  image: rabbitmq:3.13.7-management
  container_name: silverwing-rabbitmq-2
  hostname: rabbitmq-2
  environment:
    - RABBITMQ_ERLANG_COOKIE=silverwing_cluster_cookie
    - RABBITMQ_DEFAULT_USER=${RABBITMQ_USER}
    - RABBITMQ_DEFAULT_PASS=${RABBITMQ_PASSWORD}
  ports:
    - "10.0.0.3:5672:5672"
    - "10.0.0.3:15672:15672"
  networks:
    - silverwing-db-net
  restart: always
```

**加入集群**：

```bash
# 在 rabbitmq-2 容器内执行
docker exec -it silverwing-rabbitmq-2 sh
rabbitmqctl stop_app
rabbitmqctl join_cluster rabbit@rabbitmq-1
rabbitmqctl start_app

# 启用镜像队列策略（所有队列在 2 个节点上镜像）
rabbitmqctl set_policy ha-all "^" '{"ha-mode":"exactly","ha-params":2,"ha-sync-mode":"automatic"}'
```

### 验证清单

```bash
# Nacos 集群健康检查
curl "http://10.0.0.2:8848/nacos/v1/ns/operator/metrics" | grep raftTerm

# MySQL 主从状态
docker exec mysql-slave mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "SHOW SLAVE STATUS\G" | grep -E "Slave_IO_Running|Slave_SQL_Running|Seconds_Behind_Master"

# Redis 哨兵状态
docker exec redis-sentinel-1 redis-cli -p 26379 -a ${REDIS_PASSWORD} SENTINEL master silverwing-master

# RabbitMQ 集群状态
docker exec silverwing-rabbitmq-1 rabbitmqctl cluster_status

# 故障切换测试
docker stop mysql-master        # 主库宕机，验证从库是否提升
docker stop redis-master        # Redis 主宕机，Sentinel 应在 10s 内完成切换
# 业务应自动恢复
```

### 风险与回滚

| 风险 | 应对 |
|------|------|
| 主从延迟导致读到旧数据 | 关键写后读走主库，`@DS("master")` 强制 |
| 哨兵切换时 3-10s 不可用 | 可接受，比单点全挂强 |
| Nacos 集群脑裂 | 奇数节点 + raft 协议天然防脑裂 |
| RabbitMQ 脑裂 | `pause_minority` 策略，少部分自动暂停 |

---

## 阶段 3：K8s + Helm，微服务 HPA 自动扩缩

### 目标

微服务从 Docker Compose 迁移到 Kubernetes，实现按负载自动扩缩容、滚动更新零停机、资源隔离。

### 架构

```
┌────────── K8s 集群（3+ 节点）──────────────────────────┐
│                                                         │
│  ┌─── Master ───┐  ┌─── Worker-1 ──┐  ┌─── Worker-2 ─┐│
│  │ etcd/API/调度│  │ 微服务 Pod ×N │  │ 微服务 Pod ×N││
│  └──────────────┘  │ Ingress       │  │ Ingress      ││
│                    └───────────────┘  └──────────────┘│
│                                                         │
│  微服务：Deployment + HPA（CPU>70% 自动扩 2→5 副本）   │
│  入口：Ingress Controller（替代 Nginx）                 │
│  配置：Nacos（外部） / ConfigMap / Secret               │
│  监控：Prometheus Operator + Grafana                    │
│                                                         │
└──────────────────────┬──────────────────────────────────┘
                       │
          ┌────────────┴────────────┐
          │  外部数据库集群（阶段2） │
          │  MySQL主从 / Redis哨兵  │
          │  Nacos集群 / RabbitMQ   │
          └─────────────────────────┘
```

### 机器配置

| 机器 | 角色 | CPU | 内存 | 磁盘 | 说明 |
|------|------|-----|------|------|------|
| K8s Master | 控制面 | 4C | 8GB | 50GB SSD | 可与 Worker 合并（小集群用 k3s） |
| Worker-1 | 工作节点 | 16C | 32GB | 100GB SSD | 跑微服务 Pod |
| Worker-2 | 工作节点 | 16C | 32GB | 100GB SSD | 跑微服务 Pod |
| 数据库机 | 外部 | - | - | - | 阶段 2 的 B/C 机器不变 |

> 推荐使用 **k3s**（轻量 K8s）起步，资源占用小，单二进制部署，适合 5 台以下小集群。

### 具体改动

#### 1）K3s 集群搭建

```bash
# Master 节点
curl -sfL https://get.k3s.io | sh -s - --write-kubeconfig-mode 644
# 获取 token
sudo cat /var/lib/rancher/k3s/server/node-token

# Worker 节点
curl -sfL https://get.k3s.io | K3S_URL=https://<master-ip>:6443 K3S_TOKEN=<token> sh -

# 验证
kubectl get nodes
# 期望：3 个节点均为 Ready
```

#### 2）Helm Chart 结构

```
silverwing-helm/
├── Chart.yaml                     # Chart 元信息
├── values.yaml                    # 全局配置（镜像版本、环境变量）
├── values-prod.yaml               # 生产环境覆盖
├── templates/
│   ├── _helpers.tpl               # 公共模板宏
│   ├── gateway-deployment.yaml    # Gateway Deployment
│   ├── gateway-service.yaml       # Gateway Service
│   ├── gateway-hpa.yaml           # Gateway HPA
│   ├── auth-deployment.yaml
│   ├── auth-service.yaml
│   ├── auth-hpa.yaml
│   ├── core-deployment.yaml
│   ├── core-service.yaml
│   ├── core-hpa.yaml
│   ├── ai-deployment.yaml
│   ├── ai-service.yaml
│   ├── ai-hpa.yaml
│   ├── ...（其余微服务）
│   ├── ingress.yaml               # Ingress 规则
│   ├── configmap.yaml             # 公共配置
│   └── secret.yaml                # 敏感信息
```

#### 3）values.yaml（核心配置）

```yaml
# 镜像仓库配置
image:
  registry: harbor.internal.silverwing.com   # 内网 Harbor
  tag: latest
  pullPolicy: IfNotPresent

# Nacos 地址（指向外部集群）
nacos:
  serverAddr: "10.0.0.2:8848,10.0.0.3:8848,10.0.0.4:8848"

# 各服务资源配置
services:
  gateway:
    replicas: 2
    port: 8080
    javaOpts: "-Xms256m -Xmx512m"
    resources:
      requests:
        cpu: "250m"
        memory: "512Mi"
      limits:
        cpu: "1000m"
        memory: "768Mi"
    hpa:
      minReplicas: 2
      maxReplicas: 4
      targetCPUUtilization: 70

  auth:
    replicas: 2
    port: 8081
    javaOpts: "-Xms256m -Xmx512m"
    resources:
      requests:
        cpu: "250m"
        memory: "512Mi"
      limits:
        cpu: "1000m"
        memory: "768Mi"
    hpa:
      minReplicas: 2
      maxReplicas: 3
      targetCPUUtilization: 70

  core-service:
    replicas: 2
    port: 8082
    javaOpts: "-Xms512m -Xmx1g"
    resources:
      requests:
        cpu: "500m"
        memory: "1Gi"
      limits:
        cpu: "2000m"
        memory: "1536Mi"
    hpa:
      minReplicas: 2
      maxReplicas: 5
      targetCPUUtilization: 70

  digital-twin:
    replicas: 1
    port: 8083
    javaOpts: "-Xms512m -Xmx1g"
    resources:
      requests:
        cpu: "500m"
        memory: "1Gi"
      limits:
        cpu: "2000m"
        memory: "1536Mi"
    hpa:
      minReplicas: 1
      maxReplicas: 2
      targetCPUUtilization: 70

  ai-service:
    replicas: 1
    port: 8084
    javaOpts: "-Xms1g -Xmx2g"
    resources:
      requests:
        cpu: "500m"
        memory: "1536Mi"
      limits:
        cpu: "3000m"
        memory: "2560Mi"
    hpa:
      minReplicas: 1
      maxReplicas: 2
      targetCPUUtilization: 80

  ops-service:
    replicas: 1
    port: 8085
    javaOpts: "-Xms256m -Xmx512m"
    resources:
      requests:
        cpu: "250m"
        memory: "512Mi"
      limits:
        cpu: "1000m"
        memory: "768Mi"
    hpa:
      minReplicas: 1
      maxReplicas: 2
      targetCPUUtilization: 70

  integration:
    replicas: 1
    port: 8086
    javaOpts: "-Xms256m -Xmx512m"
    resources:
      requests:
        cpu: "250m"
        memory: "512Mi"
      limits:
        cpu: "1000m"
        memory: "768Mi"
    hpa:
      minReplicas: 1
      maxReplicas: 2
      targetCPUUtilization: 70

  admin-web:
    replicas: 1
    port: 8087
    javaOpts: "-Xms256m -Xmx512m"
    resources:
      requests:
        cpu: "250m"
        memory: "512Mi"
      limits:
        cpu: "1000m"
        memory: "768Mi"
    hpa:
      minReplicas: 1
      maxReplicas: 2
      targetCPUUtilization: 70
```

#### 4）Deployment 模板示例（Gateway）

```yaml
# templates/gateway-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: silverwing-gateway
  labels:
    app: silverwing-gateway
spec:
  replicas: {{ .Values.services.gateway.replicas }}
  selector:
    matchLabels:
      app: silverwing-gateway
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0              # 滚动更新零停机
  template:
    metadata:
      labels:
        app: silverwing-gateway
    spec:
      containers:
        - name: gateway
          image: "{{ .Values.image.registry }}/gateway:{{ .Values.image.tag }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - containerPort: {{ .Values.services.gateway.port }}
          env:
            - name: NACOS_SERVER_ADDR
              value: {{ .Values.nacos.serverAddr | quote }}
            - name: SPRING_PROFILES_ACTIVE
              value: "k8s"
            - name: JAVA_OPTS
              value: {{ .Values.services.gateway.javaOpts | quote }}
          resources:
            {{- toYaml .Values.services.gateway.resources | nindent 12 }}
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: {{ .Values.services.gateway.port }}
            initialDelaySeconds: 30
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: {{ .Values.services.gateway.port }}
            initialDelaySeconds: 60
            periodSeconds: 30
```

#### 5）HPA 模板示例（Core Service）

```yaml
# templates/core-hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: silverwing-core-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: silverwing-core-service
  minReplicas: {{ .Values.services.core-service.hpa.minReplicas }}
  maxReplicas: {{ .Values.services.core-service.hpa.maxReplicas }}
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: {{ .Values.services.core-service.hpa.targetCPUUtilization }}
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
        - type: Percent
          value: 100
          periodSeconds: 30
    scaleDown:
      stabilizationWindowSeconds: 300   # 缩容等待 5 分钟，防止抖动
      policies:
        - type: Pods
          value: 1
          periodSeconds: 60
```

#### 6）Ingress 配置

```yaml
# templates/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: silverwing-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/proxy-body-size: "50m"
    nginx.ingress.kubernetes.io/proxy-read-timeout: "60"
spec:
  ingressClassName: nginx
  rules:
    - host: api.silverwing.local
      http:
        paths:
          - path: /api/auth
            pathType: Prefix
            backend:
              service:
                name: silverwing-auth
                port:
                  number: 8081
          - path: /api/core
            pathType: Prefix
            backend:
              service:
                name: silverwing-core-service
                port:
                  number: 8082
          - path: /api/ai
            pathType: Prefix
            backend:
              service:
                name: silverwing-ai-service
                port:
                  number: 8084
          # ... 其余路由
```

#### 7）CI/CD 流水线变化

```
现有流程：
  mvn package → docker build → scp tar.gz → docker load → docker compose up

K8s 流程：
  mvn package → docker build → docker push Harbor → helm upgrade --set image.tag=x.x.x
```

**GitLab CI / Jenkins 流水线示例**：

```yaml
# .gitlab-ci.yml（示意）
build:
  stage: build
  script:
    - mvn clean package -DskipTests
    - docker build -t harbor.internal.silverwing.com/auth:${CI_COMMIT_SHORT_SHA} ./docker/auth
    - docker push harbor.internal.silverwing.com/auth:${CI_COMMIT_SHORT_SHA}

deploy:
  stage: deploy
  script:
    - helm upgrade silverwing ./silverwing-helm
      --set image.tag=${CI_COMMIT_SHORT_SHA}
      --namespace silverwing-prod
      --reuse-values
```

### 验证清单

```bash
# 部署
helm install silverwing ./silverwing-helm -n silverwing-prod --create-namespace
kubectl get pods -n silverwing-prod -w

# HPA 状态
kubectl get hpa -n silverwing-prod
# NAME                              REFERENCE                    TARGETS   MINPODS   MAXPODS   REPLICAS
# silverwing-core-service-hpa       Deployment/core-service      15%/70%   2         5         2

# 压测触发扩容
kubectl run load-generator --image=busybox -- sh -c "while true; do wget -q -O- http://silverwing-core-service/api/core/orders; done"
# 观察 HPA 变化
kubectl get hpa silverwing-core-service-hpa -n silverwing-prod -w
# 期望：replicas 从 2 逐渐升到 4-5

# 滚动更新零停机
helm upgrade silverwing ./silverwing-helm --set image.tag=1.1.0 -n silverwing-prod
kubectl rollout status deployment/silverwing-core-service -n silverwing-prod
# 期间 curl 持续请求，验证无 5xx

# 回滚
helm rollback silverwing 1 -n silverwing-prod
```

### 风险与回滚

| 风险 | 应对 |
|------|------|
| K8s 学习曲线 | 用 k3s 起步，学习成本远低于完整 K8s |
| 镜像仓库依赖 | 内网自建 Harbor 或使用 `registry:2` 轻量方案 |
| 配置迁移 | Nacos 保持不变，ConfigMap/Secret 仅补充 K8s 特有配置 |
| 回滚 | `helm rollback` 一键回退到上一版本 |

---

## 阶段 4：Ollama 独立 GPU 节点

### 目标

把 AI 推理从 CPU（慢，6-8GB 内存）迁到 GPU（快，独立显存），AI 服务与 LLM 解耦，互不影响。

### 架构

```
┌──── K8s 集群 ────┐    ┌─── GPU 节点（服务器 D）───┐
│                  │    │  CPU 8C / 16GB / 100GB     │
│  AI Service Pod  │───→│  GPU: RTX 3060 12GB        │
│  (无 LLM，纯API) │    │                            │
│                  │    │  Ollama + Qwen2.5 7B       │
│  Core/Auth/...   │    │  (GPU 推理，~5GB 显存)     │
│                  │    │                            │
└──────────────────┘    │  Embedding Model (BGE)    │
                        │  (CPU 推理，~500MB)        │
                        └────────────────────────────┘
```

### 机器配置

| 机器 | 角色 | CPU | 内存 | 磁盘 | GPU | 新增 |
|------|------|-----|------|------|-----|------|
| 服务器 D | **GPU 节点** | 8C | 16GB | 100GB SSD | **RTX 3060 12GB** | **新增** |

> RTX 3060 12GB 是性价比之选。Qwen2.5 7B 量化后约 4.5GB 显存，12GB 足够同时加载模型 + 推理上下文。如需跑更大模型（14B+），考虑 RTX 3090/4090 24GB。

### 具体改动

#### 1）GPU 节点环境准备

```bash
# 安装 NVIDIA 驱动
sudo apt update
sudo apt install -y nvidia-driver-535
sudo reboot
nvidia-smi   # 验证驱动安装成功

# 安装 nvidia-container-toolkit（让 Docker 能挂载 GPU）
curl -fsSL https://nvidia.github.io/libnvidia-container/gpgkey | sudo gpg --dearmor -o /usr/share/keyrings/nvidia-container-toolkit-keyring.gpg
curl -s -L https://nvidia.github.io/libnvidia-container/stable/deb/nvidia-container-toolkit.list | \
  sed 's#deb https://#deb [signed-by=/usr/share/keyrings/nvidia-container-toolkit-keyring.gpg] https://#g' | \
  sudo tee /etc/apt/sources.list.d/nvidia-container-toolkit.list
sudo apt update
sudo apt install -y nvidia-container-toolkit
sudo systemctl restart docker
```

#### 2）GPU 节点部署 Ollama

```yaml
# gpu-node-compose.yml
services:
  ollama:
    image: ollama/ollama:latest
    container_name: ollama
    ports:
      - "10.0.0.5:11434:11434"    # 只对内网开放
    volumes:
      - ollama-models:/root/.ollama
    deploy:
      resources:
        reservations:
          devices:
            - driver: nvidia
              capabilities: [gpu]
              count: 1
    environment:
      - OLLAMA_HOST=0.0.0.0
      - OLLAMA_MAX_LOADED_MODELS=1
      - OLLAMA_NUM_PARALLEL=2       # 并发推理数，RTX 3060 建议 2
      - OLLAMA_KEEP_ALIVE=5m        # 模型闲置 5 分钟后卸载
    restart: always

volumes:
  ollama-models:
```

#### 3）预拉模型

```bash
docker exec -it ollama ollama pull qwen2.5:7b
# 模型约 4.5GB，持久化到 ollama-models 卷
# 验证
docker exec -it ollama ollama list
# NAME          ID              SIZE      MODIFIED
# qwen2.5:7b    845d7d4a4d06    4.5 GB    2 days ago
```

#### 4）AI 服务连接地址改为 GPU 节点

```yaml
# Nacos: silverwing-ai-service.yml
langchain4j:
  ollama:
    chat-model:
      base-url: http://10.0.0.5:11434   # 从 host.docker.internal 改为 GPU 节点 IP
      model: qwen2.5:7b
      temperature: 0.7
      timeout: 60s
    embedding-model:
      base-url: http://10.0.0.5:11434
      model: bge-m3
      timeout: 30s
```

#### 5）AI Service 通过 ExternalName Service 访问（K8s 环境）

```yaml
# K8s ExternalName Service
apiVersion: v1
kind: Service
metadata:
  name: ollama-external
  namespace: silverwing-prod
spec:
  type: ExternalName
  externalName: 10.0.0.5     # GPU 节点 IP
```

AI Service Pod 通过 `http://ollama-external:11434` 访问，无需硬编码 IP。

#### 6）为什么不在 K8s 里跑 Ollama？

| 方式 | 优点 | 缺点 | 建议 |
|------|------|------|------|
| K8s 内（GPU Operator） | 统一管理 | 需装 nvidia-device-plugin，GPU 调度复杂，资源独占 | 不推荐初期 |
| **独立 Docker（推荐）** | 简单稳定，GPU 直通，独立扩缩 | 多一层网络（内网 <1ms） | **当前最佳** |
| K8s + GPU Node | 统一调度 | 需 GPU nodeSelector，Ollama 作为 DaemonSet | 有 K8s GPU 经验后可选 |

医院场景 AI 推理 QPS 不高（语音下单、知识库问答），独立 Docker 完全够用。

### 验证清单

```bash
# GPU 节点
nvidia-smi   # 确认 GPU 可用，驱动正常
curl http://10.0.0.5:11434/api/tags   # 确认模型已加载

# 推理性能对比
# CPU 推理（当前）：~15-30s/次（Qwen2.5 7B on 16C CPU）
# GPU 推理（阶段4）：~2-5s/次（Qwen2.5 7B on RTX 3060）
# 性能提升：5-10x

# AI Service 调用测试
curl -X POST http://<ingress>/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"帮我申请一批手术手套"}'
# 期望：2-5s 内返回结果

# 监控 GPU 使用率
watch -n 1 nvidia-smi
# 观察推理时 GPU-Util 是否上升到 80-100%
```

### 风险与回滚

| 风险 | 应对 |
|------|------|
| GPU 节点宕机 | AI 功能降级但不影响主业务（语音下单可回退到传统方式） |
| 模型版本不一致 | 通过 Ollama API 锁定模型 tag（如 `qwen2.5:7b-q4_K_M`） |
| 显存不足 | RTX 3060 12GB 跑 7B 模型绰绰有余，若未来换 14B 模型需 24GB 卡 |
| 回滚 | Nacos 配置改回 `host.docker.internal:11434`，恢复 CPU 推理 |

---

## 全局演进路线图

```
时间轴（按需推进，无需同时启动所有阶段）

阶段 1（预计 2 周）
  ├── 第 1 周：准备服务器 B，部署中间件
  ├── 第 2 周：修改 Nacos 配置 + compose 文件，联调验证
  └── 交付：应用与数据分离，内存压力解除

阶段 2（预计 3 周）
  ├── 第 1 周：准备服务器 C，部署 Nacos 集群 + MySQL 从库
  ├── 第 2 周：Redis Sentinel + RabbitMQ 集群
  └── 第 3 周：故障切换测试，验收高可用

阶段 3（预计 4 周）
  ├── 第 1 周：搭建 K3s 集群 + Harbor 镜像仓库
  ├── 第 2 周：编写 Helm Chart
  ├── 第 3 周：微服务迁移 + CI/CD 流水线
  └── 第 4 周：HPA 压测 + 灰度切换

阶段 4（预计 1 周）
  ├── 第 1-3 天：GPU 节点部署 Ollama + 模型下载
  ├── 第 4-5 天：AI 服务联调 + 性能对比
  └── 交付：AI 推理加速 5-10 倍
```

---

## 附录

### A. 网络规划参考

| 网段 | 用途 | 备注 |
|------|------|------|
| `10.0.0.0/24` | 内网管理网段 | 所有服务器在这个网段 |
| `10.0.0.1` | 网关/路由器 | VPN 入口 |
| `10.0.0.2` | 服务器 B（数据库主） | 阶段 1-4 |
| `10.0.0.3` | 服务器 C（数据库从） | 阶段 2-4 |
| `10.0.0.4` | 服务器 A（应用机/K8s Master） | 阶段 1-4 |
| `10.0.0.5` | 服务器 D（GPU 节点） | 阶段 4 |
| `10.0.0.10-19` | K8s Worker 节点（预留） | 阶段 3-4 |

### B. 中间件密码安全加固

> 当前所有密码为 `123456`，生产环境务必修改。

| 中间件 | 当前密码 | 修改位置 | 影响范围 |
|--------|---------|---------|---------|
| MySQL root | `123456` | `onepanel-infra.env` → `MYSQL_ROOT_PASSWORD` | 所有微服务 |
| Redis | 空（无密码） | `onepanel-infra.env` + Nacos `common-redis.yml` | 所有微服务 |
| Nacos | `123456` | `onepanel-infra.env` | Nacos 自身 |
| RabbitMQ | `123456` | `onepanel-infra.env` | 消息队列 |
| RustFS | `silverwing` / `123456` | `onepanel-infra.env` → `RUSTFS_ACCESS_KEY` / `RUSTFS_SECRET_KEY` | RAG 对象存储 |

建议使用环境变量注入，不同环境不同密码，生产密码通过 Vault/Sealed Secrets 管理。

### C. 与现有文档的关系

| 文档 | 内容 | 本指南补充 |
|------|------|-----------|
| `ARCHITECTURE.md` | 技术架构设计 | 部署架构的物理拓扑 |
| `DOCKER_DEPLOYMENT.md` | 单机 Docker Compose 部署 | 多机拆分和高可用 |
| `DEPLOYMENT.md` | 基础部署说明 | 4 阶段演进路线 |
| **本指南** | **部署架构演进** | **从单机到集群的完整路径** |

### D. 回滚总体原则

每个阶段都保持前向兼容，可以独立回滚：

1. 先备份当前配置和 compose 文件
2. 按阶段逐步回滚（阶段 4 → 3 → 2 → 1）
3. 每个阶段回滚后验证业务正常再回滚下一阶段
4. 核心业务数据（MySQL/PGVector/Redis）始终保留，不随部署架构变更丢失
