# 银翼智驭 Gateway 服务 Dockerfile
# 基于国内镜像源的基础镜像，提升构建速度
FROM silverwing/base:1.0.0

LABEL maintainer="silverwing-tech"
LABEL description="SilverWing Gateway Service"

# 设置工作目录
WORKDIR /app

# 复制 JAR 包
COPY target/silverwing-gateway.jar app.jar

# 暴露端口
EXPOSE 8080

# JVM 参数优化 (继承自基础镜像)
# HEALTHCHECK 和 ENTRYPOINT 已配置

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
