# E-Commerce Microservices — Render Free Tier Optimised

## Architecture (4 Services)

| Service | Port | Description |
|---|---|---|
| api-gateway | 8080 | Single entry point, routes to all services |
| auth-service | 8081 | JWT auth, user registration/login |
| product-service | 8082 | Product catalog, Redis caching |
| commerce-service | 8083 | Cart + Order + Payment + Notification merged |

## Infrastructure

| Resource | Provider | Notes |
|---|---|---|
| PostgreSQL | Render | 1 instance, 3 schemas auto-created by Hibernate |
| Redis | Render | Cart storage |
| RabbitMQ | None | Replaced by Spring ApplicationEventPublisher |

## Schemas Created Automatically

- `auth_schema` — users table
- `product_schema` — products, categories tables
- `commerce_schema` — orders, order_items, payments, notifications tables

## Build Command (every service)
```
./mvnw clean package -DskipTests
```

## Start Commands
```
api-gateway:      java -jar target/api-gateway-1.0.0.jar
auth-service:     java -jar target/auth-service-1.0.0.jar
product-service:  java -jar target/product-service-1.0.0.jar
commerce-service: java -jar target/commerce-service-1.0.0.jar
```

## Environment Variables

### API Gateway
| Variable | Value |
|---|---|
| AUTH_SERVICE_URL | https://your-auth-service.onrender.com |
| PRODUCT_SERVICE_URL | https://your-product-service.onrender.com |
| COMMERCE_SERVICE_URL | https://your-commerce-service.onrender.com |
| CORS_ALLOWED_ORIGINS | https://your-frontend.com |
| JWT_SECRET | (64-char hex — must match all services) |

### Auth Service
| Variable | Value |
|---|---|
| SPRING_DATASOURCE_URL | Internal PostgreSQL URL |
| SPRING_DATASOURCE_USERNAME | DB username |
| SPRING_DATASOURCE_PASSWORD | DB password |
| JWT_SECRET | Same as gateway |

### Product Service
| Variable | Value |
|---|---|
| SPRING_DATASOURCE_URL | Same internal PostgreSQL URL |
| SPRING_DATASOURCE_USERNAME | DB username |
| SPRING_DATASOURCE_PASSWORD | DB password |
| SPRING_REDIS_HOST | Redis host |
| SPRING_REDIS_PORT | 6379 |
| JWT_SECRET | Same as gateway |

### Commerce Service
| Variable | Value |
|---|---|
| SPRING_DATASOURCE_URL | Same internal PostgreSQL URL |
| SPRING_DATASOURCE_USERNAME | DB username |
| SPRING_DATASOURCE_PASSWORD | DB password |
| SPRING_REDIS_HOST | Redis host |
| SPRING_REDIS_PORT | 6379 |
| JWT_SECRET | Same as gateway |
| PRODUCT_SERVICE_URL | https://your-product-service.onrender.com |

## Deployment Order
1. Provision PostgreSQL on Render
2. Provision Redis on Render
3. Deploy auth-service
4. Deploy product-service
5. Deploy commerce-service
6. Deploy api-gateway LAST (needs all service URLs)

## Generate JWT Secret
```bash
openssl rand -hex 32
```
