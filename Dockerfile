# ==============================================================================
# Multi-Stage Dockerfile for Mio Wealth
# Stage 1: Build Angular Frontend (imui)
# Stage 2: Build Spring Boot Backend (server) with embedded Angular assets
# Stage 3: Minimal, Hardened Production JRE Runtime
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build Angular Frontend
# ------------------------------------------------------------------------------
FROM node:22-alpine AS frontend-builder
WORKDIR /app/imui

# Cache and install dependencies
COPY imui/package*.json ./
RUN npm ci

# Copy Angular source and compile production bundle
COPY imui/ ./
RUN npm run build

# ------------------------------------------------------------------------------
# Stage 2: Build Spring Boot Backend
# ------------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21-alpine AS backend-builder
WORKDIR /app/server

# Cache Maven dependencies
COPY server/pom.xml ./
RUN mvn dependency:go-offline -B

# Copy server sources
COPY server/src ./src

# Embed compiled Angular static assets from Stage 1 into Spring Boot static resources
COPY --from=frontend-builder /app/imui/dist/imui/browser/ ./src/main/resources/static/

# Package standalone executable fat JAR
RUN mvn clean package -DskipTests

# ------------------------------------------------------------------------------
# Stage 3: Minimal, Hardened Production JRE Runtime
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: Run as dedicated non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy executable JAR from builder stage
COPY --from=backend-builder /app/server/target/*.jar app.jar

# Enforce secure file permissions
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

# Production JVM optimizations for container environments
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
