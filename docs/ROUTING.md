# 路由说明（分开部署版：Nginx + Gateway）

## 1. 文档目的

本文只保留分开部署版的请求路由说明，描述外部请求如何经过 Nginx、Gateway，最终到达具体微服务。

本文对应的配置文件如下：

- `onepanel-infra-compose.yml`
- `onepanel-services-compose.yml`
- `onepanel-infra.env`
- `onepanel-services.env`
- `docker/nginx-conf/conf/nginx.conf`
- `docker/nginx-conf/conf/conf.d/default.conf`
- `silverwing-gateway/src/main/resources/application.yml`

---

## 2. 整体链路

分开部署版的标准链路如下：

```text
客户端
  -> Nginx
      -> /api/* 转发到 Gateway（去掉 /api/ 前缀）
          -> Gateway 按静态路由转发到具体微服务
      -> /* 转发到 Gateway（原样透传）
          -> Gateway 按静态路由转发到具体微服务
```

也就是说：

- **所有请求统一进入 Gateway**（包括页面和 API）
- API 请求使用 `/api/{servicePrefix}/**` 格式
- 页面请求可直接使用 `/{servicePrefix}/**` 格式
- Gateway 负责按服务前缀转发到具体微服务，并统一处理认证、限流等

---

## 3. 部署结构与端口

分开部署版由两个 Compose 文件组成：

- 基础设施：`onepanel-infra-compose.yml`
- 微服务：`onepanel-services-compose.yml`

### 3.1 Nginx 对外端口

来自 `onepanel-infra.env`：

- `NGINX_HTTP_PORT=80`
- `NGINX_HTTPS_PORT=443`

因此，外部访问入口默认是：

- `http://{host}:80`
- `https://{host}:443`

### 3.2 微服务端口

来自 `onepanel-services.env`：

- Gateway：`8080`
- Auth：`8081`
- Core：`8082`
- Digital Twin：`8083`
- AI：`8084`
- Ops：`8085`
- Integration：`8086`
- Admin Web：`8087`

这些端口主要用于容器内部转发、宿主机映射与排障定位。

---

## 4. Nginx 路由

分开部署版 Nginx 配置由两层组成：

1. 主配置：`docker/nginx-conf/conf/nginx.conf`
2. 入口规则：`docker/nginx-conf/conf/conf.d/default.conf`

其中主配置只负责加载 `conf.d/*.conf`，真正的路由规则在 `default.conf`。

### 4.1 Nginx 入口规则

| 外部路径 | Nginx 转发目标 | 说明 |
| --- | --- | --- |
| `/api/` | `silverwing-gateway:8080` | API 请求，去掉 `/api/` 前缀后转发 |
| `/` | `silverwing-gateway:8080` | 其他请求，原样透传到 Gateway |
| `/health` | Nginx 本地返回 `200 OK` | 健康检查（Nginx 自身） |

### 4.2 Nginx 链路示例

#### API 请求

```text
POST /api/admin/config/update
  -> Nginx（去掉 /api/ 前缀）
  -> Gateway 收到 /admin/config/update
  -> Gateway 匹配 /admin/** 规则（去掉 /admin 前缀）
  -> Admin Web 收到 /config/update
```

#### 其他微服务请求

```text
POST /api/ai/database-rag/query
  -> Nginx（去掉 /api/ 前缀）
  -> Gateway 收到 /ai/database-rag/query
  -> Gateway 匹配 /ai/** 规则（去掉 /ai 前缀）
  -> AI Service 收到 /database-rag/query
```

### 4.3 当前配置说明

当前 `default.conf` 使用的 Gateway upstream 是：

- `silverwing-gateway:8080`

而 `onepanel-services-compose.yml` 中的服务名与容器名是：

- `silverwing-gateway`
- `silverwing-admin-web`
- `silverwing-auth`
- `silverwing-core-service`
- 其他微服务...

所有请求统一通过 Gateway 转发，利用 Nacos 服务发现进行负载均衡。

---

## 5. Gateway 静态路由

Gateway 当前已关闭基于服务发现的自动路由，只保留静态路由。

这意味着：

- 不再暴露按服务名自动生成的路由
- 前端与调用方应只使用约定好的静态前缀
- 整体路由规则更稳定、更易维护

### 5.1 Gateway 路由表

| Gateway 路径前缀 | 目标服务 | 转发方式 |
| --- | --- | --- |
| `/auth/**` | `lb://silverwing-auth` | `StripPrefix=1` |
| `/core/**` | `lb://silverwing-core-service` | `StripPrefix=1` |
| `/twin/**` | `lb://silverwing-digital-twin` | `StripPrefix=1` |
| `/ai/**` | `lb://silverwing-ai-service` | `StripPrefix=1` |
| `/ops/**` | `lb://silverwing-ops-service` | `StripPrefix=1` |
| `/integration/**` | `lb://silverwing-integration` | `StripPrefix=1` |
| `/admin/**` | `lb://silverwing-admin-web` | `StripPrefix=1` |

注意：admin 服务已统一走 Gateway，不再由 Nginx 直连。

### 5.2 `StripPrefix=1` 的含义

以 `POST /api/ai/nlp/chat` 为例：

```text
外部请求:        /api/ai/nlp/chat
Nginx 转给 Gateway: /ai/nlp/chat
Gateway 去前缀后:   /nlp/chat
AI 服务实际收到:    /nlp/chat
```

因此：

- 对外路径应带 `/api/{servicePrefix}`
- 微服务内部 Controller 不需要再写 `/ai`、`/core`、`/twin` 这类网关前缀

---

## 6. 对外访问规范

分开部署版建议统一采用下面的访问规范。

### 6.1 页面入口

- 管理后台页面：`/admin/**` 或 `/api/admin/**`

### 6.2 API 入口

- API：`/api/{servicePrefix}/**`

其中 `{servicePrefix}` 固定为：

- `auth`
- `core`
- `twin`
- `ai`
- `ops`
- `integration`
- `admin`

### 6.3 示例

| 场景 | 对外请求地址 | 服务内部路径 |
| --- | --- | --- |
| 登录 | `POST /api/auth/login` | `/login` |
| 获取用户信息 | `GET /api/auth/userInfo` | `/userInfo` |
| AI 智能问答 | `POST /api/ai/database-rag/query` | `/database-rag/query` |
| 管理后台配置 | `POST /api/admin/config/update` | `/config/update` |
| 管理后台用户列表 | `GET /api/admin/user/list` | `/user/list` |
| 核心业务订单 | `GET /api/core/order/list` | `/order/list` |

---

## 7. 排查顺序

如果出现“请求打不到接口”，建议按下面顺序排查：

1. 确认请求是否走了分开部署版入口
2. 检查 Nginx 是否命中正确的 `location`
3. 检查 Gateway 是否存在对应静态路由
4. 检查服务内部 Controller 路径是否和去前缀后的路径一致
5. 检查容器端口、服务名、网络别名是否一致

---

## 8. 关键文件速查

### Nginx

- 主配置：`docker/nginx-conf/conf/nginx.conf`
- 路由入口：`docker/nginx-conf/conf/conf.d/default.conf`

### Gateway

- 路由配置：`silverwing-gateway/src/main/resources/application.yml`

### Compose / 环境变量

- 基础设施：`onepanel-infra-compose.yml`
- 微服务：`onepanel-services-compose.yml`
- 基础设施端口变量：`onepanel-infra.env`
- 微服务端口变量：`onepanel-services.env`

---

## 9. 当前建议

后续继续维护时，建议始终遵循下面两条规则：

1. 所有 API 对外统一使用 `/api/{servicePrefix}/**`
2. 所有业务 API 统一先进入 Gateway，再由 Gateway 转发到具体服务

这样可以保证：

- 前端联调地址统一
- 鉴权、日志、限流等能力统一收口
- 文档与实际请求链路一致
