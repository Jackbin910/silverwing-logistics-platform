# 银翼智驭医流综合管理平台

面向医院的智能物流综合管理系统，通过数字孪生、AI 智能分析、统一调度等技术，实现医院物资配送、设备管理、运营决策的智能化。

## 核心功能

- **手术物资申领与配送** - 语音下单、智能分拣、自动出库、配送追踪、自动签收
- **全院物流设备预测性维护** - IoT 异常检测、AI 根因分析、自动工单、备件管理
- **多系统协同批量配送** - HRP/SPD 需求整合、智能分单、动态路径规划、闭环签收
- **院长驾驶舱与决策支持** - 多维度数据聚合、3D 数字孪生可视化、智能洞察

## 技术栈

| 层级 | 技术 | 版本                   |
|------|------|----------------------|
| 框架 | Spring Boot + Spring Cloud + Spring Cloud Alibaba | 3.5.0 / 2025.0.0     |
| 网关 | Spring Cloud Gateway + Sa-Token | - / 1.40.0           |
| 数据库 | MySQL + MyBatis-Plus + Druid | 8.0 / 3.5.9 / 1.2.23 |
| 缓存 | Redis + Redisson | 7.0 / 3.32.0         |
| 注册/配置 | Nacos | 2.4.3                |
| 消息队列 | RabbitMQ | 3.13                 |
| 任务调度 | XXL-Job | 2.4.2                |
| AI | LangChain4j + Qwen2.5 7B + BGE-small-zh-v15-Q + PGVector | 1.12.2               |
| API 文档 | Knife4j (OpenAPI 3.0) | 4.5.0                |

## 模块结构

```
silverwing-logistics-platform/
├── silverwing-common          # 公共模块（异常处理、工具类、基础配置）
├── silverwing-gateway         # API 网关（8080，路由/鉴权/限流/日志审计）
├── silverwing-auth            # 认证授权（8081）
├── silverwing-core-service    # 核心业务（8082，订单/配送/库存）
├── silverwing-digital-twin    # 数字孪生（8083，可视化/实时态势）
├── silverwing-ai-service      # AI 分析（8084，预测维护/RAG 问答）
├── silverwing-ops-service     # 运维服务（8085，设备/工单/备件）
├── silverwing-integration     # 集成服务（8086，IoT/HRP/SPD 对接）
├── silverwing-admin-web       # 管理后台（8087，报表统计）
├── docker/                    # 各服务 Dockerfile
├── scripts/                   # 数据库初始化 SQL
└── nacos-config-templates/    # Nacos 配置中心模板
```

## 部署

### 部署文档

根据您的需求选择对应的部署方式：

| 部署方式 | 适用场景 | 文档链接 |
|---------|---------|---------|
| **本地开发环境** | 开发调试、本地测试 | [本地部署指南](docs/LOCAL_DEPLOYMENT.md) 📖 |
| **Docker 容器化** | 测试环境、生产环境 | [Docker 部署指南](docs/DOCKER_DEPLOYMENT.md) 🐳 |

### 快速开始

#### 本地开发环境

```bash
# 1. 初始化数据库
mysql -u root -p < scripts/init.sql

# 2. 启动基础设施（MySQL/Redis/Nacos）
docker compose -f onepanel-infra-compose.yml up -d mysql redis nacos

# 3. 在 Nacos 导入配置模板（nacos-config-templates/ 目录）

# 4. 构建并启动
mvn clean install -DskipTests
java -jar silverwing-gateway/target/silverwing-gateway.jar
```

详细步骤请参阅：[本地部署指南](docs/LOCAL_DEPLOYMENT.md)

#### Docker 容器化部署

```bash
# 预拉取基础镜像（加速部署）
./scripts/pre-pull-images.sh

# 使用一键部署配置
docker compose -f onepanel-docker-compose.yml up -d
```

详细步骤请参阅：[Docker 部署指南](docs/DOCKER_DEPLOYMENT.md)

### 微服务列表

| 服务名称 | 端口 | 说明 |
|---------|------|------|
| silverwing-gateway | 8080 | API 网关 |
| silverwing-auth | 8081 | 认证服务 |
| silverwing-core-service | 8082 | 核心服务 |
| silverwing-digital-twin | 8083 | 数字孪生 |
| silverwing-ai-service | 8084 | AI 服务 |
| silverwing-ops-service | 8085 | 运维服务 |
| silverwing-integration | 8086 | 集成服务 |
| silverwing-admin-web | 8087 | 管理后台 |

## 接口文档

启动后访问：`http://localhost:8080/doc.html`

## 许可证

Copyright (c) 2024 Silverwing Technology Co., Ltd.
