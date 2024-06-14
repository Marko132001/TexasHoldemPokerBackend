FROM gradle:7-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM openjdk:11
EXPOSE 80:80
EXPOSE 443:443
RUN mkdir /app
#COPY /src/main/resources/pokerapp-8f562-firebase-adminsdk-7n239-13a746135f.json /app/
COPY --from=build /home/gradle/src/build/libs/*.jar /app/poker-app_backend.jar
ENTRYPOINT ["java","-jar","/app/poker-app_backend.jar"]