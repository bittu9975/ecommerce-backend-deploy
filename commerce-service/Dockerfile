# Multi-stage Dockerfile for Commerce Service
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY . .
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests -f pom.xml

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/commerce-service-1.0.0.jar commerce-service.jar

# Expose port
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8083/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
  "-Dspring.profiles.active=production", \
  "-Dserver.port=${SERVER_PORT:8083}", \
  "-Dspring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/ecommerce}", \
  "-Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}", \
  "-Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD:root}", \
  "-Dspring.data.redis.host=${SPRING_REDIS_HOST:localhost}", \
  "-Dspring.data.redis.port=${SPRING_REDIS_PORT:6379}", \
  "-Djwt.secret=${JWT_SECRET}", \
  "-Dproduct-service.url=${PRODUCT_SERVICE_URL:http://localhost:8082}/api/products", \
  "-Dcart.ttl=${CART_TTL:86400}", \
  "-Dcart.max-items=${CART_MAX_ITEMS:50}", \
  "-Dpayment.mock.enabled=${PAYMENT_MOCK_ENABLED:false}", \
  "-Dpayment.mock.success-rate=${PAYMENT_MOCK_SUCCESS_RATE:90}", \
  "-Dnotification.email.from=${NOTIFICATION_EMAIL_FROM:noreply@ecommerce.com}", \
  "-Dnotification.email.enabled=${NOTIFICATION_EMAIL_ENABLED:true}", \
  "-Dnotification.sms.enabled=${NOTIFICATION_SMS_ENABLED:false}", \
  "-jar", "commerce-service.jar"]
