# 网关模块 (silverwing-gateway)

## 概述

网关模块是 Silverwing 物流平台的统一入口，负责请求路由、认证授权、限流保护、日志记录等核心功能。

## 核心功能

### 1. 路由转发

基于 Spring Cloud Gateway 实现动态路由，支持：
- 服务发现集成（Nacos）
- 负载均衡（Ribbon）
- 路径重写（StripPrefix）
- 自定义路由规则

**路由配置** (`application.yml`):
```yaml
spring.cloud.gateway.routes:
  - id: core-service
    uri: lb://silverwing-core-service
    predicates:
      - Path=/core/**
    filters:
      - StripPrefix=1
```

### 2. 认证授权

基于 Sa-Token 实现统一的认证授权：
- 使用 `SaReactorFilter` 适配 WebFlux 响应式环境
- 支持白名单路径放行
- 自动验证用户登录状态
- 向下游服务传递用户上下文（X-User-Id）

**配置说明**：
- 放行路径：登录、登出、验证码、健康检查等
- 认证方式：Bearer Token（Authorization Header）
- 用户信息传递：通过请求头传递到下游服务

### 3. 日志记录

记录所有请求的详细信息：
- 请求方法、路径、来源 IP
- 响应状态码、耗时
- 慢请求告警（超过 1 秒）

### 4. 跨域处理

支持 CORS 跨域请求：
- 开发环境：允许所有域名
- 生产环境：通过环境变量 `CORS_ALLOWED_ORIGINS` 配置具体域名
- 支持 Cookie 携带
- 暴露必要的响应头

### 5. 安全响应头

自动添加安全相关的响应头：
- `X-Content-Type-Options`: nosniff
- `X-Frame-Options`: SAMEORIGIN
- `X-XSS-Protection`: 1; mode=block
- `Referrer-Policy`: strict-origin-when-cross-origin

## 过滤器链

```
请求 → LogGlobalFilter (HIGHEST_PRECEDENCE)
     → SaReactorFilter (认证)
     → UserContextFilter (100)
     → 路由转发
     → SecurityHeaderFilter (LOWEST_PRECEDENCE-1)
     → 响应
```

## 配置说明

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| CORS_ALLOWED_ORIGINS | 允许的跨域域名（逗号分隔） | * |
| NACOS_SERVER_ADDR | Nacos 地址 | localhost:8848 |
| NACOS_NAMESPACE | Nacos 命名空间 | prod |
| NACOS_USERNAME | Nacos 用户名 | nacos |
| NACOS_PASSWORD | Nacos 密码 | nacos |

## 监控端点

| 端点 | 说明 |
|------|------|
| /actuator/health | 健康检查 |
| /actuator/info | 应用信息 |
| /actuator/metrics | 监控指标 |
| /actuator/gateway/routes | 路由信息 |
| /actuator/gateway/filters | 过滤器信息 |

## 技术栈

- **Spring Cloud Gateway**: 响应式网关
- **Sa-Token Reactor**: 响应式认证框架
- **Nacos**: 服务发现与配置中心
- **Spring Boot 3**: 基于 Spring Boot 3

## 架构说明

### 为什么 Gateway 不依赖 common 模块？

- Gateway 使用 **WebFlux（响应式）** 编程模型
- Common 模块使用 **Servlet（传统）** 编程模型
- Sa-Token 在两种模型下需要不同的配置：
  - Gateway: `SaReactorFilter`
  - Services: `SaInterceptor`
- 依赖会导致配置冲突，因此 Gateway 独立实现认证逻辑

### 异常处理

- **Gateway 层**: `GlobalExceptionHandler` 处理 WebFlux 异常
- **服务层**: 各微服务依赖 common 模块的 `GlobalExceptionHandler` 处理 Servlet 异常

## 部署说明

### Docker 部署

```bash
# 构建镜像
docker build -t silverwing-gateway:latest .

# 运行容器
docker run -d \
  -p 8080:8080 \
  -e NACOS_SERVER_ADDR=192.168.1.100:8848 \
  -e CORS_ALLOWED_ORIGINS=https://example.com,https://admin.example.com \
  silverwing-gateway:latest
```

### K8s 部署

参考 `k8s/gateway-deployment.yaml`

## 注意事项

1. **生产环境 CORS 配置**: 必须设置 `CORS_ALLOWED_ORIGINS` 为具体域名，不要使用 `*`
2. **超时配置**: 不同服务的超时时间需要根据实际情况调整
3. **日志级别**: 生产环境建议使用 `WARN` 或 `ERROR`
