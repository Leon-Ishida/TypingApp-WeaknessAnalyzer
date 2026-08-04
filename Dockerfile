FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /TypingApplication

COPY pom.xml .
COPY src ./src

RUN mvn clean package

FROM eclipse-temurin:21-jre

WORKDIR /TypingApplication

COPY --from=build /TypingApplication/target/*.jar TypingApp.jar

ENTRYPOINT ["java", "-jar", "TypingApp.jar"]

EXPOSE 8080