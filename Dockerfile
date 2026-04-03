# Multi-stage build for Mini Marketplace application
# Stage 1: Build application with Maven
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application (skip tests for faster build)
RUN mvn clean package -DskipTests

# Stage 2: Run application with JDK
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /app/target/mini-marketplace-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Health check will be handled by docker-compose service healthcheck

# Run the application - using shell form to enable environment variable expansion
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
