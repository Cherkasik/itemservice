FROM gradle:4.7.0-jdk8-alpine

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
CMD [ "./gradlew", "build" ]
