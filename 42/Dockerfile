# Étape 1 : Build avec Maven
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Exécuter avec JDK slim
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/stage-42-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=$PORT
EXPOSE $PORT
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=${PORT}"]
