# 银翼智驭 - Jenkins 离线部署完整流程

> **场景**：服务器**没有外网**，不能通过 Git 拉代码，也不能在线拉 Docker 镜像。
> 所有镜像在外网机器构建打包后，通过 U 盘 / 内网传输到目标服务器，用 Jenkins 编排部署。
>
> Jenkins 与微服务、基础设施全部跑在同一台离线服务器上。

---

## 和有网场景的核心区别

| 环节 | 有网场景 | 离线场景（本文档） |
|---|---|---|
| 镜像来源 | Jenkins 在线 `docker build` | 外网构建好，`docker load` 导入 |
| 代码来源 | Jenkins 从 Git 拉取 | 不需要代码，镜像已构建好 |
| Maven 编译 | Jenkins 内 `mvn package` | 外网已完成，内网不需要 |
| JDK / Maven | Jenkins 需要 | **不需要**（镜像已构建好） |
| Git 仓库 | 需要（Gitea / 远程） | **不需要** |
| Jenkins 流水线 | 6 阶段（检出→编译→构建→部署→检查） | 4 阶段（检查镜像→更新版本→部署→检查） |

**一句话总结**：外网把所有镜像构建好打包，内网 `docker load` 后，Jenkins 只负责 `docker compose up` + 健康检查。

---

## 流程总览

```
┌─────────────── 外网机器（有网络）───────────────┐
│                                                    │
│  ① 构建基础设施镜像包  build-infra-images.sh       │
│  ② 构建微服务镜像包    build-microservices.sh      │
│  ③ 导出 Jenkins 镜像   docker save                 │
│                                                    │
│  产物（3 个包）                                     │
│    → silverwing-infra-images.tar.gz                │
│      （含基础设施镜像 + base 镜像 + 基础设施配置）  │
│    → silverwing-microservices-1.0.1.tar.gz         │
│      （含微服务镜像 + 微服务部署配置）              │
│    → jenkins-image.tar                             │
└────────────────────┬───────────────────────────────┘
                     │ U盘 / 内网传输
                     ▼
┌─────────────── 内网服务器（离线）─────────────────┐
│                                                    │
│  ④ 服务器初始化        装 Docker                   │
│  ⑤ 加载所有镜像        docker load                 │
│  ⑥ 基础设施部署        docker compose up           │
│  ⑦ Nacos 配置初始化    建命名空间、导入配置        │
│  ⑧ 安装 Jenkins        docker run（用导入的镜像）  │
│  ⑨ 创建部署流水线      Pipeline script（不用 Git） │
│  ⑩ 一键部署微服务      Build → 自动部署+健康检查   │
│  ⑪ 验证 & 日常运维      访问、更新、回滚           │
│                                                    │
└────────────────────────────────────────────────────┘
```

**为什么不需要单独打包源码？**

两个构建脚本已经把所有配置文件打进镜像包了：

| 配置文件 | 在哪个包里 | 由哪个脚本打包 |
|---|---|---|
| `onepanel-infra-compose.yml` + `onepanel-infra.env` | infra 包 | `build-infra-images.sh` 第 4 步 |
| `scripts/`（数据库 SQL） | infra 包 | `build-infra-images.sh` 第 4 步 |
| `docker/`（Nginx 配置等） | infra 包 | `build-infra-images.sh` 第 4 步 |
| `docker-compose.yml`（= services-compose） | microservices 包 | `build-microservices.sh` 第 5 步 |
| `.env`（= services.env） | microservices 包 | `build-microservices.sh` 第 5 步 |

所以不需要额外打包源码，解压镜像包就有全部配置。

**约定**（替换为实际值）：

| 项 | 值 |
|---|---|
| 外网机器 | 有 Docker + Maven + JDK17 + 项目源码的开发机 |
| 内网服务器 IP | `192.168.164.128` |
| 镜像版本 | `1.0.1`（与 `onepanel-services.env` 的 `APP_VERSION` 一致） |
| Jenkins 端口 | `8888` |

---

## 第一阶段：外网准备（在有网络的机器上操作）

### ① 构建基础设施镜像包

在外网机器的项目根目录执行：

```bash
./build-infra-images.sh
```

产物：`silverwing-infra-images.tar.gz`

包含内容：
- MySQL / Redis / PGVector / Nacos / RabbitMQ / XXL-Job / Nginx 镜像
- `silverwing/base:1.0.0` 基础镜像（微服务 Dockerfile 的 FROM）
- `onepanel-infra-compose.yml` + `onepanel-infra.env`（基础设施配置）
- `scripts/`（数据库 SQL）
- `docker/`（Nginx 配置等）
- `load-infra-images.sh`（加载脚本）

### ② 构建微服务镜像包

```bash
BUILD_VERSION=1.0.1 ./build-microservices.sh
```

产物：`silverwing-microservices-1.0.1.tar.gz`

包含内容：
- 8 个微服务镜像（`silverwing/gateway:1.0.1` 等）
- `docker-compose.yml`（即 `onepanel-services-compose.yml`，微服务部署配置）
- `.env`（即 `onepanel-services.env`，微服务环境变量）
- `load-microservices-images.sh`（加载脚本）

> 如果只改了部分服务，可以只构建变更的：
> `SERVICES=auth,gateway BUILD_VERSION=1.0.2 ./build-microservices.sh`

### ③ 导出 Jenkins 镜像

Jenkins 镜像也要离线导入。在外网机器上：

```bash
# 先拉取（如果本地没有）
docker pull docker.1ms.run/jenkins/jenkins:lts-jdk17

# 重新打 tag（去掉加速前缀，内网用干净名字）
docker tag docker.1ms.run/jenkins/jenkins:lts-jdk17 jenkins/jenkins:lts-jdk17

# 导出
docker save jenkins/jenkins:lts-jdk17 -o jenkins-image.tar
```

### 传输清单

把以下 3 个文件传到内网服务器（U 盘 / scp / 内网中转）：

| 文件 | 大小（参考） | 说明 |
|---|---|---|
| `silverwing-infra-images.tar.gz` | ~1.5GB | 基础设施镜像 + base 镜像 + 基础设施配置 + SQL + Nginx 配置 |
| `silverwing-microservices-1.0.1.tar.gz` | ~1.5GB | 8 个微服务镜像 + 微服务部署配置（compose + env） |
| `jenkins-image.tar` | ~600MB | Jenkins 镜像 |

---

## 第二阶段：内网部署（在离线服务器上操作）

### ④ 服务器初始化

SSH 登录内网服务器。

**装 Docker**（如果有 Docker 的离线安装包）：

```bash
# 方式 1：如果有 docker 离线 deb/rpm 包
dpkg -i docker-ce-*.deb docker-ce-cli-*.deb containerd.io_*.deb
systemctl enable --now docker

# 方式 2：如果有 docker 静态二进制包
tar -xzf docker-*.tgz -C /usr/local/bin
systemctl enable --now docker
```

> 如果连 Docker 都没有离线包，需要在外网额外准备 Docker 离线安装包。
> 下载地址：https://download.docker.com/linux/static/stable/

**验证**：

```bash
docker version
docker compose version
```

**不需要装 JDK、Maven、Git**——离线场景下 Jenkins 不编译代码，镜像已在外网构建好。

### ⑤ 加载所有镜像

```bash
cd /opt

# 1. 解压并加载基础设施镜像（解压后目录含配置文件，保留）
tar -xzf silverwing-infra-images.tar.gz
cd silverwing-infra-images
./load-infra-images.sh
cd /opt

# 2. 解压并加载微服务镜像（解压后目录含配置文件，保留）
tar -xzf silverwing-microservices-1.0.1.tar.gz
cd docker-microservices-1.0.1
./load-microservices-images.sh
cd /opt

# 3. 加载 Jenkins 镜像
docker load < jenkins-image.tar

# 4. 把微服务部署配置复制到部署目录（只需首次做一次）
mkdir -p /opt/silverwing-deploy
cp docker-microservices-1.0.1/docker-compose.yml /opt/silverwing-deploy/
cp docker-microservices-1.0.1/.env /opt/silverwing-deploy/
```

> 第 4 步把微服务包里的 `docker-compose.yml` 和 `.env` 放到部署目录，后续 Jenkins 部署直接用这个目录。如果以后 compose 文件有更新，重新从新包复制覆盖即可。

**验证所有镜像**：

```bash
docker images | grep -E "mysql|redis|nacos|rabbitmq|nginx|silverwing|jenkins"
```

应看到：
- 基础设施镜像 7 个（mysql/redis/pgvector/nacos/rabbitmq/xxl-job/nginx）
- `silverwing/base:1.0.0`
- 微服务镜像 8 个（`silverwing/gateway:1.0.1` 等）
- `jenkins/jenkins:lts-jdk17`

### ⑥ 基础设施部署

基础设施配置在 infra 包解压目录里，直接用：

```bash
# 创建共享网络
docker network create silverwing-network

cd /opt/silverwing-infra-images

# 改密码（生产务必改，当前都是 123456）
vi onepanel-infra.env
# 重点改：MYSQL_ROOT_PASSWORD / NACOS_SERVICE_PASSWORD / RABBITMQ_PASSWORD 等

# 启动基础设施
docker compose -f onepanel-infra-compose.yml --env-file onepanel-infra.env up -d

# 等待全部 healthy（约 1-2 分钟）
watch -n2 'docker ps --format "table {{.Names}}\t{{.Status}}" | grep -E "mysql|redis|nacos|rabbitmq|nginx|xxl"'
```

验证：

```bash
curl -s http://localhost:8848/nacos/v1/console/health/readiness   # OK
docker exec mysql-server mysql -uroot -p123456 -e "SHOW DATABASES;"
```

### ⑦ Nacos 配置初始化

浏览器打开 `http://192.168.164.128:8848/nacos`，登录（`nacos` / `onepanel-infra.env` 里的密码）。

**1. 创建命名空间**

命名空间 → 新建：
- 命名空间 ID：**`b7273740-ffae-4c91-9e78-ba1360e42a0e`**（必须和 `.env` 的 `NACOS_NAMESPACE` 一致）
- 名称：`银翼生产环境`

**2. 导入配置**

切换到该命名空间 → 配置管理 → 配置列表 → 导入配置。

至少创建：

| Data ID | 内容要点 |
|---|---|
| `common-mysql.yml` | host=`mysql`、port=3306、用户名密码 |
| `common-redis.yml` | host=`redis`、port=6379 |
| `common-pgvector.yml` | host=`pgvector`（AI 服务用） |
| `common-rabbitmq.yml` | host=`rabbitmq`、port=5672 |
| `silverwing-gateway.yml` | 网关路由规则 |
| `silverwing-auth.yml` | Auth 专属配置 |
| `silverwing-core-service.yml` | 核心服务专属配置 |
| `silverwing-ai-service.yml` | AI 服务专属（含 Ollama 地址） |

> host 必须用容器名（`mysql`/`redis`/`nacos`/`rabbitmq`/`pgvector`），微服务和基础设施在同一 Docker 网络。

### ⑧ 安装 Jenkins

```bash
mkdir -p /opt/jenkins/home

docker run -d \
  --name jenkins \
  --restart always \
  --user root \
  --network silverwing-network \
  -p 8888:8080 -p 50000:50000 \
  -v /opt/jenkins/home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v /opt/silverwing-deploy:/opt/silverwing-deploy \
  -v /usr/bin/docker:/usr/bin/docker \
  -v /usr/libexec/docker/cli-plugins:/usr/libexec/docker/cli-plugins \
  --group-add $(getent group docker | cut -d: -f3) \
  jenkins/jenkins:lts-jdk17
```

**关键点**：
- 挂载 `docker.sock` + `docker` 二进制 → Jenkins 容器内能执行 docker 命令
- `--group-add docker组ID` → 确保能访问 docker.sock
- `--network silverwing-network` → 和微服务同网络
- **不挂载 Maven / JDK** → 离线场景不需要编译

验证 Jenkins 能跑 docker：

```bash
docker exec jenkins docker ps
# 能列出宿主机容器即正常
```

解锁 Jenkins：

```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

浏览器打开 `http://192.168.164.128:8888`：
1. 粘贴密码 → 继续
2. **选「选择插件来安装」→ 取消所有勾选 → 不装任何插件**（离线环境装不了，Pipeline 是自带的）
3. 创建管理员账号

> 如果跳过插件安装后提示配置代理，直接关掉，不影响使用。

### ⑨ 创建部署流水线

**1. 新建任务**

Jenkins 首页 → New Item：
- 名称：`silverwing-deploy`
- 类型：**Pipeline**
- OK

**2. 配置 Pipeline**

任务配置页 → 拉到底部 **Pipeline** 区域：

```
Definition:  Pipeline script
```

把 `Jenkinsfile.offline` 的**完整内容**复制粘贴到 Script 文本框里。

> `Jenkinsfile.offline` 在项目根目录，外网构建时如果需要可以一并抄一份带过去。
> 也可以直接从项目仓库复制内容：https://你的仓库地址/blob/master/Jenkinsfile.offline

保存。

**流水线说明**（4 个阶段，对比有网的 6 阶段）：

```
Verify Images → Prepare → Deploy → Health Check
```

| 阶段 | 做什么 |
|---|---|
| Verify Images | 检查 `silverwing/*:1.0.1` 镜像是否已 load，缺失则报错 |
| Prepare | 检查部署目录的 docker-compose.yml 存在，更新 .env 的版本号 |
| Deploy | `docker compose up -d --force-recreate` 重建容器 |
| Health Check | 轮询检查容器状态（最多 3 分钟），失败打印日志 |

### ⑩ 一键部署微服务

任务页 → **Build with Parameters**：

| 参数 | 值 | 说明 |
|---|---|---|
| `BUILD_VERSION` | `1.0.1` | 与已 load 的镜像版本一致 |
| `SERVICES` | （留空） | 空表示全部 8 个微服务 |
| `DEPLOY_DIR` | `/opt/silverwing-deploy` | 默认值（第⑤步已放好配置） |
| `HEALTH_CHECK` | ✓ 勾选 | 部署后健康检查 |

点 Build。

观察 Console Output，4 个阶段依次执行。全部绿色即部署成功。

```bash
# 验证
docker ps --format "table {{.Names}}\t{{.Status}}" | grep silverwing
# 8 个 silverwing-* 容器都是 running/healthy
```

### ⑪ 验证 & 日常运维

**访问验证**：

| 服务 | 地址 |
|---|---|
| 管理后台 | `http://192.168.164.128/admin/` |
| API 文档 | `http://192.168.164.128/doc.html` |
| Nacos 控制台 | `http://192.168.164.128:8848/nacos` |
| RabbitMQ 管理 | `http://192.168.164.128:15672` |
| Jenkins | `http://192.168.164.128:8888` |

**更新流程**（离线场景的核心运维操作）：

```
外网机器                          内网服务器
─────────                        ─────────
改代码
↓
SERVICES=auth BUILD_VERSION=1.0.2
  ./build-microservices.sh       docker load
  → silverwing-microservices-    < 新镜像
    1.0.2.tar.gz                 ↓
↓ 传输                           Jenkins Build with Parameters
                                 BUILD_VERSION=1.0.2
                                 SERVICES=auth
                                 → 自动部署
```

具体步骤：

```bash
# ① 外网：只构建变更的服务
SERVICES=auth BUILD_VERSION=1.0.2 ./build-microservices.sh

# ② 传输 silverwing-microservices-1.0.2.tar.gz 到内网

# ③ 内网：加载新镜像
cd /opt
tar -xzf silverwing-microservices-1.0.2.tar.gz
cd docker-microservices-1.0.2
./load-microservices-images.sh

# ④ Jenkins：Build with Parameters
#    BUILD_VERSION = 1.0.2
#    SERVICES = auth
#    → 点 Build
```

> 如果 compose 文件本身也有变更，需要额外更新部署配置：
> ```bash
> cp docker-microservices-1.0.2/docker-compose.yml /opt/silverwing-deploy/
> cp docker-microservices-1.0.2/.env /opt/silverwing-deploy/
> ```
> 然后再触发 Jenkins 构建。

**回滚**：

```bash
# 方式 1：Jenkins 回滚（旧镜像还在 Docker 里）
#   BUILD_VERSION = 1.0.1
#   SERVICES = auth
#   → Build

# 方式 2：命令行直接回滚
cd /opt/silverwing-deploy
sed -i 's/APP_VERSION=.*/APP_VERSION=1.0.1/' .env
docker compose up -d --force-recreate silverwing-auth
```

**常用命令**：

```bash
# 微服务状态
cd /opt/silverwing-deploy && docker compose ps

# 某服务日志
docker logs -f silverwing-auth

# 重启单个服务
docker restart silverwing-auth

# 看已有镜像版本（回滚用）
docker images silverwing/auth

# 清理 dangling 镜像
docker image prune -f
```

**故障排查**：

| 现象 | 排查 |
|---|---|
| Verify Images 报镜像缺失 | `docker images \| grep silverwing` 确认是否 load 成功 |
| Prepare 阶段报 docker-compose.yml 不存在 | 第⑤步没把配置复制到 `/opt/silverwing-deploy`，补做 |
| 容器起不来 | `docker logs silverwing-xxx`，通常 Nacos 配置缺失或密码不对 |
| Jenkins 无法执行 docker | `docker exec jenkins docker ps` 检查 sock 挂载和组权限 |
| 健康检查超时 | `docker inspect silverwing-xxx --format '{{.State.Health.Status}}'` |

---

## 附录

### 部署完成后的目录结构

```
/opt/
├── silverwing-infra-images/         # 基础设施包（解压后保留，基础设施部署依赖）
│   ├── onepanel-infra-compose.yml
│   ├── onepanel-infra.env
│   ├── scripts/                     # 数据库 SQL
│   ├── docker/                      # Nginx 配置
│   ├── *.tar                        # 基础设施镜像（load 后可删）
│   └── load-infra-images.sh
│
├── docker-microservices-1.0.1/      # 微服务包（解压后保留，更新时复制配置）
│   ├── docker-compose.yml
│   ├── .env
│   ├── silverwing-*.tar             # 微服务镜像（load 后可删）
│   └── load-microservices-images.sh
│
├── silverwing-deploy/               # 微服务部署目录（Jenkins 管理，docker compose 在这执行）
│   ├── docker-compose.yml           # 第⑤步从微服务包复制
│   └── .env                         # 第⑤步从微服务包复制，Jenkins 自动更新版本号
│
├── jenkins-image.tar                # Jenkins 镜像包（load 后可删）
│
└── jenkins/home/                    # Jenkins 数据卷
```

### 容器总览（约 16 个）

```
基础设施 7 个：mysql-server / redis-server / pgvector-server / nacos-standalone
              rabbitmq-server / xxl-job-admin / nginx-server
微服务   8 个：silverwing-gateway / auth / core-service / digital-twin
              ai-service / ops-service / integration / admin-web
工具     1 个：jenkins
```

### 离线 vs 有网场景对照

| 项 | 有网（JENKINS_FULL_DEPLOY.md） | 离线（本文档） |
|---|---|---|
| 外网准备 | 不需要 | 需要（构建 3 个包） |
| 服务器装 JDK/Maven | 需要 | 不需要 |
| 代码托管 Gitea | 需要 | 不需要 |
| Jenkins 插件 | 安装推荐插件 | 不装（Pipeline 自带） |
| Jenkinsfile | `Jenkinsfile`（6 阶段） | `Jenkinsfile.offline`（4 阶段） |
| Jenkins Pipeline 来源 | Pipeline script from SCM | Pipeline script（直接粘贴） |
| 更新方式 | `git push` → Jenkins 自动拉 | 外网构建 → 传包 → load → Jenkins 部署 |

### 相关文档

| 文档 | 何时看 |
|---|---|
| **本文档** | 离线服务器部署 |
| [JENKINS_FULL_DEPLOY.md](./JENKINS_FULL_DEPLOY.md) | 有网服务器部署 |
| [CICD_JENKINS.md](./CICD_JENKINS.md) | Jenkins 配置深入参考 |
| [DOCKER_DEPLOYMENT.md](./DOCKER_DEPLOYMENT.md) | 纯命令行离线部署（不用 Jenkins） |
