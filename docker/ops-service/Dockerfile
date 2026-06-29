# 银翼智驭 OPS Service Dockerfile
FROM silverwing/base:1.0.0

LABEL maintainer="silverwing-tech"
LABEL description="SilverWing OPS Service"

WORKDIR /app

COPY target/silverwing-ops-service.jar app.jar

EXPOSE 8085

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
