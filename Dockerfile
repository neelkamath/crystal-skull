FROM openjdk:8-jre-alpine

ENV APPLICATION_USER ktor
RUN adduser -Dg '' $APPLICATION_USER

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

USER $APPLICATION_USER

COPY ./build/libs/crystal-skull-all.jar /app/crystal-skull-all.jar
COPY ./build/resources/main/ /app/src/main/resources/
WORKDIR /app

CMD ["java", "-server", "-jar", "crystal-skull-all.jar"]