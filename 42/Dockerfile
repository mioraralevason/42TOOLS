# Stage 1: Build
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/stage-42-0.0.1-SNAPSHOT.jar app.jar

# Render utilise la variable d'environnement PORT
EXPOSE ${PORT:-9090}

# Utiliser la variable PORT fournie par Render
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-9090} -jar app.jar"]