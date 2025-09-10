FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/stage-42-0.0.1-SNAPSHOT.jar app.jar
ENV PORT=10000
EXPOSE $PORT
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=$PORT"]
