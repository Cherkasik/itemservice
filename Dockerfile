FROM gradle:4.10.0-jdk8-alpine

RUN rm -rf /home/gradle/src
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew build
