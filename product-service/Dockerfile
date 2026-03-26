# Multi-stage Dockerfile for Product Service
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY . .
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests -f pom.xml

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/product-service-1.0.0.jar product-service.jar

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
  "-Dspring.profiles.active=production", \
  "-Dserver.port=${SERVER_PORT:8082}", \
  "-Dspring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/ecommerce}", \
  "-Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}", \
  "-Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD:root}", \
  "-Dspring.data.redis.host=${SPRING_REDIS_HOST:localhost}", \
  "-Dspring.data.redis.port=${SPRING_REDIS_PORT:6379}", \
  "-Djwt.secret=${JWT_SECRET}", \
  "-jar", "product-service.jar"]
