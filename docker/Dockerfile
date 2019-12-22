FROM gradle:6.0.1-jdk8 AS builder
COPY build.gradle.kts gradle.properties settings.gradle.kts ./
COPY src/main/ src/main/
# Gradle commands use carriage returns when logging by default, which aren't visible during Docker builds. Since gradle
# invocations take several minutes, it looks it froze. Hence, we set the log level to info.
RUN gradle --no-daemon -i shadowJar

FROM openjdk:jre-alpine
WORKDIR /app
COPY --from=builder /home/gradle/build/libs/*.jar crystal-skull-all.jar
COPY --from=builder /home/gradle/build/resources/main src/main/resources
COPY wait-for-it.sh .
EXPOSE 80
RUN apk --no-cache add bash curl
HEALTHCHECK --timeout=5s --start-period=5s --retries=1 \
    CMD curl -f http://localhost:80/health_check
RUN adduser -D user
USER user