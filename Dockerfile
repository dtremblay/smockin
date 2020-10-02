# Dockerfile
FROM adoptopenjdk/openjdk8:jre8u242-b08-alpine

ARG APP_VERSION_ARG='2.4.5'
RUN addgroup -g 2345 smockin && adduser -u 2345 -G smockin -D smockin  && \
    mkdir -p /app/db/data && mkdir /app/db/driver && mkdir /app/log && \
    chown -R smockin:smockin /app

COPY install/h2-1.4.194.jar /app/db/driver/h2-1.4.194.jar
COPY target/smockin-${APP_VERSION_ARG}.jar /app/smockin-${APP_VERSION_ARG}.jar
COPY install/launch.sh /app/launch.sh
RUN  chmod +x /app/launch.sh

WORKDIR /app
USER smockin
EXPOSE 8000
EXPOSE 8001
EXPOSE 8002
EXPOSE 8003
EXPOSE 8010
EXPOSE 9092

HEALTHCHECK --interval=5s --timeout=3s CMD curl --fail http://localhost:8001/ || exit 1

ENTRYPOINT ["/app/launch.sh"]
