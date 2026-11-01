# --- Stage 1: Build the application using Maven image ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# Download dependencies (cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src src

# Build the jar file (skipping tests)
RUN mvn package -DskipTests

# --- Stage 2: Run the application ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from the first stage
COPY --from=build /app/target/*.jar app.jar

# Spring Boot runs on port 8080 by default
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
