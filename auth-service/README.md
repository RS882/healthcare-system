# 🛡️ Auth Service — Healthcare System

Authentication and authorization microservice using Spring Boot, JWT, Redis, and HttpOnly Cookies. It's part of the Healthcare Record Management System.

---

## 📚 Features

- 🔐 User registration and login
- 🔑 Access / Refresh token generation (JWT)
- 🧁 Refresh token stored in HttpOnly cookie
- 🔁 Token refresh via `/refresh`
- 🚪 Logout with access token blacklist
- 🧼 Refresh token removal
- 🚫 Redis-based session limit blocking
- ☁️ Communicates with `user-service` via Feign (no direct DB access)

---

## 📦 Technologies

| Technology         | Purpose                             |
|--------------------|-------------------------------------|
| Spring Boot        | Core application framework          |
| Spring Security    | Authentication/Authorization        |
| JWT                | Access and Refresh tokens           |
| Redis              | Token and blacklist storage         |
| Feign Client       | Integration with `user-service`     |
| Swagger/OpenAPI    | API documentation                   |

---

## 🔧 Setup

### Required services:

- Redis
- RedisInsight (optional UI)
- `user-service` (manages users)

### Example configuration (via Spring Cloud Config):

```yaml
spring:
  application:
    name: auth-service
  cloud:
    config:
      uri: http://localhost:8888

jwt:
  access-secret: <base64-secret>
  refresh-secret: <base64-secret>
  access-token-expiration: 600000
  refresh-token-expiration: 1209600000
```

> 💡 Auth-service does not use its own database — all user management is delegated to `user-service`.

---

## 🔐 Main Endpoints

| Method | URL | Description        |
|--------|-----|--------------------|
| POST   | `/api/v1/auth/registration` | Register user      |
| POST   | `/api/v1/auth/login`        | Login user         |
| POST   | `/api/v1/auth/refresh`      | Refresh tokens     |
| POST   | `/api/v1/auth/logout`       | Logout user        |

---

## 🔬 Testing

```bash
./mvnw test
```

Includes unit and integration tests:

- ✅ JwtService
- ✅ RefreshTokenService
- ✅ TokenBlacklistService
- ✅ AuthServiceImpl (mocked `UserClient`)
- ✅ Controller (via MockMvc or WebMvcTest)

---

## 📜 Swagger UI

Access API documentation:

```
http://localhost:8081/swagger-ui/index.html
```

---

## 🐳 Redis (Docker Compose)

```yaml
services:
  redis:
    image: redis
    ports:
      - "6379:6379"

  redis-insight:
    image: redis/redisinsight
    ports:
      - "8001:8001"
```

---

## 📁 Project Structure

```
auth-service/
├── controller/
├── config/
├── domain/
├── service/
├── exception_handler/
├── filter/
├── validator/
├── resources/
```

---

## 🧑‍💻 Author

- Developer: @Ruslan Senkin
- Last updated: March 2025

---

## 🧩 Part of microservice system

- `auth-service` — authentication
- `user-service` — user management (MySQL)
- `patient-service`, `appointment-service`, `notification-service`
- `api-gateway`, `config-server`, `service-registry`