FROM openjdk:21-jdk-slim
COPY target/*.jar stats_server.jar
ENTRYPOINT ["java", "-jar", "/stats_server.jar"]