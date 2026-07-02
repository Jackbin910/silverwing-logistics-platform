# 银翼智驭 - Jenkins 同机部署完整流程

> **场景**：一台全新服务器，上面没有 Jenkins。本文档带你从零开始——安装 Jenkins，并用它把银翼微服务平台部署到**同一台机器**上。
>
> Jenkins 与微服务、基础设施全部跑在同一台服务器，Jenkins 构建完镜像直接在本机 `docker compose up`，不依赖 Harbor、不依赖远程 SSH。

---

## 流程总览

```
① 服务器初始化      装 Docker / JDK17 / Maven / Git
       ↓
② 基础设施部署       起 MySQL/Redis/Nacos/RabbitMQ/Nginx（微服务依赖）
       ↓
③ Nacos 配置初始化   建命名空间、导入公共配置
       ↓
④ 代码托管           起 Gitea（同机 Git），推代码上去
       ↓
⑤ 安装 Jenkins       docker run 起 Jenkins，挂载 docker.sock
       ↓
⑥ 配置 Jenkins       装 JDK/Maven 工具、配 Git 凭据
       ↓
⑦ 创建流水线         Pipeline from SCM，指向仓库的 Jenkinsfile
       ↓
⑧ 一键部署           Build with Parameters → 自动构建+部署+健康检查
       ↓
⑨ 验证运维           访问后台、更新、回滚、看日志
```

**全文约定**（按需替换为你的实际值）：

| 项 | 值 | 说明 |
|---|---|---|
| 服务器 IP | `192.168.164.128` | 示例，替换为实际 IP |
| SSH 用户 | `root` | 或有 sudo 权限的用户 |
| 操作系统 | Ubuntu 22.04 | CentOS 把 `apt` 换 `yum/dnf` |
| 代码分支 | `master` | 项目默认分支 |
| 镜像版本 | `1.0.1` | 与 `onepanel-services.env` 的 `APP_VERSION` 一致 |
| Jenkins 端口 | `8888` | 宿主机映射端口 |
| Gitea 端口 | `3000` / `222` | Web / SSH |

---

## ① 服务器初始化

SSH 登录服务器后执行。

### 1.1 装基础工具

```bash
apt update && apt upgrade -y
apt install -y curl wget vim git unzip ca-certificates
```

### 1.2 装 Docker

```bash
curl -fsSL https://get.docker.com | sh
systemctl enable --now docker
docker version && docker compose version
```

**配镜像加速**（国内必做，否则拉镜像超时）：

```bash
mkdir -p /etc/docker
cat > /etc/docker/daemon.json <<'EOF'
{
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.m.daocloud.io"
  ],
  "log-driver": "json-file",
  "log-opts": { "max-size": "100m", "max-file": "3" }
}
EOF
systemctl restart docker
```

### 1.3 装 JDK 17

Jenkins 编译微服务要用，宿主机也要有。

```bash
apt install -y openjdk-17-jdk
java -version
# 记下 JAVA_HOME 路径，后面挂载给 Jenkins
readlink -f $(which java) | sed 's:/bin/java::'
# 典型输出：/usr/lib/jvm/java-17-openjdk-amd64
```

### 1.4 装 Maven

```bash
cd /opt
wget https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.tar.gz
tar -xzf apache-maven-3.9.16-bin.tar.gz
ln -s /opt/apache-maven-3.9.16 /opt/maven
rm apache-maven-3.9.16-bin.tar.gz
```

**配阿里云镜像源**（关键，否则下依赖超时）：

```bash
cat > /opt/maven/conf/settings.xml <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0">
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Public</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
EOF
```

```bash
# 配环境变量
cat >> /etc/profile.d/maven.sh <<'EOF'
export MAVEN_HOME=/opt/maven
export PATH=$MAVEN_HOME/bin:$PATH
EOF
source /etc/profile.d/maven.sh
mvn -version
```

### 1.5 验证

```bash
docker version && docker compose version && java -version && mvn -version && git --version
```

全部正常即进入下一步。

---

## ② 基础设施部署

Jenkins 流水线**只负责 8 个微服务**，基础设施（MySQL/Redis/Nacos/RabbitMQ/Nginx 等）要提前跑起来并保持不动。

### 2.1 把项目源码放到服务器

基础设施 compose 引用了项目里的 `scripts/`、`docker/nginx-conf/`，需要先有源码。先从开发机 scp 上来：

```bash
# 在 Windows 开发机执行（PowerShell / Git Bash）
scp -r d:/workspace/silverwing-logistics-platform root@192.168.164.128:/opt/silverwing-src
```

### 2.2 创建 Docker 网络

基础设施和微服务共享一个外部网络：

```bash
docker network create silverwing-network
```

### 2.3 启动基础设施

```bash
cd /opt/silverwing-src

# 改密码（生产务必改，当前都是 123456）
vi onepanel-infra.env
# 重点改：MYSQL_ROOT_PASSWORD / NACOS_SERVICE_PASSWORD / RABBITMQ_PASSWORD 等

# 启动
docker compose -f onepanel-infra-compose.yml --env-file onepanel-infra.env up -d

# 等待全部 healthy（约 1-2 分钟）
watch -n2 'docker ps --format "table {{.Names}}\t{{.Status}}" | grep -E "mysql|redis|nacos|rabbitmq|nginx|xxl"'
```

### 2.4 验证

```bash
curl -s http://localhost:8848/nacos/v1/console/health/readiness   # Nacos：OK
docker exec mysql-server mysql -uroot -p123456 -e "SHOW DATABASES;" # MySQL
docker exec redis-server redis-cli ping                            # Redis：PONG
```

---

## ③ Nacos 配置初始化

**最容易漏的一步**。微服务启动后从 Nacos 拉 MySQL/Redis/RabbitMQ 连接信息，没配置会启动失败。

### 3.1 创建命名空间

浏览器打开 `http://192.168.164.128:8848/nacos`，登录（`nacos` / `onepanel-infra.env` 里的密码）：

1. 命名空间 → 新建
2. 命名空间 ID：**`b7273740-ffae-4c91-9e78-ba1360e42a0e`**（必须和 `onepanel-services.env` 的 `NACOS_NAMESPACE` 完全一致）
3. 命名空间名：`银翼生产环境`

### 3.2 导入公共配置

切换到该命名空间 → 配置管理 → 配置列表 → 导入配置。

至少创建这些 Data ID（Group 用 `DEFAULT_GROUP`）：

| Data ID | 内容 |
|---|---|
| `common-mysql.yml` | MySQL 连接，host=`mysql`、port=3306 |
| `common-redis.yml` | Redis 连接，host=`redis`、port=6379 |
| `common-pgvector.yml` | PGVector 连接，host=`pgvector`（AI 服务用） |
| `common-rabbitmq.yml` | RabbitMQ 连接，host=`rabbitmq`、port=5672 |
| `silverwing-gateway.yml` | 网关路由规则 |
| `silverwing-auth.yml` | Auth 服务专属配置 |
| `silverwing-core-service.yml` | 核心服务专属配置 |
| `silverwing-ai-service.yml` | AI 服务专属（含 Ollama 地址） |
| ... | 其余微服务按需创建 |

> **关键**：配置里的 host 必须用**容器名**（`mysql`/`redis`/`nacos`/`rabbitmq`/`pgvector`），因为微服务和基础设施在同一 Docker 网络。

---

## ④ 代码托管（Gitea）

Jenkins 需要从 Git 仓库拉代码。同机用 Gitea 最省事（单容器）。

### 4.1 启动 Gitea

```bash
mkdir -p /opt/gitea/data

docker run -d \
  --name gitea \
  --restart always \
  -p 3000:3000 -p 222:22 \
  -v /opt/gitea/data:/data \
  --network silverwing-network \
  docker.1ms.run/gitea/gitea:1.22
```

### 4.2 初始化 Gitea

浏览器打开 `http://192.168.164.128:3000`：

1. 首页 → 安装页，数据库选 SQLite，点安装
2. 创建管理员账号（如 `silverwing` / 自己的密码）
3. 登录 → 新建仓库：`silverwing-logistics-platform`（私有，空仓库）

### 4.3 推代码到 Gitea

开发机（Windows）上：

```bash
cd d:/workspace/silverwing-logistics-platform
git remote add gitea http://192.168.164.128:3000/silverwing/silverwing-logistics-platform.git
git push -u gitea master
```

> 后续改完代码 `git push gitea master`，Jenkins 就能拉到新代码。

### 4.4 生成 Jenkins 访问令牌

Gitea → 右上角头像 → 设置 → 应用 → 生成新令牌：
- 令牌名：`jenkins`
- 权限：勾 `repository`
- 复制 token（只显示一次）

---

## ⑤ 安装 Jenkins

Jenkins 跑在 Docker 里，通过挂载 `docker.sock` 让它能在宿主机构建镜像、跑 compose。

### 5.1 启动 Jenkins 容器

```bash
mkdir -p /opt/jenkins/home

# 获取 docker 组 ID（用于 --group-add）
DOCKER_GID=$(getent group docker | cut -d: -f3)
# 获取 JDK 实际路径（1.3 步的输出）
JDK_PATH=$(readlink -f $(which java) | sed 's:/bin/java::')

docker run -d \
  --name jenkins \
  --restart always \
  --user root \
  --network silverwing-network \
  -p 8888:8080 -p 50000:50000 \
  -v /opt/jenkins/home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v /usr/bin/docker:/usr/bin/docker \
  -v /opt/maven:/opt/maven \
  -v ${JDK_PATH}:/opt/jdk-17 \
  --group-add ${DOCKER_GID} \
  jenkins/jenkins:lts-jdk17
```

**关键挂载说明**：

| 挂载 | 作用 |
|---|---|
| `/var/run/docker.sock` + `/usr/bin/docker` | 让 Jenkins 容器内能执行 docker 命令 |
| `/opt/maven` | 复用宿主机 Maven + 阿里云镜像源 |
| `${JDK_PATH}` → `/opt/jdk-17` | 复用宿主机 JDK17 |
| `--group-add ${DOCKER_GID}` | 确保 jenkins 用户能访问 docker.sock |
| `--network silverwing-network` | 让 Jenkins 能用容器名访问 Gitea（`gitea:3000`） |

### 5.2 验证 Jenkins 能跑 docker

```bash
docker exec jenkins docker ps
# 能列出宿主机所有容器即正常
```

### 5.3 解锁 Jenkins

```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

浏览器打开 `http://192.168.164.128:8888`：
1. 粘贴初始密码 → 继续
2. 选「安装推荐插件」（等几分钟）
3. 创建管理员账号

---

## ⑥ 配置 Jenkins

### 6.1 装插件

Manage Jenkins → Plugins → Available plugins，搜索安装：

- **Pipeline**（通常已装）
- **Git**
- **AnsiColor**（Jenkinsfile 用到，彩色输出）
- **Generic Webhook Trigger**（可选，push 自动触发）

装完重启。

### 6.2 配置全局工具

Manage Jenkins → Global Tool Configuration：

**JDK**：
```
Name:       jdk17
JAVA_HOME:  /opt/jdk-17
去掉 "Install automatically" 勾选
```

**Maven**：
```
Name:        maven3
MAVEN_HOME:  /opt/maven
去掉 "Install automatically" 勾选
```

### 6.3 配置 Git 凭据

Manage Jenkins → Credentials → System → Global → Add：

```
Kind:         Username with password
Username:     silverwing            （Gitea 账号）
Password:     <第4.4步的 token>      ← 用 token，不是登录密码
ID:           gitea-credentials
Description:  Gitea 访问令牌
```

### 6.4 验证工具

新建一个临时 Pipeline 任务，执行：

```groovy
pipeline {
    agent any
    stages {
        stage('Verify') {
            steps {
                sh 'java -version'
                sh 'mvn -version'
                sh 'docker version'
                sh 'git --version'
            }
        }
    }
}
```

全部正常输出即就绪，删掉这个临时任务。

---

## ⑦ 创建流水线任务

### 7.1 新建任务

Jenkins 首页 → New Item：
- 名称：`silverwing-cicd`
- 类型：**Pipeline**
- OK

### 7.2 配置 Pipeline

任务配置页 → 拉到底部 **Pipeline** 区域：

```
Definition:        Pipeline script from SCM
SCM:               Git
Repository URL:    http://gitea:3000/silverwing/silverwing-logistics-platform.git
                   （用容器名 gitea，Jenkins 已加入 silverwing-network）
Credentials:       gitea-credentials
Branch Specifier:  */master
Script Path:       Jenkinsfile
```

保存。

> Repository URL 用 `http://gitea:3000/...` 而非宿主机 IP，因为 Jenkins 和 Gitea 同网络，更快更稳。

### 7.3 确认 Jenkinsfile 在仓库

项目根目录已有 `Jenkinsfile`（同机部署流水线），确认已推到 Gitea：

```bash
docker exec jenkins git ls-remote http://gitea:3000/silverwing/silverwing-logistics-platform.git refs/heads/master
```

---

## ⑧ 一键部署

### 8.1 触发构建

任务页 → **Build with Parameters**：

| 参数 | 值 | 说明 |
|---|---|---|
| `BUILD_VERSION` | `1.0.1` | 与 `onepanel-services.env` 的 `APP_VERSION` 一致 |
| `SERVICES` | （留空） | 空表示全部 8 个微服务 |
| `DEPLOY_AFTER_BUILD` | ✓ 勾选 | 构建后自动部署 |
| `SKIP_MAVEN` | 不勾选 | 首次必须编译 |
| `DEPLOY_DIR` | `/opt/silverwing-deploy` | 默认值 |

点 Build。

### 8.2 观察流水线

点构建编号 → Console Output，6 个阶段依次执行：

```
Checkout → Prepare → Maven Build → Docker Build → Deploy → Health Check
```

**首次构建约 10-20 分钟**（Maven 下依赖慢）。Jenkinsfile 会自动：
1. 检查并构建 `silverwing/base:1.0.0` 基础镜像
2. `mvn clean package -Pdocker -DskipTests` 编译所有微服务
3. 逐个 `docker build` 构建微服务镜像（打版本 tag + latest）
4. 同步 compose 和 env 到 `/opt/silverwing-deploy`
5. `docker compose up -d --force-recreate` 重建容器
6. 轮询健康检查（最多等 3 分钟）

**常见卡点**：

| 卡点 | 原因 | 解决 |
|---|---|---|
| Prepare 阶段 base 镜像构建失败 | 拉 `eclipse-temurin` 慢 | 手工 `docker pull docker.1ms.run/library/eclipse-temurin:17-jre-alpine` 后重试 |
| Maven Build 超时 | 首次下依赖慢 | 重试，Maven 会用本地缓存 |
| Deploy 阶段容器起不来 | Nacos 配置缺失或密码不对 | 回第③步补配置，看 `docker logs silverwing-xxx` |
| Health Check 超时 | 启动慢 | 流水线会自动打印失败容器日志 |

### 8.3 健康检查通过后

```bash
docker ps --format "table {{.Names}}\t{{.Status}}" | grep silverwing
# 应看到 8 个 silverwing-* 容器都是 running/healthy
```

---

## ⑨ 验证 & 日常运维

### 9.1 访问验证

| 服务 | 地址 |
|---|---|
| 管理后台 | `http://192.168.164.128/admin/` |
| API 文档 | `http://192.168.164.128/doc.html` |
| Nacos 控制台 | `http://192.168.164.128:8848/nacos` |
| RabbitMQ 管理 | `http://192.168.164.128:15672` |
| Jenkins | `http://192.168.164.128:8888` |
| Gitea | `http://192.168.164.128:3000` |

```bash
# 端到端：登录接口
curl -X POST http://192.168.164.128/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 9.2 日常更新

开发机改完代码：

```bash
git push gitea master
```

Jenkins → silverwing-cicd → Build with Parameters：
- 只更新变更的服务：`SERVICES=auth`
- 升级版本：`BUILD_VERSION=1.0.2`

点 Build，几分钟即生效。

### 9.3 回滚

**方式 1：Jenkins 回滚旧版本**（镜像还在 Docker 里）

```
BUILD_VERSION = 1.0.0
SKIP_MAVEN = ✓            （跳过编译，直接用旧镜像）
DEPLOY_AFTER_BUILD = ✓
```

**方式 2：命令行直接回滚**

```bash
cd /opt/silverwing-deploy
sed -i 's/APP_VERSION=.*/APP_VERSION=1.0.0/' .env
docker compose up -d --force-recreate
```

### 9.4 常用运维命令

```bash
# 微服务状态
cd /opt/silverwing-deploy && docker compose ps

# 某服务日志
docker logs -f silverwing-auth

# 重启单个服务
docker restart silverwing-auth

# 看历史镜像版本（回滚用）
docker images silverwing/auth

# 清理 dangling 镜像
docker image prune -f
```

### 9.5 故障排查

| 现象 | 排查 |
|---|---|
| Maven 编译失败 | 看 Jenkins Console Output，通常是依赖下载超时 → 检查 settings.xml 阿里云源 |
| 容器起不来 | `docker logs silverwing-xxx`，通常 Nacos 配置缺失或密码不对 |
| Jenkins 无法执行 docker | `docker exec jenkins docker ps` 检查，看 docker.sock 挂载和组权限 |
| 健康检查超时 | `docker inspect silverwing-xxx --format '{{.State.Health.Status}}'` |

---

## 附录：部署完成后的状态

### 目录结构

```
/opt/
├── silverwing-src/              # 项目源码（基础设施 compose 依赖）
├── silverwing-deploy/           # 微服务部署目录（Jenkins 自动管理）
│   ├── docker-compose.yml
│   └── .env
├── jenkins/home/                # Jenkins 数据卷
├── gitea/data/                  # Gitea 数据卷
├── maven/                       # Maven（Jenkins 和宿主机共用）
└── apache-maven-3.9.9/
```

### 容器总览（约 17 个）

```
基础设施 7 个：mysql-server / redis-server / pgvector-server / nacos-standalone
              rabbitmq-server / xxl-job-admin / nginx-server
微服务   8 个：silverwing-gateway / auth / core-service / digital-twin
              ai-service / ops-service / integration / admin-web
工具     2 个：jenkins / gitea
```

### 相关文档

| 文档 | 何时看 |
|---|---|
| **本文档** | 首次部署照着做 |
| [CICD_JENKINS.md](./CICD_JENKINS.md) | Jenkins 配置深入参考 |
| [DOCKER_DEPLOYMENT.md](./DOCKER_DEPLOYMENT.md) | 内网离线包部署 |
| [DEPLOYMENT_EVOLUTION.md](./DEPLOYMENT_EVOLUTION.md) | 单机不够用、要演进集群时 |
