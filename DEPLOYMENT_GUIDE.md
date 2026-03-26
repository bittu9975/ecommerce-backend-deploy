# Deployment Guide - E-Commerce Microservices

## Quick Start (Development)

### Using Docker Compose Locally

```bash
# Generate JWT Secret (run once and save it)
openssl rand -hex 32
# Example output: a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6...

# Create .env file
cp .env.example .env

# Edit .env and replace with your generated JWT_SECRET
nano .env

# Start all services
docker-compose up -d

# Check logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Manual Build & Run

```bash
# Build all services
./mvnw clean package -DskipTests

# Run each service
java -jar auth-service/target/auth-service-1.0.0.jar &
java -jar product-service/target/product-service-1.0.0.jar &
java -jar commerce-service/target/commerce-service-1.0.0.jar &
java -jar api-gateway/target/api-gateway-1.0.0.jar &
```

---

## Production Deployment (Render.com)

### Step 1: Prerequisites

1. **Render Account** - Sign up at https://render.com
2. **PostgreSQL Instance** - Create a free PostgreSQL database on Render
3. **Redis Instance** - Create a free Redis instance on Render
4. **GitHub Repository** - Push your code to GitHub

### Step 2: Generate Secrets

```bash
# Generate strong JWT secret (64-character hex)
openssl rand -hex 32

# Save this value - you'll use it for all services
```

### Step 3: Deploy Database

1. Create PostgreSQL on Render:
   - Go to Dashboard → New → PostgreSQL
   - Choose Free tier
   - Name: `ecommerce-postgres`
   - Note the connection details

2. Create Redis on Render:
   - Go to Dashboard → New → Redis
   - Choose Free tier
   - Name: `ecommerce-redis`
   - Note the connection details

### Step 4: Deploy Services (in order)

#### Service 1: Auth Service

1. Go to Dashboard → New → Web Service
2. Select your GitHub repository
3. Service name: `auth-service`
4. Environment variables:

```
SPRING_DATASOURCE_URL=postgresql://<DB_USER>:<DB_PASSWORD>@<DB_HOST>:<DB_PORT>/<DB_NAME>
SPRING_DATASOURCE_USERNAME=<DB_USER>
SPRING_DATASOURCE_PASSWORD=<DB_PASSWORD>
JWT_SECRET=<generated_64_hex_secret>
SERVER_PORT=8081
```

5. Build Command: `./mvnw clean package -DskipTests`
6. Start Command: `java -jar auth-service/target/auth-service-1.0.0.jar`
7. Deploy

#### Service 2: Product Service

1. Repeat Service 1 steps with:
   - Service name: `product-service`
   - SERVER_PORT: 8082
   - Add Redis environment:
     ```
     SPRING_REDIS_HOST=<REDIS_HOST>
     SPRING_REDIS_PORT=<REDIS_PORT>
     ```
   - Start Command: `java -jar product-service/target/product-service-1.0.0.jar`

#### Service 3: Commerce Service

1. Repeat Service 1 steps with:
   - Service name: `commerce-service`
   - SERVER_PORT: 8083
   - Add Redis environment:
     ```
     SPRING_REDIS_HOST=<REDIS_HOST>
     SPRING_REDIS_PORT=<REDIS_PORT>
     PRODUCT_SERVICE_URL=https://<product-service-url>
     PAYMENT_MOCK_ENABLED=false
     ```
   - Start Command: `java -jar commerce-service/target/commerce-service-1.0.0.jar`

#### Service 4: API Gateway (Deploy LAST)

1. Go to Dashboard → New → Web Service
2. Select your GitHub repository
3. Service name: `api-gateway`
4. Environment variables:

```
AUTH_SERVICE_URL=https://<auth-service-render-url>
PRODUCT_SERVICE_URL=https://<product-service-render-url>
COMMERCE_SERVICE_URL=https://<commerce-service-render-url>
JWT_SECRET=<same_64_hex_secret>
CORS_ALLOWED_ORIGINS=https://<frontend-domain>
SERVER_PORT=8080
```

5. Build Command: `./mvnw clean package -DskipTests`
6. Start Command: `java -jar api-gateway/target/api-gateway-1.0.0.jar`
7. Deploy

---

## Environment Variables Reference

### Critical Variables (MUST SET IN PRODUCTION)

| Variable | Description | Example |
|----------|-------------|---------|
| `JWT_SECRET` | 64-char hex secret | `a1b2c3d4e5f6...` |
| `SPRING_DATASOURCE_URL` | PostgreSQL connection | `postgresql://user:pass@host:5432/db` |
| `SPRING_DATASOURCE_USERNAME` | DB username | `prod_user` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | `strong_password` |
| `SPRING_REDIS_HOST` | Redis hostname | `redis.hostname` |
| `SPRING_REDIS_PORT` | Redis port | `6379` |

### Service Discovery URLs (API Gateway & Commerce)

| Variable | Service | Example |
|----------|---------|---------|
| `AUTH_SERVICE_URL` | Auth Service URL | `https://auth-service-xxxxx.onrender.com` |
| `PRODUCT_SERVICE_URL` | Product Service URL | `https://product-service-xxxxx.onrender.com` |
| `COMMERCE_SERVICE_URL` | Commerce Service URL | `https://commerce-service-xxxxx.onrender.com` |

### Optional Configuration

| Variable | Default | Purpose |
|----------|---------|---------|
| `PAYMENT_MOCK_ENABLED` | `false` | Enable payment mocking (dev only) |
| `CART_TTL` | `86400` | Cart expiration in seconds |
| `NOTIFICATION_EMAIL_ENABLED` | `true` | Enable email notifications |
| `NOTIFICATION_SMS_ENABLED` | `false` | Enable SMS notifications |

---

## Security Checklist

- [ ] JWT_SECRET is 64-character hex string (generated with `openssl rand -hex 32`)
- [ ] JWT_SECRET is IDENTICAL across all services
- [ ] Database password is STRONG (min 12 chars, mixed case, numbers, symbols)
- [ ] Database password is NOT the default `root`
- [ ] All services are using HTTPS URLs in production (https://, not http://)
- [ ] CORS_ALLOWED_ORIGINS contains only trusted domains
- [ ] PAYMENT_MOCK_ENABLED is set to `false` in production
- [ ] No hardcoded passwords in source code or environment files
- [ ] .env file is in .gitignore (never commit secrets)
- [ ] Render environment variables are used (not .env files in production)

---

## Monitoring & Debugging

### Health Checks

Each service exposes `/actuator/health`:

```bash
curl https://api-gateway-xxxxx.onrender.com/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

### Logs

View logs in Render dashboard or via CLI:

```bash
# Using Render CLI
render logs --service auth-service

# Or check via dashboard: Logs tab
```

### Common Issues

**Problem:** Service stuck in "Building"
- Check build logs for Maven errors
- Ensure `.mvn/wrapper/maven-wrapper.jar` exists
- Verify pom.xml syntax

**Problem:** 503 Service Unavailable
- Check if database is accessible from service
- Verify all environment variables are set
- Check Redis connectivity for product/commerce services

**Problem:** JWT validation errors
- Ensure JWT_SECRET is identical across all services
- Verify JWT_SECRET is 64-character hex string
- Check token expiration: curl -H "Authorization: Bearer <token>" endpoint

**Problem:** Database connection timeout
- Verify database URL in environment variable
- Check database credential accuracy
- Ensure Render network connectivity

---

## Rollback Plan

1. **Previous Version Deployment:**
   - Go to service in Render dashboard
   - Click "Environment" tab
   - Change build command or GitHub branch to previous version
   - Click "Manual Deploy"

2. **Database Backup:**
   - Render auto-backs up PostgreSQL daily
   - Contact Render support for point-in-time restore

3. **Cache (Redis):**
   - Rebuild cart cache (non-critical, TTL handles cleanup)
   - Users sessions remain valid if JWT_SECRET unchanged

---

## Performance Optimization (Free Tier)

- Connection pool size: 5 (optimized for free tier resources)
- Redis TTL: 24 hours (automatic cleanup)
- Hibernate DDL: `update` mode (auto-schema)
- Response caching: Product service caches via Redis

---

## Support & Resources

- **Docs:** See DEPLOYMENT_READINESS_REPORT.md
- **Issues:** Check logs in Render dashboard
- **Architecture:** See README.md
- **Environment Template:** See .env.example
