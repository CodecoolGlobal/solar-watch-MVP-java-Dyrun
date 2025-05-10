FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY pom.xml mvnw ./

COPY .mvn .mvn

RUN ./mvnw dependency:go-offline

COPY ./src ./src

RUN ./mvnw clean package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar /app/solar_watch_app.jar

ENTRYPOINT ["java", "-jar", "/app/solar_watch_app.jar"]