# Security & Best Practices - Production Deployment

## 🔴 CRITICAL SECURITY ISSUES

### 1. JWT Secret Must Be Changed
**Current Status:** Default value visible in source code  
**Action Required:** 
```bash
# Generate a new 64-character hex secret
openssl rand -hex 32
# Example: a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1

# Set this SAME secret on ALL services:
JWT_SECRET=<generated_value>
```

**Verification:** All services must have identical JWT_SECRET

### 2. Database Credentials Must Be Strong
**Current Status:** Default password is "root" (weak)  
**Action Required:**
```
Generate strong password:
- Minimum 12 characters
- Include uppercase letters (A-Z)
- Include lowercase letters (a-z)
- Include numbers (0-9)
- Include special characters (!@#$%^&*)

Examples (DO NOT USE THESE - GENERATE YOUR OWN):
✓ P@ssw0rd$2024Secure!
✓ MySecure#Pass99!
✗ root, password, 123456, admin (weak - DO NOT USE)
```

### 3. Never Commit Secrets to Git
**Action Required:**
```bash
# Verify .gitignore contains:
echo ".env" >> .gitignore
echo ".env.local" >> .gitignore
echo ".env.production" >> .gitignore

# Remove any accidentally committed secrets
git rm --cached .env
git commit -m "Remove .env file with secrets"

# For historical cleanup (if committed):
git filter-branch --tree-filter 'rm -f .env' HEAD
```

### 4. Use HTTPS in Production (Not HTTP)
**Required for all service URLs:**
```
✓ CORRECT:
AUTH_SERVICE_URL=https://auth-service-xxxxx.onrender.com
PRODUCT_SERVICE_URL=https://product-service-xxxxx.onrender.com
COMMERCE_SERVICE_URL=https://commerce-service-xxxxx.onrender.com
CORS_ALLOWED_ORIGINS=https://yourdomain.com

✗ INCORRECT (HTTP only for local development):
AUTH_SERVICE_URL=http://auth-service:8081  # OK for docker-compose locally
```

### 5. CORS Configuration
**Current Risk:** Localhost origins allowed for CORS  
**Production Action:**
```yaml
# DEVELOPMENT
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200

# PRODUCTION - ONLY trusted domains
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# NOT recommended (security risk):
# * (wildcard - allows ANY origin)
```

---

## ⚠️ IMPORTANT CONFIGURATION CHANGES

### Disable Mock Payment Processing
**Location:** Commerce Service  
**Action:**
```
DEVELOPMENT:
PAYMENT_MOCK_ENABLED=true
PAYMENT_MOCK_SUCCESS_RATE=90

PRODUCTION:
PAYMENT_MOCK_ENABLED=false
PAYMENT_MOCK_SUCCESS_RATE=not_used
```

### Change Hibernate Mode from UPDATE to VALIDATE
**Current:** `ddl-auto: update`  
**Risk:** Auto-modifies production schema  
**Recommendation:**

Option 1 - Use VALIDATE mode (recommended):
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Only validates, doesn't modify
```

Option 2 - Use application-prod.yml:
```yaml
# application.yml (development)
spring.jpa.hibernate.ddl-auto=update

# application-prod.yml (production)
spring.jpa.hibernate.ddl-auto=validate
spring.profiles.active=prod
```

### Disable Show-SQL in Production
**Current:** `show-sql: false` ✅ (already correct)  
**Verify:** All application.yml files have this set to false

### Connection Pool Sizing
**Current:** `maximum-pool-size: 5` (OK for free tier)  
**Adjust for production based on:**
```
formula: (number_of_cpu_cores × 2) + effective_spindle_count
examples:
- Small app (low concurrency): 5-10
- Medium app (moderate traffic): 20-30
- Large app (high concurrency): 50+

For Render free tier: 5 is appropriate
```

---

## 🔐 SECRETS MANAGEMENT STRATEGY

### Development (Local)
```bash
# Use .env file (git-ignored)
cp .env.example .env
# Edit with local values
docker-compose up

# NEVER commit .env to git
```

### Staging/Production (Render.com)
```
1. Go to Render Dashboard
2. Select Service → Environment
3. Add each secret:
   - Key: JWT_SECRET
   - Value: <generated_64_hex_string>
   - Check "Encrypted"
4. Repeat for all secrets
5. Click "Save"
6. Service auto-redeploys with new secrets
```

### Production (AWS/Azure)
```
Use dedicated secrets managers:
- AWS: AWS Secrets Manager or Parameter Store
- Azure: Azure Key Vault
- Docker Swarm: Docker Secrets
- Kubernetes: Kubernetes Secrets

Example with AWS:
1. Create secret in AWS Secrets Manager
2. Use IAM role to access from service
3. Inject at runtime via environment variables
```

---

## 🛡️ DEPLOYMENT SECURITY CHECKLIST

Before deploying to production, verify:

- [ ] **JWT Secret**
  - [ ] Generated with `openssl rand -hex 32`
  - [ ] 64 characters long
  - [ ] Hexadecimal format only
  - [ ] Same value on ALL services
  - [ ] NOT in source code or config files
  - [ ] Stored in secrets manager

- [ ] **Database Credentials**
  - [ ] Password is strong (12+ chars, mixed case, numbers, symbols)
  - [ ] NOT the default "root" password
  - [ ] Username is NOT "postgres"
  - [ ] Stored in secrets manager
  - [ ] NOT in .env or source code

- [ ] **Service URLs**
  - [ ] All use HTTPS (not HTTP) in production
  - [ ] Correct service hostnames/IPs
  - [ ] Services are publicly accessible (if needed)
  - [ ] Health check endpoints work

- [ ] **CORS Configuration**
  - [ ] Only trusted domains listed
  - [ ] Wildcard (*) NOT used in production
  - [ ] Frontend domain included
  - [ ] Credentials and cookies handled securely

- [ ] **Payment Processing**
  - [ ] PAYMENT_MOCK_ENABLED=false in production
  - [ ] Real payment gateway configured
  - [ ] Payment credentials secured
  - [ ] PCI compliance verified

- [ ] **Logging & Monitoring**
  - [ ] Sensitive data NOT logged (passwords, secrets, tokens)
  - [ ] Logging level set appropriately (INFO for production)
  - [ ] Error tracking configured
  - [ ] Monitoring/alerting active

- [ ] **Database Security**
  - [ ] Connection only from authorized services
  - [ ] Backups enabled
  - [ ] Encryption at rest enabled (if available)
  - [ ] Network isolation (private subnet if possible)

- [ ] **Git Security**
  - [ ] .env file in .gitignore
  - [ ] No secrets in commit history
  - [ ] Repository is private (or has limited access)
  - [ ] Branch protection rules enabled

- [ ] **Docker Image Security**
  - [ ] Base image is up-to-date (eclipse-temurin:21-jre-alpine)
  - [ ] Container runs as non-root user (if possible)
  - [ ] No hardcoded secrets in Dockerfile
  - [ ] Image scanned for vulnerabilities

---

## 🚨 INCIDENT RESPONSE

### If JWT Secret is Compromised
```
1. Generate new secret: openssl rand -hex 32
2. Update on ALL services immediately
3. Invalidate existing tokens (users must re-login)
4. Review access logs for suspicious activity
5. Consider rotating signing key version
```

### If Database Password is Compromised
```
1. Change database password immediately
2. Update all services with new password
3. Review database access logs
4. Check for unauthorized schema changes
5. Restore from backup if needed
```

### If Service is Hacked
```
1. Take service offline immediately
2. Review access logs and audit trail
3. Check for data exfiltration
4. Identify the vulnerability
5. Fix the issue
6. Deploy patched version
7. Verify integrity of data
8. Notify users if data was accessed
```

---

## 📋 ENVIRONMENT VARIABLE MATRIX

| Variable | Development | Staging | Production |
|----------|-------------|---------|------------|
| JWT_SECRET | default-hex | strong-hex | STRONG-HEX |
| DB_PASSWORD | root | strong | STRONG |
| PAYMENT_MOCK | true | false | false |
| DB_URL | localhost | staging-db | prod-db |
| Service URLs | localhost:8080 | staging.render | prod.render |
| HTTPS | false | true | true ✓ |
| Logging Level | DEBUG | INFO | INFO |

---

## 🔄 Rotating Secrets

### JWT Secret Rotation
```
Step 1: Generate new secret
openssl rand -hex 32

Step 2: Update one service at a time
- Keep old tokens valid for 24 hours
- Update one service
- Monitor for errors
- Update next service

Step 3: Retire old secret
- After 24-48 hours
- All old tokens naturally expire
```

### Database Password Rotation
```
Step 1: Create new database user with new password
Step 2: Update one service with new credentials
Step 3: Remove old user access
Step 4: Verify all services connected
Step 5: Delete old user account
```

---

## 🎓 Security Resources

- OWASP Top 10: https://owasp.org/Top10/
- JWT Best Practices: https://tools.ietf.org/html/rfc8949
- Spring Security: https://spring.io/projects/spring-security
- PostgreSQL Security: https://www.postgresql.org/docs/current/sql-syntax.html

---

## ❓ Common Questions

**Q: Can I use the same password for all services?**  
A: Yes, they all connect to the same PostgreSQL database. However, use different user accounts (auth_user, product_user, commerce_user) with individual passwords for better security.

**Q: How often should I rotate secrets?**  
A: Industry best practice: every 90 days minimum. After any suspected compromise: immediate.

**Q: Is localhost safe for development?**  
A: Yes, but only on your machine. Never expose localhost on network. Consider using Docker instead.

**Q: What if I lose my JWT_SECRET?**  
A: You must generate a new one and all users must re-login (existing tokens become invalid).

**Q: Should I log sensitive data?**  
A: Never. Configure logging to exclude:
```yaml
# DO NOT LOG:
- Passwords
- API Keys
- JWT Tokens
- Credit card numbers
- Social security numbers
```

---

## ✅ Ready for Deployment

After completing all security checklist items, you're ready to deploy safely to production!

For step-by-step deployment instructions, see: **DEPLOYMENT_GUIDE.md**
