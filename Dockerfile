# --- Stage 1: Build the application ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and project description files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src src

# Build the jar file (skipping tests for faster deployment)
RUN ./mvnw package -DskipTests

# --- Stage 2: Run the application ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from the first stage
COPY --from=build /app/target/*.jar app.jar

# Spring Boot runs on port 8080 by default
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]