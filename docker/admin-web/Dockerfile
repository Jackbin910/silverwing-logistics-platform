# 银翼智驭 Admin Web Service Dockerfile
FROM silverwing/base:1.0.0

LABEL maintainer="silverwing-tech"
LABEL description="SilverWing Admin Web Service"

WORKDIR /app

COPY target/silverwing-admin-web.jar app.jar

EXPOSE 8087

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
