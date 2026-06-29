# 银翼智驭医流综合管理平台 - 部署文档总览

本文档提供银翼智驭平台的部署文档索引和快速导航。

## 部署文档

根据您的部署场景，选择对应的部署文档：

| 部署方式 | 适用场景 | 文档链接 |
|---------|---------|---------|
| **本地开发环境** | 开发调试、本地测试 | [本地部署指南](LOCAL_DEPLOYMENT.md) |
| **Docker 容器化** | 测试环境、生产环境 | [Docker 部署指南](DOCKER_DEPLOYMENT.md) |

---

## 快速选择

### 我要本地开发调试

👉 查看 [本地开发环境部署指南](LOCAL_DEPLOYMENT.md)

**内容包括**：
- 环境搭建（JDK、Maven、MySQL、Redis、Nacos）
- 数据库初始化
- Nacos 配置导入
- 项目构建和启动
- 常见问题排查

### 我要 Docker 容器化部署

👉 查看 [Docker 容器化部署指南](DOCKER_DEPLOYMENT.md)

**内容包括**：
- 基础镜像构建
- 微服务镜像构建
- OnePanel 生产部署（分离式部署）
- 验证部署
- 更新流程
- 故障排查
- 性能优化
- 安全加固

---

## 部署架构概览

```
┌─────────────────────────────────────────────────────────┐
│                  银翼智驭医流综合管理平台                    │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  ┌──────────────────┐      ┌──────────────────┐         │
│  │   基础设施        │      │   微服务         │         │
│  │                  │      │                  │         │
│  │ - MySQL          │      │ - Gateway       │         │
│  │ - Redis          │      │ - Auth          │         │
│  │ - Nacos          │      │ - Core Svc      │         │
│  │ - RabbitMQ       │      │ - Digital Twin  │         │
│  │ - PGVector       │      │ - AI Service    │         │
│  │ - OpenResty      │      │ - Ops Svc       │         │
│  └──────────────────┘      │ - Integration   │         │
│         │                 │ - Admin Web     │         │
│         └─────────────────┴──────────────────┘         │
└─────────────────────────────────────────────────────────┘
```

---

## 微服务列表

| 服务名称 | 端口 | 说明 |
|---------|------|------|
| silverwing-gateway | 8080 | API 网关（路由、限流、认证） |
| silverwing-auth | 8081 | 认证服务（登录、Token 管理） |
| silverwing-core-service | 8082 | 核心服务（订单、物流、库存） |
| silverwing-digital-twin | 8083 | 数字孪生（3D 可视化） |
| silverwing-ai-service | 8084 | AI 服务（NLP、RAG、预测） |
| silverwing-ops-service | 8085 | 运维服务（设备、工单） |
| silverwing-integration | 8086 | 集成服务（第三方对接） |
| silverwing-admin-web | 8087 | 管理后台 |

---

## 相关文档

- [系统架构](ARCHITECTURE.md) - 详细的系统架构说明
- [项目 README](../README.md) - 项目概览和快速开始
- [AI 服务说明](../silverwing-ai-service/README.md) - AI 服务专项文档

---

## 联系方式

如有问题，请联系技术支持：

- **项目地址**：https://github.com/silverwing-tech/silverwing-logistics-platform
- **问题反馈**：https://github.com/silverwing-tech/silverwing-logistics-platform/issues
- **技术支持**：support@silverwing.tech
