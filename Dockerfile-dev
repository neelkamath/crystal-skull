FROM openjdk:11
WORKDIR /crystal-skull
COPY gradle/ gradle/
COPY build.gradle.kts gradle.properties gradlew settings.gradle.kts ./
ENV PORT 80
EXPOSE $PORT
# Run <gradle --daemon> to manually start a single daemon to prevent the next command from starting two in parallel.
ENTRYPOINT ./gradlew --daemon; ./gradlew -t assemble & ./gradlew run