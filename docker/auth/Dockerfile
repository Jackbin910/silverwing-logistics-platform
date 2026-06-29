# 银翼智驭 Auth 服务 Dockerfile
FROM silverwing/base:1.0.0

LABEL maintainer="silverwing-tech"
LABEL description="SilverWing Auth Service"

# 设置工作目录
WORKDIR /app

# 复制 JAR 包
COPY target/silverwing-auth.jar app.jar

# 暴露端口
EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
