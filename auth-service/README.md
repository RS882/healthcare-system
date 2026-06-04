# 🛡️ Auth Service

Authentication and Authorization Service of the Healthcare System platform.

This microservice is responsible for user authentication, JWT token management, secure session handling, token validation, logout processing, and identity verification for internal microservices.

The service is designed as an independent authentication provider within a distributed microservice architecture and follows modern security practices including JWT-based authentication, HttpOnly cookie storage, Redis-backed session management, and centralized validation through API Gateway.

---

# 📖 Overview

Auth Service serves as the central authentication component of the Healthcare System.

Its primary responsibilities include:

* User authentication
* JWT Access Token generation
* JWT Refresh Token generation
* Token validation
* Session management
* Logout processing
* Security context validation
* Integration with API Gateway
* Communication with User Service

The service follows a stateless authentication model while maintaining additional security controls through Redis.

---

# 🚀 Core Features

## 🔐 User Authentication

Authenticates users using their credentials and issues secure JWT tokens.

Features:

* Credential validation
* User status verification
* Authentication exception handling
* Secure token generation

---

## 🎟 JWT Access & Refresh Tokens

The service generates two token types.

### Access Token

Short-lived JWT used for accessing protected resources.

Contains:

* User ID
* User Roles
* Token Expiration

### Refresh Token

Long-lived JWT used for obtaining new access tokens without requiring re-authentication.

Benefits:

* Improved security
* Reduced login frequency
* Better user experience

---

## 🍪 Secure Refresh Token Storage

Refresh Tokens are stored in HttpOnly Cookies.

Advantages:

* Not accessible through JavaScript
* Reduced XSS attack surface
* Secure browser-managed storage
* Improved session security

---

## 🔄 Token Refresh Workflow

The service supports secure token renewal.

Flow:

1. Client sends Refresh Token cookie.
2. Refresh Token is validated.
3. Session state is verified.
4. New Access Token is issued.
5. New Refresh Token is generated.

This allows users to maintain authenticated sessions while minimizing exposure of long-lived credentials.

---

## 🚪 Logout Processing

Logout immediately invalidates the active session.

Features:

* Refresh Token removal
* Access Token blacklisting
* Redis cleanup
* Session invalidation

This prevents reuse of previously issued tokens.

---

## 🚫 Redis-Based Session Management

Redis is used to maintain security-related state.

Responsibilities:

* Active session tracking
* Access token blacklist
* Logout support
* Session limitation

Benefits:

* Immediate token invalidation
* Fast lookups
* Distributed deployment support
* Improved security

---

## ☁ User Service Integration

Auth Service does not manage user data directly.

Instead, user information is retrieved through internal APIs exposed by User Service.

Communication is implemented using OpenFeign clients.

Benefits:

* Clear service ownership
* Reduced coupling
* Independent scalability
* Better maintainability

---

# 🏗 Architecture

```text
Client
   │
   ▼
API Gateway
   │
   ▼
Auth Service
   │
   ├── Authentication
   ├── JWT Generation
   ├── Token Validation
   ├── Session Management
   │
   ▼
Redis
   │
   ▼
User Service
```

The service follows strict separation of responsibilities:

* Authentication → Auth Service
* User Management → User Service
* Routing & Security Boundary → API Gateway

---

# 🔒 Security Design

Implemented security mechanisms:

## JWT Authentication

Stateless authentication using signed JSON Web Tokens.

---

## Access & Refresh Token Strategy

Separate token lifecycles improve overall security and user experience.

---

## HttpOnly Cookies

Refresh Tokens are protected from client-side script access.

---

## Access Token Blacklisting

Logged-out access tokens are blocked immediately.

---

## Redis-backed Session Control

Provides centralized security state management.

---

## Gateway Validation Support

Dedicated validation endpoint allows API Gateway to authenticate requests before they reach business services.

---

# 📡 Main API Endpoints

| Method | Endpoint                  | Description           |
| ------ | ------------------------- | --------------------- |
| POST   | `/api/v1/auth/login`      | Authenticate user     |
| POST   | `/api/v1/auth/refresh`    | Refresh tokens        |
| POST   | `/api/v1/auth/logout`     | Logout user           |
| POST   | `/api/v1/auth/validation` | Validate access token |

---

# ⚙ Technology Stack

## Backend

* Java
* Spring Boot
* Spring Security

## Security

* JWT
* HttpOnly Cookies

## Infrastructure

* Redis
* OpenFeign
* Spring Cloud Config
* Eureka Discovery Client

## Documentation

* OpenAPI
* Swagger UI

## Testing

* JUnit 5
* Mockito
* Spring Boot Test
* MockMvc

---

# 🧪 Testing Strategy

The service contains unit and integration tests covering core authentication functionality.

Covered components:

* JWT generation and validation
* Refresh Token management
* Session management
* Token blacklist functionality
* Authentication business logic
* REST API endpoints
* Exception handling

Testing tools:

* JUnit 5
* Mockito
* MockMvc
* Spring Boot Test

---

# ⚙ Configuration

Configuration is externalized using Spring Cloud Config Server.

Example:

```yaml
jwt:
  access-token-expiration: 600000
  refresh-token-expiration: 1209600000
```

This allows centralized configuration management across environments.

---

# 📜 API Documentation

Swagger UI:

```text
http://localhost:8081/swagger-ui/index.html
```

---

# 🐳 Infrastructure Dependencies

Required services:

* Redis
* User Service
* API Gateway
* Config Server
* Service Registry

Optional:

* RedisInsight

---

# 📁 Project Structure

```text
auth-service
├── controller
├── service
├── security
├── filter
├── validator
├── config
├── domain
├── exception
├── mapper
└── resources
```

---

# 🤔 Why This Design?

### Why separate authentication from user management?

Authentication and user management represent different business responsibilities.

Separating them into dedicated services provides:

* Better maintainability
* Independent scalability
* Clear domain ownership
* Reduced coupling

User data remains the responsibility of User Service while Auth Service focuses exclusively on authentication and authorization.

---

### Why JWT?

JWT enables stateless authentication.

Benefits:

* Horizontal scalability
* Reduced database load
* Standardized security model
* Simplified distributed authentication

---

### Why Access and Refresh Tokens?

A single long-lived token increases security risks.

Separating responsibilities allows:

* Short-lived access credentials
* Secure session renewal
* Better compromise containment
* Improved user experience

---

### Why HttpOnly Cookies?

Refresh Tokens are stored in HttpOnly Cookies to reduce exposure to browser-based attacks.

Benefits:

* Protection against JavaScript access
* Reduced XSS attack surface
* Secure browser handling

---

### Why Redis?

JWT is stateless by design, but practical security requirements require shared state.

Redis enables:

* Logout support
* Access token blacklisting
* Session tracking
* Active session limitation

while preserving the scalability advantages of JWT authentication.

---

### Why Feign Instead of Direct Database Access?

Auth Service intentionally has no direct access to user storage.

Benefits:

* Strong service boundaries
* Independent deployments
* Domain ownership
* Better long-term maintainability

---

### Why API Gateway Validation?

Authentication is validated at the system boundary.

Benefits:

* Consistent security enforcement
* Reduced duplication
* Simpler downstream services
* Centralized access control

---

# 🏥 Healthcare System Platform

This service is part of the Healthcare System, a cloud-native healthcare platform built using a microservice architecture.

The platform follows modern software engineering principles including:

* Domain-driven service separation
* Centralized configuration management
* Service discovery
* API Gateway routing
* Secure inter-service communication
* Event-driven integration
* Containerized deployment
* Automated testing

Each service is designed to be independently deployable, maintainable, and scalable while collaborating through clearly defined APIs and infrastructure components.

---

# 👨‍💻 Author

**Ruslan Senkin**

Java Backend Developer

Specialization:

* Java
* Spring Boot
* Spring Security
* Microservices
* Spring Cloud
* Distributed Systems
* Event-Driven Architecture
* API Design
* Cloud-Native Development
