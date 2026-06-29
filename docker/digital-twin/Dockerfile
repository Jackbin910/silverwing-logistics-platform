# 银翼智驭 Digital Twin Service Dockerfile
FROM silverwing/base:1.0.0

LABEL maintainer="silverwing-tech"
LABEL description="SilverWing Digital Twin Service"

WORKDIR /app

COPY target/silverwing-digital-twin.jar app.jar

EXPOSE 8083

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
