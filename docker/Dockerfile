# Gradle use carriage returns when logging by default, which aren't visible during Docker builds. Since gradle
# invocations take several minutes, it looks like it froze. Hence, we set the log level to info to see the progress.
FROM openjdk:8 AS builder
WORKDIR /app
COPY gradle/ gradle/
COPY build.gradle.kts gradle.properties gradlew settings.gradle.kts ./
# The shadowJar task maximizes cacheable outputs when compared to the build task or the --refresh-dependencies flag.
RUN ./gradlew --no-daemon -i shadowJar
COPY src/main/ src/main/
RUN ./gradlew --no-daemon -i shadowJar

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/crystal-skull-all.jar .
COPY docker/ docker/
# VisualVM uses port 9010.
EXPOSE 80 9010
RUN apk --no-cache add bash curl
HEALTHCHECK --timeout=5s --start-period=5s --retries=1 \
    CMD curl -f http://localhost:80/health_check