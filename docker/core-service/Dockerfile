# 银翼智驭 Core Service Dockerfile
FROM silverwing/base:1.0.0

LABEL maintainer="silverwing-tech"
LABEL description="SilverWing Core Service"

WORKDIR /app

COPY target/silverwing-core-service.jar app.jar

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
