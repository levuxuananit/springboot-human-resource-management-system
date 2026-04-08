FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
# Copy file jar build from target folder to image
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]