# Stage 1: build
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src
RUN ./mvnw clean package -DskipTests

# Stage 2: runtime
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/stage-42-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]  