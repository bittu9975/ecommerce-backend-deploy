# Multi-stage Dockerfile for API Gateway
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY . .
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests -f pom.xml

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/api-gateway-1.0.0.jar api-gateway.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application with environment variables
ENTRYPOINT ["java", \
  "-Dspring.profiles.active=production", \
  "-Dserver.port=${SERVER_PORT:8080}", \
  "-Dspring.cloud.gateway.routes[0].uri=${AUTH_SERVICE_URL:http://localhost:8081}", \
  "-Dspring.cloud.gateway.routes[1].uri=${PRODUCT_SERVICE_URL:http://localhost:8082}", \
  "-Dspring.cloud.gateway.routes[2].uri=${COMMERCE_SERVICE_URL:http://localhost:8083}", \
  "-Dspring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000}", \
  "-jar", "api-gateway.jar"]
