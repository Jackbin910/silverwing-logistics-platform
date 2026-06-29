# 银翼基础镜像
FROM docker.1ms.run/library/eclipse-temurin:17-jre-alpine

LABEL maintainer="silverwing-tech"
LABEL description="SilverWing Base Image with common dependencies"
LABEL version="1.0.0"

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 安装必要的工具
USER root
RUN apk add --no-cache curl tzdata

# 创建应用目录
WORKDIR /app

# 创建日志目录
RUN mkdir -p /app/logs && chmod 777 /app/logs

# 健康检查端口（默认）
EXPOSE 8080

# JVM 通用参数
ENV JAVA_OPTS="-Xms512m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heap_dump.hprof"

# 验证 Java 安装（构建时测试）
RUN java -version && echo "✅ Java installed successfully"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 默认命令（用于测试）
CMD ["java", "--version"]
