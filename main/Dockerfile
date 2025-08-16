FROM eclipse-temurin:21-jre-jammy
ARG JAR_FILE=*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]