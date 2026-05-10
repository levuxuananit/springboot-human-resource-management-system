# Stage 1: Build source
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy các file POM để cache dependencies
COPY pom.xml .
COPY core/pom.xml core/
COPY auth-service/pom.xml auth-service/
COPY user-service/pom.xml user-service/

# Download dependencies (tận dụng Docker layer cache)
RUN mvn dependency:go-offline -B

# Copy mã nguồn các module cần thiết
COPY core/ core/
COPY auth-service/ auth-service/

# Build module core và auth-service
RUN mvn clean package -DskipTests -pl auth-service -am

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Lấy file JAR từ build stage
COPY --from=build /app/auth-service/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]