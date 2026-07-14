# 前端对接约定

> 本文档面向前端开发人员，描述 SilverWing 智慧物流平台的 API 路由规则、认证机制、统一响应格式、JSON 序列化约定、分页规范与错误处理，供前端联调参考。

---

## 1. 请求路由

### 1.1 统一入口

所有 API 请求统一走 Nginx → Gateway → 微服务链路，对外地址格式：

```
/api/{servicePrefix}/{业务路径}
```

`{servicePrefix}` 为服务前缀，固定取值如下：

| 服务前缀 | 目标服务 | 说明 |
| --- | --- | --- |
| `auth` | 认证服务 | 登录、登出、用户信息、权限刷新 |
| `core` | 核心业务服务 | 订单、设备、任务等核心业务 |
| `twin` | 数字孪生服务 | 3D 可视化、孪生数据 |
| `ai` | AI 服务 | 智能问答、RAG 检索 |
| `ops` | 运维服务 | 运维管理 |
| `integration` | 集成服务 | 第三方系统对接 |
| `admin` | 管理后台 | 系统配置、用户管理 |

### 1.2 路径示例

| 场景 | 前端请求地址 |
| --- | --- |
| 用户登录 | `POST /api/auth/login` |
| 获取当前用户信息 | `GET /api/auth/userInfo` |
| 用户登出 | `POST /api/auth/logout` |
| AI 智能问答 | `POST /api/ai/database-rag/query` |
| 核心业务订单列表 | `GET /api/core/order/list` |
| 管理后台用户列表 | `GET /api/admin/user/list` |

> **注意**：前端始终使用 `/api/` 前缀，无需关心微服务内部路径。

---

## 2. 认证机制

### 2.1 登录流程

登录采用 **RSA 非对称加密** 保护密码传输。公钥硬编码在前端代码中，不通过接口获取。

```
1. 前端用硬编码的 RSA 公钥加密明文密码    → JSEncrypt
2. 前端 POST /api/auth/login              → 提交加密后的密码
3. 后端 RSA 私钥解密 → BCrypt 校验 → Sa-Token 签发 → 返回 Token
```

**公钥硬编码**

RSA 公钥直接写在前端常量中（公钥本身就是公开的，硬编码无安全风险）：

```js
// src/utils/crypto.js
import JSEncrypt from 'jsencrypt'

const RSA_PUBLIC_KEY = 'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...'

export function encryptPassword(password) {
  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(RSA_PUBLIC_KEY)
  return encryptor.encrypt(password)
}
```

**提交登录**

```
POST /api/auth/login

{
    "username": "admin",
    "password": "RSA加密后的Base64字符串"
}
```

> `password` 字段必须传 RSA 加密后的密文，后端不接受明文密码。

成功后返回：

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "token": "xxxx-xxxx-xxxx",
        "username": "admin",
        "roles": ["ADMIN"]
    },
    "timestamp": 1720857600000,
    "traceId": "a1b2c3d4"
}
```

前端将 `token` 存储到 localStorage，后续请求需在 Header 中携带。

### 2.2 Token 传递

后续所有请求需在请求头中携带 Token：

```
satoken: {token值}
```

> Sa-Token 默认从名为 `satoken` 的请求头或 Cookie 中读取 Token，无需 `Bearer` 前缀。

### 2.3 白名单接口

以下路径无需登录即可访问：

- `GET /api/auth/public-key`（获取 RSA 公钥）
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `/api/doc.html`、`/api/webjars/**`、`/api/v3/api-docs/**`（接口文档）
- `/api/actuator/**`（健康检查）

### 2.4 Token 过期处理

当 Token 过期或无效时，返回 `401` 状态码：

```json
{
    "code": 401,
    "message": "登录已过期，请重新登录"
}
```

前端收到 `401` 后应清除本地 Token 并跳转登录页。

### 2.5 权限不足处理

当用户无权限访问某接口时，返回 `403`：

```json
{
    "code": 403,
    "message": "无操作权限"
}
```

---

## 3. 统一响应格式

### 3.1 响应结构

所有接口统一返回以下 JSON 结构：

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {},
    "timestamp": 1720857600000,
    "traceId": "a1b2c3d4"
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | `Integer` | 状态码，`200` 表示成功，其他为错误码 |
| `message` | `String` | 提示消息，可直接展示给用户 |
| `data` | `Object` | 业务数据，成功时返回，失败时可能为 `null` |
| `timestamp` | `Long` | 服务器时间戳（毫秒） |
| `traceId` | `String` | 链路追踪 ID，排查问题时提供此值 |

### 3.2 成功判断

前端统一通过 `code === 200` 判断请求是否成功，不要依赖 HTTP Status Code（HTTP 层始终返回 200，业务状态码在 `code` 字段中）。

---

## 4. 状态码定义

### 4.1 通用状态码

| code | 含义 | 前端处理建议 |
| --- | --- | --- |
| `200` | 操作成功 | 正常处理 `data` |
| `400` | 请求参数错误 | 提示 `message` |
| `401` | 未登录 / 登录已过期 | 清除 Token，跳转登录页 |
| `403` | 无操作权限 | 提示无权限 |
| `404` | 资源不存在 | 提示资源不存在 |
| `405` | 请求方法不支持 | 检查请求方法是否正确 |
| `500` | 系统繁忙 | 提示"系统繁忙，请稍后重试" |
| `503` | 服务不可用 | 提示服务暂不可用 |

### 4.2 业务状态码（1xxx）

| code | 含义 |
| --- | --- |
| `1000` | 业务处理失败 |
| `1001` | 参数校验失败（`message` 包含具体字段校验信息） |
| `1002` | 数据不存在 |
| `1003` | 数据已存在 |
| `1004` | 数据状态非法 |

> 参数校验失败时，`message` 格式为 `字段名: 错误信息; 字段名: 错误信息`，前端可直接展示或解析后定位到对应表单项。

---

## 5. JSON 序列化约定

平台使用 **FastJSON2** 作为统一的 JSON 序列化框架，前后端需遵守以下约定。

### 5.1 日期格式

所有日期/时间字段统一格式化为字符串：

```
yyyy-MM-dd HH:mm:ss
```

示例：

```json
{
    "createTime": "2026-07-13 14:30:00",
    "updateTime": "2026-07-13 15:00:00"
}
```

前端提交日期参数时也使用此格式。

### 5.2 字符集

统一使用 **UTF-8** 编码，请求和响应的 `Content-Type` 应为：

```
Content-Type: application/json;charset=UTF-8
```

### 5.3 Null 值处理

后端序列化时**保留 null 字段**并按类型输出默认值，前端无需做 `undefined` 兜底：

| Java 类型 | null 时 JSON 输出 | 说明 |
| --- | --- | --- |
| `String` | `""` | 空字符串 |
| `List` / `Array` | `[]` | 空数组 |
| `Number`（Integer/Long/Double...） | `0` | 零值 |
| `Boolean` | `false` | false |
| 其他对象 | `null` | null |

示例：

```json
{
    "name": "",
    "age": 0,
    "active": false,
    "roles": [],
    "profile": null
}
```

### 5.4 字段命名

- 后端 Java 字段使用 **驼峰命名**（如 `createTime`）
- FastJSON2 反序列化开启了 **SmartMatch**，前端传下划线（`create_time`）也能匹配
- **推荐前端统一使用驼峰**，保持与响应一致

---

## 6. 分页规范

### 6.1 分页请求参数

分页查询接口统一使用以下参数（Query String 或 Request Body 均可）：

| 参数 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `current` | `Integer` | `1` | 当前页码，从 1 开始 |
| `size` | `Integer` | `10` | 每页条数，最大 500 |
| `sortField` | `String` | - | 排序字段（可选） |
| `sortOrder` | `String` | - | 排序方向：`ASC` / `DESC`（可选） |

请求示例：

```
GET /api/core/order/list?current=1&size=20&sortField=createTime&sortOrder=DESC
```

### 6.2 分页响应结构

分页接口的 `data` 字段统一为：

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "current": 1,
        "size": 20,
        "total": 156,
        "pages": 8,
        "records": [
            { "id": 1, "name": "订单1" },
            { "id": 2, "name": "订单2" }
        ]
    },
    "timestamp": 1720857600000,
    "traceId": "a1b2c3d4"
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `current` | `Long` | 当前页码 |
| `size` | `Long` | 每页条数 |
| `total` | `Long` | 总记录数 |
| `pages` | `Long` | 总页数 |
| `records` | `Array` | 当前页数据列表 |

---

## 7. 跨域（CORS）

Gateway 已全局配置 CORS，前端无需额外处理：

- 开发环境：允许所有域名（`*`）
- 生产环境：通过 `CORS_ALLOWED_ORIGINS` 环境变量指定允许的域名
- 允许携带 Cookie（`allowCredentials: true`）
- 预检请求缓存 2 小时

---

## 8. 国际化

后端支持多语言，前端可通过请求头指定语言：

```
Accept-Language: zh-CN
```

支持的值：

- `zh-CN` — 简体中文（默认）
- `en-US` — English

后端异常消息会根据 `Accept-Language` 自动翻译为对应语言。

---

## 9. 接口文档

开发环境下可访问 Knife4j 接口文档：

```
http://{host}/api/doc.html
```

文档包含所有接口的请求参数、响应结构、字段说明，支持在线调试。

---

## 10. 前端 HTTP 拦截器参考

以下为 Axios 请求/响应拦截器参考实现：

```javascript
import axios from 'axios';
import JSEncrypt from 'jsencrypt';

const service = axios.create({
    baseURL: '/api',
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json;charset=UTF-8'
    }
});

// 请求拦截器：注入 Token
service.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers['satoken'] = token;
    }
    return config;
});

// 响应拦截器：统一处理业务状态码
service.interceptors.response.use(
    response => {
        const res = response.data;
        if (res.code !== 200) {
            // 401：登录过期，跳转登录页
            if (res.code === 401) {
                localStorage.removeItem('token');
                window.location.href = '/login';
                return Promise.reject(new Error(res.message));
            }
            // 其他错误：展示 message
            console.error(`[traceId=${res.traceId}] ${res.message}`);
            return Promise.reject(new Error(res.message || 'Error'));
        }
        return res;
    },
    error => {
        console.error('请求异常：', error);
        return Promise.reject(error);
    }
);

/**
 * 登录方法（RSA 加密密码）
 */
export async function login(username, password) {
    // 1. 获取 RSA 公钥
    const keyRes = await service.get('/auth/public-key');
    const publicKey = keyRes.data;

    // 2. 用公钥加密明文密码
    const encryptor = new JSEncrypt();
    encryptor.setPublicKey(publicKey);
    const encryptedPassword = encryptor.encrypt(password);

    // 3. 提交登录
    const res = await service.post('/auth/login', {
        username: username,
        password: encryptedPassword
    });

    // 4. 存储 Token
    localStorage.setItem('token', res.data.token);
    return res.data;
}

export default service;
```

---

## 11. 排查指南

接口异常时，请按以下步骤排查：

1. **检查 `traceId`**：响应中的 `traceId` 可帮助后端快速定位日志
2. **检查 `code`**：确认是认证问题（401/403）、参数问题（400/1001）还是服务端问题（500）
3. **检查请求头**：确认 `satoken` 已正确携带
4. **检查请求路径**：确认使用了 `/api/{servicePrefix}/` 前缀
5. **检查日期格式**：提交的日期参数需为 `yyyy-MM-dd HH:mm:ss`
6. **查看接口文档**：访问 `/api/doc.html` 确认接口定义

---

## 12. SSE 流式接口（知识库问答）

知识库问答提供**流式响应**版本，逐 token 推送回答内容，前端可实时展示打字效果。

### 12.1 接口说明

| 项目 | 说明                              |
| --- |---------------------------------|
| 地址 | `POST /api/ai/knowledge/qa/stream` |
| 内部路径 | `/knowledge/qa/stream`          |
| 响应类型 | `text/event-stream`（SSE）        |
| 请求体 | 原始字符串（用户问题，非 JSON）              |

### 12.2 SSE 事件格式

流式响应通过 SSE 推送以下三类事件：

| 事件名 | `data` 内容 | 说明 |
| --- | --- | --- |
| `token` | 文本片段（部分回答） | 每生成一个片段推送一次，前端追加展示 |
| `done` | `[DONE]` | 回答生成完毕 |
| `error` | 错误描述文本 | 处理异常时推送 |

### 12.3 前端消费示例

SSE 流式接口**不能使用普通 Axios 请求**（Axios 会缓冲完整响应），需使用 `fetch` + `ReadableStream` 解析：

```javascript
/**
 * 知识库流式问答
 * @param {string} question 用户问题
 * @param {(token: string) => void} onToken 每收到一个文本片段时回调
 * @param {() => void} onDone 完成时回调
 */
export async function streamKnowledgeQa(question, onToken, onDone) {
    const response = await fetch('/api/ai/knowledge/qa/stream', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'satoken': localStorage.getItem('token')
        },
        body: question // 原始字符串，非 JSON
    });

    if (!response.ok) {
        throw new Error('知识库问答请求失败：' + response.status);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop(); // 保留未完整的行

        let eventName = '';
        for (const line of lines) {
            if (line.startsWith('event:')) {
                eventName = line.slice(6).trim();
            } else if (line.startsWith('data:')) {
                const data = line.slice(5).trim();
                if (eventName === 'token') {
                    onToken(data);
                } else if (eventName === 'done') {
                    onDone();
                } else if (eventName === 'error') {
                    throw new Error(data);
                }
            }
        }
    }
}
```

> **注意**：SSE 流式接口为非阻塞长连接，前端需自行处理超时与断线重连；如需降级，可继续使用原有的非流式接口 `POST /api/ai/knowledge/qa`。
