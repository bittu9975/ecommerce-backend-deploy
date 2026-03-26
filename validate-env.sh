# Environment Variables Verification Script
# Use this to validate your deployment configuration

#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "E-Commerce Microservices - Env Validation"
echo "=========================================="
echo ""

# Function to check variable
check_var() {
    local var_name=$1
    local var_value=$2
    local required=$3
    
    if [ -z "$var_value" ]; then
        if [ "$required" == "required" ]; then
            echo -e "${RED}✗ MISSING${NC} $var_name - REQUIRED"
            return 1
        else
            echo -e "${YELLOW}⚠ EMPTY${NC} $var_name - optional"
            return 0
        fi
    else
        # Check if value looks like a placeholder or example
        if [[ "$var_value" == *"EXAMPLE"* ]] || [[ "$var_value" == *"YOUR"* ]] || [[ "$var_value" == *"REPLACE"* ]]; then
            echo -e "${RED}✗ PLACEHOLDER${NC} $var_name - needs real value"
            return 1
        fi
        echo -e "${GREEN}✓ SET${NC} $var_name"
        return 0
    fi
}

# Function to validate JWT secret format
validate_jwt_secret() {
    local secret=$1
    local length=${#secret}
    
    if [ $length -ne 64 ]; then
        echo -e "${RED}✗ Invalid JWT_SECRET length: $length (should be 64 hex chars)${NC}"
        return 1
    fi
    
    if ! [[ "$secret" =~ ^[0-9a-fA-F]+$ ]]; then
        echo -e "${RED}✗ Invalid JWT_SECRET format: must be hexadecimal${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✓ JWT_SECRET format valid (64-char hex)${NC}"
    return 0
}

# Function to validate database URL format
validate_db_url() {
    local url=$1
    
    if [[ "$url" == *"postgresql://"* || "$url" == *"jdbc:postgresql://"* ]]; then
        echo -e "${GREEN}✓ Database URL format valid${NC}"
        return 0
    else
        echo -e "${RED}✗ Invalid database URL format${NC}"
        return 1
    fi
}

# Function to validate URL format
validate_url() {
    local url=$1
    local label=$2
    
    if [[ "$url" == http://* ]] || [[ "$url" == https://* ]]; then
        echo -e "${GREEN}✓ $label format valid${NC}"
        return 0
    else
        echo -e "${RED}✗ Invalid URL format for $label${NC}"
        return 1
    fi
}

# Load .env file if it exists
if [ -f ".env" ]; then
    echo -e "${YELLOW}Loading variables from .env file...${NC}\n"
    export $(cat .env | grep -v '#' | xargs)
else
    echo -e "${YELLOW}No .env file found - checking environment variables${NC}\n"
fi

# Initialize error counter
errors=0

# ============================================
# DATABASE CONFIGURATION
# ============================================
echo "📦 DATABASE CONFIGURATION"
echo "---"
check_var "POSTGRES_USER" "$POSTGRES_USER" "required" || ((errors++))
check_var "POSTGRES_PASSWORD" "$POSTGRES_PASSWORD" "required" || ((errors++))
check_var "SPRING_DATASOURCE_URL" "$SPRING_DATASOURCE_URL" "required" || ((errors++))

if [ ! -z "$SPRING_DATASOURCE_URL" ]; then
    validate_db_url "$SPRING_DATASOURCE_URL" || ((errors++))
fi

check_var "SPRING_DATASOURCE_USERNAME" "$SPRING_DATASOURCE_USERNAME" "required" || ((errors++))
check_var "SPRING_DATASOURCE_PASSWORD" "$SPRING_DATASOURCE_PASSWORD" "required" || ((errors++))
echo ""

# ============================================
# REDIS CONFIGURATION
# ============================================
echo "⚡ REDIS CONFIGURATION (Product & Commerce Services)"
echo "---"
check_var "SPRING_REDIS_HOST" "$SPRING_REDIS_HOST" "required" || ((errors++))
check_var "SPRING_REDIS_PORT" "$SPRING_REDIS_PORT" "required" || ((errors++))
echo ""

# ============================================
# JWT CONFIGURATION
# ============================================
echo "🔐 JWT CONFIGURATION (ALL SERVICES)"
echo "---"
check_var "JWT_SECRET" "$JWT_SECRET" "required" || ((errors++))

if [ ! -z "$JWT_SECRET" ]; then
    validate_jwt_secret "$JWT_SECRET" || ((errors++))
fi

check_var "JWT_EXPIRATION" "$JWT_EXPIRATION" "optional"
echo ""

# ============================================
# SERVICE DISCOVERY
# ============================================
echo "🌐 SERVICE DISCOVERY (API Gateway & Commerce)"
echo "---"
check_var "AUTH_SERVICE_URL" "$AUTH_SERVICE_URL" "required" || ((errors++))
if [ ! -z "$AUTH_SERVICE_URL" ]; then
    validate_url "$AUTH_SERVICE_URL" "AUTH_SERVICE_URL" || ((errors++))
fi

check_var "PRODUCT_SERVICE_URL" "$PRODUCT_SERVICE_URL" "required" || ((errors++))
if [ ! -z "$PRODUCT_SERVICE_URL" ]; then
    validate_url "$PRODUCT_SERVICE_URL" "PRODUCT_SERVICE_URL" || ((errors++))
fi

check_var "COMMERCE_SERVICE_URL" "$COMMERCE_SERVICE_URL" "required" || ((errors++))
if [ ! -z "$COMMERCE_SERVICE_URL" ]; then
    validate_url "$COMMERCE_SERVICE_URL" "COMMERCE_SERVICE_URL" || ((errors++))
fi
echo ""

# ============================================
# CORS CONFIGURATION
# ============================================
echo "🛡️  CORS CONFIGURATION"
echo "---"
check_var "CORS_ALLOWED_ORIGINS" "$CORS_ALLOWED_ORIGINS" "optional"
echo ""

# ============================================
# COMMERCE SERVICE SPECIFIC
# ============================================
echo "🛒 COMMERCE SERVICE CONFIGURATION"
echo "---"
check_var "PAYMENT_MOCK_ENABLED" "$PAYMENT_MOCK_ENABLED" "optional"
check_var "CART_TTL" "$CART_TTL" "optional"
check_var "CART_MAX_ITEMS" "$CART_MAX_ITEMS" "optional"
check_var "NOTIFICATION_EMAIL_ENABLED" "$NOTIFICATION_EMAIL_ENABLED" "optional"
check_var "NOTIFICATION_SMS_ENABLED" "$NOTIFICATION_SMS_ENABLED" "optional"
echo ""

# ============================================
# SERVER PORTS
# ============================================
echo "🚀 SERVER PORT CONFIGURATION"
echo "---"
check_var "API_GATEWAY_PORT" "${API_GATEWAY_PORT:-8080}" "optional"
check_var "AUTH_SERVICE_PORT" "${AUTH_SERVICE_PORT:-8081}" "optional"
check_var "PRODUCT_SERVICE_PORT" "${PRODUCT_SERVICE_PORT:-8082}" "optional"
check_var "COMMERCE_SERVICE_PORT" "${COMMERCE_SERVICE_PORT:-8083}" "optional"
echo ""

# ============================================
# SUMMARY
# ============================================
echo "=========================================="
if [ $errors -eq 0 ]; then
    echo -e "${GREEN}✅ ALL CHECKS PASSED${NC}"
    echo "Your environment is ready for deployment!"
    echo ""
    echo "Next steps:"
    echo "1. Review .env file for sensitive values"
    echo "2. Ensure .env is in .gitignore"
    echo "3. For production: use platform secrets instead"
    exit 0
else
    echo -e "${RED}❌ $errors ERROR(S) FOUND${NC}"
    echo "Please fix the errors above before deploying"
    echo ""
    echo "Tips:"
    echo "- Check .env.example for required variables"
    echo "- Ensure JWT_SECRET is generated:"
    echo "  openssl rand -hex 32"
    exit 1
fi
