# Multi-stage Dockerfile for Auth Service
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY . .
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests -f pom.xml

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/auth-service-1.0.0.jar auth-service.jar

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
  "-Dspring.profiles.active=production", \
  "-Dserver.port=${SERVER_PORT:8081}", \
  "-Dspring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/ecommerce}", \
  "-Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}", \
  "-Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD:root}", \
  "-Djwt.secret=${JWT_SECRET}", \
  "-jar", "auth-service.jar"]
