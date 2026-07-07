# bge-m3 Embedding 模型部署指南

## 概述

将 Embedding 模型从 `bge-small-zh-v1.5-q`（512维，仅中文）升级为 `bge-m3`（1024维，多语言），通过 Ollama 提供 embedding API，无需在 Java 服务侧部署模型文件。

## 前置条件

- Ollama 已安装并运行（当前部署在 192.168.31.84:11434）

## 步骤一：Ollama 拉取 bge-m3 模型

```bash
# 在 Ollama 服务器上（192.168.31.84）
ollama pull bge-m3:latest
```

验证模型可用：

```bash
curl http://192.168.31.84:11434/api/embeddings -d '{
  "model": "bge-m3:latest",
  "prompt": "测试文本"
}'
```

## 步骤二：配置 Nacos

在 Nacos 的 `silverwing-ai-service.yml` 中添加 Embedding 模型配置：

```yaml
langchain4j:
  ollama:
    # ChatModel 配置（如果已配置可忽略）
    chat-model:
      base-url: http://192.168.31.84:11434
      model-name: qwen2.5:7b
    # EmbeddingModel 配置（新增）
    embedding-model:
      base-url: http://192.168.31.84:11434
      model-name: bge-m3:latest
  vector-store:
    pgvector:
      dimension: 1024    # 从 512 改为 1024
```

## 步骤三：重建向量表

向量维度从 512 变为 1024，旧数据不兼容：

```sql
-- 连接 PGVector 数据库执行
DROP TABLE IF EXISTS silverwing_embedding;
```

重启服务后表会自动以 1024 维度重新创建。

## 步骤四：重新导入文档

重启服务后，通过知识库管理接口重新上传所有文档。

## 代码变更清单

| 文件 | 变更内容 |
|---|---|
| `pom.xml`（parent） | 移除 `langchain4j-embeddings-bge-small-zh-v15-q`，不再需要 ONNX 依赖 |
| `silverwing-ai-service/pom.xml` | 同上 |
| `LangChain4jConfig.java` | 移除手动 EmbeddingModel bean，改由 `langchain4j-ollama-spring-boot-starter` 自动配置 |
| `docs/ARCHITECTURE.md` | 文档更新 |
| `silverwing-ai-service/README.md` | 文档更新 |
| `README.md` | 文档更新 |

## 优势

- 无需在 Java 服务侧部署 2.2GB 的 ONNX 模型文件
- 模型由 Ollama 统一管理，升级/切换模型只需改 Nacos 配置
- Ollama 内置 GPU 加速，embedding 性能更好
- 同一台 Ollama 服务器同时提供 ChatModel 和 EmbeddingModel
