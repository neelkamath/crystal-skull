FROM openjdk:8-jre-alpine
ENV APPLICATION_USER ktor
RUN adduser -Dg '' $APPLICATION_USER
RUN mkdir /app
RUN chown -R $APPLICATION_USER /app
USER $APPLICATION_USER
COPY ./build/libs/crystal-skull-all.jar /app/crystal-skull-all.jar
COPY ./build/resources/main/ /app/src/main/resources/
WORKDIR /app
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