# 银翼智驭 Integration Service Dockerfile
FROM silverwing/base:1.0.0

LABEL maintainer="silverwing-tech"
LABEL description="SilverWing Integration Service"

WORKDIR /app

COPY target/silverwing-integration.jar app.jar

EXPOSE 8086

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
