FROM openjdk:8-jre-alpine

ENV APPLICATION_USER ktor
RUN adduser -D -g '' $APPLICATION_USER

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

USER $APPLICATION_USER

COPY ./build/libs/crystal-skull-all.jar /app/crystal-skull-all.jar
WORKDIR /app

CMD ["java", "-server", "-jar", "crystal-skull-all.jar"]