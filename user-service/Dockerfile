# Stage 1: Build source
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY core/pom.xml core/
COPY auth-service/pom.xml auth-service/
COPY user-service/pom.xml user-service/

RUN mvn dependency:go-offline -B

COPY core/ core/
COPY user-service/ user-service/

# Build module core và user-service
RUN mvn clean package -DskipTests -pl user-service -am

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/user-service/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]