FROM openjdk:8 AS builder
WORKDIR /app
COPY gradle/ gradle/
COPY src/main/ src/main/
COPY build.gradle.kts gradle.properties gradlew settings.gradle.kts ./
RUN ./gradlew shadowJar

FROM openjdk:8-jre-alpine
RUN apk --no-cache add curl
COPY --from=builder /app/build/libs/crystal-skull-all.jar crystal-skull-all.jar
COPY --from=builder /app/build/resources/main/ src/main/resources/
ENV PORT 80
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