FROM openjdk:11 AS builder
WORKDIR /app
COPY gradle/ gradle/
COPY src/main/ src/main/
COPY build.gradle.kts gradle.properties gradlew settings.gradle.kts ./
RUN ./gradlew shadowJar

FROM azul/zulu-openjdk-alpine:11-jre
RUN apk --no-cache add curl
COPY --from=builder /app/build/libs/crystal-skull-all.jar crystal-skull-all.jar
COPY --from=builder /app/build/resources/main/ src/main/resources/
ENV PORT 80
EXPOSE 80
HEALTHCHECK --timeout=5s --start-period=5s --retries=1 \
    CMD curl -f http://localhost:$PORT/health_check
CMD ["java", "-server", "-jar", "crystal-skull-all.jar"]