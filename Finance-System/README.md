# 💰 Finance Data Processing & Access Control System

A **production-ready modular monolith** built with Java 17, Spring Boot 3, PostgreSQL, and Redis.

---

## 🏗️ Architecture

```
finance-system/
├── src/main/java/com/finance/system/
│   ├── auth/
│   │   ├── controller/   AuthController.java
│   │   ├── service/      AuthService.java
│   │   └── dto/          AuthDtos.java
│   ├── records/
│   │   ├── controller/   FinancialRecordController.java
│   │   ├── service/      FinancialRecordService.java
│   │   └── dto/          RecordDtos.java
│   ├── dashboard/
│   │   ├── controller/   DashboardController.java
│   │   ├── service/      DashboardService.java
│   │   └── dto/          DashboardDtos.java
│   ├── entity/           User.java, FinancialRecord.java
│   ├── repository/       UserRepository.java, FinancialRecordRepository.java
│   ├── security/
│   │   ├── jwt/          JwtUtils.java
│   │   ├── filter/       JwtAuthenticationFilter.java
│   │   └── service/      UserDetailsImpl.java, UserDetailsServiceImpl.java
│   ├── config/           SecurityConfig.java, RedisConfig.java, OpenApiConfig.java, DataSeeder.java
│   ├── exception/        ApiException.java, GlobalExceptionHandler.java
│   └── common/           ApiResponse.java
└── src/main/resources/
    ├── application.properties
    ├── schema.sql
    └── db/migration/V1__init_schema.sql
```

---

## ⚡ Quick Start (Docker)

```bash
# 1. Clone / extract the project
cd finance-system

# 2. Start everything (PostgreSQL + Redis + App)
docker-compose up --build

# 3. Open Swagger UI
open http://localhost:8080/swagger-ui.html
```

---

## 🖥️ Local Development (without Docker)

### Prerequisites
| Tool        | Version  |
|-------------|----------|
| Java        | 17+      |
| Maven       | 3.9+     |
| PostgreSQL  | 14+      |
| Redis       | 7+       |

### 1 — PostgreSQL

```bash
psql -U postgres -c "CREATE DATABASE finance_db;"
psql -U postgres -d finance_db -f src/main/resources/schema.sql
```

### 2 — Redis

```bash
# macOS
brew install redis && brew services start redis

# Ubuntu / Debian
sudo apt install redis-server && sudo systemctl start redis

# Windows (WSL or native)
sudo service redis-server start
```

### 3 — Configure `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/finance_db
spring.datasource.username=YOUR_PG_USER
spring.datasource.password=YOUR_PG_PASSWORD

spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### 4 — Build & Run

```bash
mvn clean package -DskipTests
java -jar target/finance-system-1.0.0.jar

# Or directly via Maven
mvn spring-boot:run
```

---

## 🔐 Authentication

All protected endpoints require a **Bearer JWT** in the `Authorization` header.

### Step 1 — Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@finance.com",
  "password": "Password1!"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userId": 1,
    "email": "admin@finance.com",
    "role": "ADMIN"
  }
}
```

### Step 2 — Use the token

```http
GET /api/v1/records
Authorization: Bearer eyJhbGci...
```

---

## 👤 Sample Users

| Email             | Password    | Role    | Status   |
|-------------------|-------------|---------|----------|
| admin@finance.com | Password1!  | ADMIN   | ACTIVE   |
| yash@finance.com  | Password1!  | ANALYST | ACTIVE   |
| raj@finance.com   | Password1!  | ANALYST | ACTIVE   |
| samir@finance.com | Password1!  | VIEWER  | ACTIVE   |
| amir@finance.com  | Password1!  | VIEWER  | INACTIVE |

---

## 📡 API Reference

### Auth Endpoints (Public)

| Method | URL                    | Description         |
|--------|------------------------|---------------------|
| POST   | /api/v1/auth/register  | Register new user   |
| POST   | /api/v1/auth/login     | Login & get JWT     |

### User Management (Protected)

| Method | URL                        | Roles               |
|--------|----------------------------|---------------------|
| GET    | /api/v1/users              | ADMIN, ANALYST      |
| GET    | /api/v1/users/{id}         | ADMIN, ANALYST      |
| PUT    | /api/v1/users/{id}/status  | ADMIN               |
| DELETE | /api/v1/users/{id}         | ADMIN               |

### Financial Records (Protected)

| Method | URL                   | Roles                    |
|--------|-----------------------|--------------------------|
| POST   | /api/v1/records       | ADMIN, ANALYST           |
| GET    | /api/v1/records       | ADMIN, ANALYST, VIEWER   |
| GET    | /api/v1/records/{id}  | ADMIN, ANALYST, VIEWER   |
| PUT    | /api/v1/records/{id}  | ADMIN, ANALYST           |
| DELETE | /api/v1/records/{id}  | ADMIN                    |

**Filter parameters for GET /api/v1/records:**
- `type` — `INCOME` or `EXPENSE`
- `category` — exact string match
- `from` — start date (`yyyy-MM-dd`)
- `to` — end date (`yyyy-MM-dd`)

### Dashboard (Protected)

| Method | URL                       | Roles          |
|--------|---------------------------|----------------|
| GET    | /api/v1/dashboard/summary | ADMIN, ANALYST |

ADMIN can pass `?userId=X` to view another user's dashboard.

---

## 🗄️ Redis Caching

| Cache Key                | TTL   | Evicted When                      |
|--------------------------|-------|-----------------------------------|
| `dashboard:user:{userId}`| 5 min | Record created / updated / deleted|

---

## 🔒 Role-Based Access Summary

| Endpoint Group      | ADMIN | ANALYST | VIEWER |
|---------------------|-------|---------|--------|
| Register / Login    | ✅    | ✅      | ✅     |
| User Management     | Full  | Read    | ✗      |
| Financial Records   | Full  | CRU     | R      |
| Dashboard           | ✅    | ✅      | ✗      |

---

## 🧪 Running Tests

```bash
mvn test
```

---

## 📖 Swagger UI

Once running, visit: **http://localhost:8080/swagger-ui.html**

1. Click **Authorize** (top right)
2. Enter: `Bearer <your_token>`
3. Explore all endpoints interactively

---

## 🚀 Production Checklist

- [ ] Change `app.jwt.secret` to a strong random 256-bit Base64 key
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` (already set)
- [ ] Use environment variables for all secrets (never commit credentials)
- [ ] Enable HTTPS / TLS termination
- [ ] Configure Redis AUTH password
- [ ] Set up database connection pool sizing for expected load
- [ ] Enable Spring Boot Actuator for health checks
- [ ] Add rate limiting (e.g., Bucket4j) on `/auth/login`


  ## Outpot    

<img width="1920" height="1080" alt="Screenshot 2026-04-06 203638" src="https://github.com/user-attachments/assets/ab95d0a5-9212-47b4-a786-9e184f306747" />
<img width="1920" height="1080" alt="Screenshot 2026-04-06 203819" src="https://github.com/user-attachments/assets/b5865291-0cef-47d6-b1d9-84d195dcd03a" />



