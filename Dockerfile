# Multi-stage build for Kotlin/Ktor application

# Stage 1: Build
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy all project files
COPY . ./

# Build application
RUN gradle buildFatJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy JAR from build stage
COPY --from=build /app/build/libs/*-all.jar ./app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
