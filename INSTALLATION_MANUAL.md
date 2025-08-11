# Installation Manual

## AI-Powered Collaborative Learning Management System

This manual provides clear, step-by-step instructions to install and run the complete system using Docker. The system consists of a Vue.js frontend, Spring Boot Java backend, FastAPI Python backend, and MySQL database.

## Prerequisites

Before installation, ensure your system meets the following requirements:

### Required Software
- **Docker**: Version 20.x or higher
- **Docker Compose**: Version 2.x or higher
- **Git**: For cloning the repository

### System Requirements
- **RAM**: Minimum 4GB, Recommended 8GB
- **Storage**: At least 5GB free space
- **Network**: Internet connection for downloading dependencies

### Verification Commands
```bash
# Verify Docker installation
docker --version
# Expected output: Docker version 20.x.x or higher

# Verify Docker Compose installation
docker-compose --version
# Expected output: Docker Compose version 2.x.x or higher

# Verify Git installation
git --version
# Expected output: git version 2.x.x or higher
```

## Installation Steps

### Step 1: Clone the Repository
```bash
git clone <repository-url>
cd capstone-project-25t2-9900-t13a-almond
```

### Step 2: Environment Configuration

#### 2.1 Create Backend Java Environment File
Create `backend-java/src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://mysql:3306/capstone_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true
spring.datasource.username=capstone_user
spring.datasource.password=capstone_password_2024
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Connection Pool Configuration
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# MyBatis Configuration
mybatis.mapper-locations=classpath:demo/mapper/*.xml
mybatis.type-aliases-package=demo.pojo
mybatis.configuration.map-underscore-to-camel-case=true

# Flyway Database Migration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# JWT Configuration (Use a secure secret in production)
jwt.secret=MySecretKey12345678901234567890123456789012345678901234567890
jwt.expiration=86400000

# File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Alibaba Cloud OSS Configuration
oss.endpoint=https://oss-cn-hongkong.aliyuncs.com
oss.accessKeyId=YOUR_OSS_ACCESS_KEY_ID
oss.accessKeySecret=YOUR_OSS_ACCESS_KEY_SECRET
oss.bucketName=YOUR_OSS_BUCKET_NAME

# Python Backend Integration
python.backend.url=http://backend-python:8000

# Logging Configuration
logging.level.demo=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

#### 2.2 Create Backend Python Environment File
Create `backend-python/.env`:

```env
# OpenAI Configuration (Required for AI features)
OPENAI_API_KEY=your_openai_api_key_here

# Java Backend Integration
JAVA_BACKEND_URL=http://backend-java:8080

# FastAPI Configuration
API_HOST=0.0.0.0
API_PORT=8000
DEBUG=False

# Logging Configuration
LOG_LEVEL=INFO
```

#### 2.3 Create Frontend Environment File
Create `frontend/.env`:

```env
# API Base URLs
VUE_APP_JAVA_API_BASE_URL=http://localhost:8080
VUE_APP_PYTHON_API_BASE_URL=http://localhost:8000

# Application Configuration
VUE_APP_TITLE=AI-Powered Learning Management System
VUE_APP_VERSION=1.0.0
```

#### 2.4 Create Database Environment File
Create `.env` in the project root:

```env
# MySQL Database Configuration
MYSQL_ROOT_PASSWORD=root_password_2024
MYSQL_DATABASE=capstone_db
MYSQL_USER=capstone_user
MYSQL_PASSWORD=capstone_password_2024

# OpenAI API Key
OPENAI_API_KEY=your_openai_api_key_here
```

### Step 3: Build and Start the Application

#### 3.1 Build All Services
```bash
# Build all Docker images (this may take 5-10 minutes)
docker-compose build

# Expected output: Successfully built images for all services
```

#### 3.2 Start the Complete System
```bash
# Start all services in detached mode
docker-compose up -d

# Expected output: 
# Creating network "capstone-project-25t2-9900-t13a-almond_default" with the default driver
# Creating capstone-project-25t2-9900-t13a-almond_mysql_1 ... done
# Creating capstone-project-25t2-9900-t13a-almond_backend-java_1 ... done
# Creating capstone-project-25t2-9900-t13a-almond_backend-python_1 ... done
# Creating capstone-project-25t2-9900-t13a-almond_frontend_1 ... done
```

#### 3.3 Verify Services are Running
```bash
# Check service status
docker-compose ps

# Expected output: All services should show "Up" status
# NAME                                            COMMAND                  SERVICE         STATUS
# capstone-project-25t2-9900-t13a-almond_mysql_1       "docker-entrypoint.s…"   mysql           Up
# capstone-project-25t2-9900-t13a-almond_backend-java_1    "java -jar /app/app.…"   backend-java    Up
# capstone-project-25t2-9900-t13a-almond_backend-python_1  "uvicorn main:app --…"   backend-python  Up
# capstone-project-25t2-9900-t13a-almond_frontend_1        "/docker-entrypoint.…"   frontend        Up
```

### Step 4: Access the Application

#### 4.1 Application URLs
- **Frontend (Main Application)**: http://localhost:3000
- **Java Backend API**: http://localhost:8080
- **Python Backend API**: http://localhost:8000
- **API Documentation**: http://localhost:8000/docs (FastAPI automatic docs)

#### 4.2 Health Checks
```bash
# Check Java backend health
curl http://localhost:8080/api/hello
# Expected response: {"success":true,"message":"Hello World","data":null}

# Check Python backend health
curl http://localhost:8000/health
# Expected response: {"status":"healthy","timestamp":"...","version":"1.0.0"}

# Check frontend accessibility
curl -I http://localhost:3000
# Expected response: HTTP/1.1 200 OK
```

### Step 5: Initial System Setup

#### 5.1 Database Initialization
The database schema is automatically created by Flyway migrations when the Java backend starts. You can verify this by checking the logs:

```bash
# Check Java backend logs for database migration
docker-compose logs backend-java | grep -i flyway

# Expected output should include:
# Flyway Community Edition ... by Redgate
# Successfully applied 4 migration(s) to schema `capstone_db`
```

#### 5.2 Create Initial Admin User
Access the application at http://localhost:3000 and use the registration page to create the first admin user, or use the following default credentials if seed data is available:

- **Username**: admin
- **Password**: admin123
- **Role**: Admin

#### 5.3 Test Core Functionality

1. **User Registration**: Create a student and teacher account
2. **Login**: Test authentication with different roles
3. **Basic Navigation**: Verify all dashboards load correctly
4. **AI Features**: Test weekly goal generation (requires valid OpenAI API key)

## Environment Variables and Secrets

### Required Environment Variables

#### Critical Variables (Must be configured)
```env
# Database credentials
MYSQL_ROOT_PASSWORD=your_secure_root_password
MYSQL_USER=capstone_user
MYSQL_PASSWORD=your_secure_user_password

# JWT Secret (minimum 32 characters)
JWT_SECRET=your_jwt_secret_key_minimum_256_bits_for_security

# OpenAI API Key (for AI features)
OPENAI_API_KEY=sk-your_openai_api_key_here
```

#### Optional Variables (Have defaults)
```env
# Application ports (defaults: 3000, 8080, 8000, 3306)
FRONTEND_PORT=3000
JAVA_BACKEND_PORT=8080
PYTHON_BACKEND_PORT=8000
MYSQL_PORT=3306

# Database name (default: capstone_db)
MYSQL_DATABASE=capstone_db

# Logging level (default: INFO)
LOG_LEVEL=INFO
```

### Security Notes
- **Never commit secrets to Git repository**
- **Use strong passwords for database credentials**
- **Store OpenAI API key securely**
- **Change default JWT secret in production**

## Troubleshooting

### Common Issues and Solutions

#### Issue 1: Port Already in Use
```bash
# Error: "Port 3000 is already allocated"
# Solution: Stop conflicting services or change ports

# Check what's using the port
netstat -tulpn | grep :3000

# Kill the process if needed
sudo kill -9 <process_id>

# Or change the port in docker-compose.yml
```

#### Issue 2: Database Connection Failed
```bash
# Error: "Connection refused" or "Access denied"
# Solution: Verify database credentials and wait for MySQL to be ready

# Check MySQL container logs
docker-compose logs mysql

# Wait for MySQL to be ready (may take 30-60 seconds on first startup)
# Look for: "MySQL init process done. Ready for start up."
```

#### Issue 3: OutOfMemory Error
```bash
# Error: "OutOfMemoryError" in Java backend
# Solution: Increase Docker memory allocation

# Increase Docker Desktop memory allocation to at least 4GB
# Or add memory limits to docker-compose.yml:
# services:
#   backend-java:
#     mem_limit: 1g
```

#### Issue 4: OpenAI API Key Issues
```bash
# Error: "Invalid API key" or AI features not working
# Solution: Verify OpenAI API key configuration

# Check if API key is set
docker-compose exec backend-python env | grep OPENAI

# Test API key manually
curl -H "Authorization: Bearer your_api_key" https://api.openai.com/v1/models
```

#### Issue 5: Frontend Build Failures
```bash
# Error: "npm ERR!" during build
# Solution: Clear npm cache and rebuild

# Rebuild frontend with clean cache
docker-compose build --no-cache frontend

# Or build locally first
cd frontend
npm ci
npm run build
cd ..
docker-compose build frontend
```

### Verification Commands

#### Check All Services
```bash
# Comprehensive system check
echo "=== Docker Compose Status ==="
docker-compose ps

echo "=== Service Health Checks ==="
curl -s http://localhost:8080/api/hello | jq .
curl -s http://localhost:8000/health | jq .
curl -I http://localhost:3000 2>/dev/null | head -1

echo "=== Database Connection ==="
docker-compose exec mysql mysql -u capstone_user -p capstone_db -e "SHOW TABLES;"

echo "=== Container Logs (last 10 lines) ==="
docker-compose logs --tail=10 backend-java
docker-compose logs --tail=10 backend-python
docker-compose logs --tail=10 frontend
```

#### Performance Check
```bash
# Check resource usage
docker stats --no-stream

# Expected resource usage:
# - MySQL: ~200MB RAM
# - Java Backend: ~300-500MB RAM
# - Python Backend: ~100-200MB RAM
# - Frontend: ~50MB RAM
```

## Stopping the Application

### Graceful Shutdown
```bash
# Stop all services gracefully
docker-compose down

# Stop and remove volumes (deletes database data)
docker-compose down -v

# Stop and remove images
docker-compose down --rmi all
```

### Emergency Stop
```bash
# Force stop all containers
docker stop $(docker ps -q)

# Remove all containers
docker rm $(docker ps -aq)
```

## Alternative Installation (Non-Docker)

If Docker cannot be used, follow these manual installation steps:

### Prerequisites
- Node.js 16+, npm 8+
- Java JDK 17+, Maven 3.8+
- Python 3.11+, pip
- MySQL 8.0+

### Manual Setup Steps

#### 1. Database Setup
```sql
-- Connect to MySQL as root
mysql -u root -p

-- Create database and user
CREATE DATABASE capstone_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'capstone_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON capstone_db.* TO 'capstone_user'@'localhost';
FLUSH PRIVILEGES;
```

#### 2. Backend Java Setup
```bash
cd backend-java
# Update application.properties with localhost database URL
mvn clean install
mvn spring-boot:run
# Service available at: http://localhost:8080
```

#### 3. Backend Python Setup
```bash
cd backend-python
pip install -r requirements.txt
# Create .env file with configuration
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
# Service available at: http://localhost:8000
```

#### 4. Frontend Setup
```bash
cd frontend
npm install
# Update .env with localhost URLs
npm run serve
# Service available at: http://localhost:8080 (or next available port)
```

## Support and Contact

If you encounter issues not covered in this manual:

1. Check the application logs using `docker-compose logs [service-name]`
2. Verify all environment variables are correctly set
3. Ensure all prerequisites are met
4. Refer to the troubleshooting section above

For additional support, contact the development team or refer to the project documentation in the README.md file.

---

This installation manual provides complete instructions to run the AI-Powered Collaborative Learning Management System. The system should be fully functional after following these steps, with all services communicating properly through the Docker network.
