# Caches dependencies so that subsequent Docker builds only redownload dependencies if the build files change.
FROM gradle:6.0.1-jre8 AS cache
COPY build.gradle.kts gradle.properties settings.gradle.kts ./
RUN gradle --no-daemon --refresh-dependencies

FROM gradle:6.0.1-jdk8 AS builder
COPY --from=cache /home/gradle/.gradle /home/gradle/.gradle
COPY . .
RUN gradle --no-daemon shadowJar

FROM openjdk:jre-alpine
RUN apk --no-cache add curl
COPY --from=builder /home/gradle/build/libs/*.jar crystal-skull-all.jar
COPY --from=builder /home/gradle/build/resources/main src/main/resources
EXPOSE 80
HEALTHCHECK --timeout=5s --start-period=5s --retries=1 \
    CMD curl -f http://localhost:$PORT/health_check
CMD [ \
    "java", \
    "-server", \
    "-XX:+UnlockExperimentalVMOptions", \
    "-XX:+UseCGroupMemoryLimitForHeap", \
    "-XX:InitialRAMFraction=2", \
    "-XX:MinRAMFraction=2", \
    "-XX:MaxRAMFraction=2", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=100", \
    "-XX:+UseStringDeduplication", \
    "-jar", \
    "crystal-skull-all.jar" \
]