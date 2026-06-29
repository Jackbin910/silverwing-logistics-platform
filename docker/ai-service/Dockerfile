# 银翼智驭 AI Service Dockerfile
FROM silverwing/base:1.0.0

LABEL maintainer="silverwing-tech"
LABEL description="SilverWing AI Service"

WORKDIR /app

COPY target/silverwing-ai-service.jar app.jar

EXPOSE 8084

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
