# 🚪 API Gateway

Central entry point of the Healthcare System platform.

The API Gateway is responsible for request routing, authentication validation, request tracking, secure user context propagation, and enforcement of cross-cutting security concerns across the microservice ecosystem.

Built with Spring Cloud Gateway and Spring WebFlux, it serves as the trusted boundary between external clients and internal services.

---

# 📖 Overview

API Gateway acts as the single entry point for all incoming client requests.

Its responsibilities include:

* Request routing
* Authentication validation
* Request correlation
* User context propagation
* Security enforcement
* Header sanitization
* Service discovery integration
* Centralized access control

The gateway ensures that only authenticated and properly validated requests reach downstream services.

---

# 🚀 Core Features

## 🌐 Centralized Routing

All client traffic enters the system through the gateway.

Requests are routed dynamically to internal services using service discovery.

Examples:

* Auth Service
* User Service
* AI Service

Benefits:

* Single entry point
* Simplified client integrations
* Centralized security controls
* Reduced coupling between clients and services

---

## 🔐 Authentication Validation

Protected endpoints are validated before reaching business services.

Validation workflow:

1. Client sends JWT Access Token.
2. Gateway extracts token.
3. Gateway calls Auth Service validation endpoint.
4. User identity and roles are verified.
5. Request proceeds only when validation succeeds.

Unauthorized requests are rejected immediately.

---

## 🆔 Request Correlation

Every request receives a unique Request ID.

Features:

* Existing Request IDs are validated
* Missing Request IDs are generated automatically
* Request IDs are stored in Redis
* IDs are propagated across services

Benefits:

* Distributed tracing
* Faster debugging
* Better observability
* Log correlation across services

---

## ✍️ Signed User Context Propagation

After successful authentication, the gateway creates a signed security context.

The generated payload contains:

* User ID
* User Roles
* Request ID
* Issued At timestamp
* Expiration timestamp

The context is cryptographically signed using RSA and propagated through internal headers.

Benefits:

* Trusted identity propagation
* Protection against header tampering
* Reduced authentication overhead
* Secure service-to-service communication

---

## 🛡 Header Sanitization

Security-sensitive headers are removed and recreated by the gateway.

Clients cannot inject:

* User ID
* User Roles
* Internal Security Context
* Trusted Identity Headers

This prevents privilege escalation through forged requests.

---

## ☁ Service Discovery Integration

The gateway integrates with Eureka Service Registry.

Benefits:

* Dynamic routing
* Automatic service discovery
* Simplified deployments
* Reduced configuration maintenance

---

# 🏗 Architecture

```text
Client
   │
   ▼
API Gateway
   │
   ├── RequestId Filter
   │
   ├── Authentication Validation
   │
   ├── User Context Signing
   │
   ├── Security Headers
   │
   ▼
Microservices
   │
   ├── Auth Service
   ├── User Service
   ├── AI Service
   └── Other Services
```

The gateway acts as the trusted security perimeter of the entire platform.

---

# 🔒 Security Design

Implemented security mechanisms:

## JWT Validation

Authentication tokens are validated through Auth Service.

The gateway itself does not perform authentication logic but enforces authentication checks consistently across the platform.

---

## RSA Signed User Context

User identity information is signed using asymmetric cryptography.

Benefits:

* Integrity protection
* Trusted identity propagation
* Tamper resistance
* Reduced service coupling

---

## Fail-Closed Security Model

Security checks never fail open.

Examples:

* Invalid token → request rejected
* Missing token → request rejected
* Auth Service unavailable → request rejected
* Invalid signature → request rejected

This minimizes security risks during failures.

---

## Trusted Internal Headers

Only the gateway is allowed to create security-sensitive headers.

Downstream services trust gateway-generated context rather than client-supplied values.

---

# ⚙ Key Components

## RequestIdGlobalFilter

Responsible for:

* UUID generation
* UUID validation
* Request correlation
* Redis persistence
* Context propagation

---

## AuthValidationGatewayFilterFactory

Responsible for:

* Auth Service integration
* Token validation
* User identity extraction
* Role extraction

Authenticated user information is stored within the Gateway Exchange context.

---

## AddSignedUserContextGatewayFilterFactory

Responsible for:

* Security context creation
* JWS generation
* RSA signing
* Secure downstream propagation

Produces the signed X-User-Context header.

---

## RsaJwsUserContextSigner

Provides cryptographic signing capabilities.

Responsibilities:

* Claim creation
* JWS generation
* Signature protection
* Context integrity verification

---

# 📡 Request Processing Flow

```text
Client Request
        │
        ▼
RequestId Filter
        │
        ▼
JWT Validation
        │
        ▼
User Context Signing
        │
        ▼
Route Resolution
        │
        ▼
Target Service
```

This flow ensures consistent security enforcement for every protected request.

---

# ⚙ Technology Stack

## Backend

* Java
* Spring Boot
* Spring Cloud Gateway
* Spring WebFlux

## Security

* JWT
* JWS
* RSA Signatures

## Infrastructure

* Redis
* Eureka Discovery Client
* Spring Cloud Config

## Documentation

* OpenAPI
* Swagger

## Testing

* JUnit 5
* Mockito
* Spring Boot Test

---

# 🧪 Testing Strategy

The gateway includes unit and integration tests covering critical infrastructure and security components.

Covered areas:

* Request ID generation
* Request ID validation
* Authentication validation flow
* Gateway filter behavior
* User context signing
* Configuration validation
* Security scenarios

Testing tools:

* JUnit 5
* Mockito
* Spring Boot Test

---

# ⚙ Configuration

Configuration is externalized through Spring Cloud Config Server.

Examples:

* Route definitions
* Authentication endpoints
* Header names
* Redis settings
* Context expiration policies
* Security configuration

This approach allows centralized management across multiple environments.

---

# 📜 API Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

---

# 🐳 Infrastructure Dependencies

Required services:

* Auth Service
* Redis
* Config Server
* Service Registry

Optional:

* Monitoring and tracing tools

---

# 📁 Project Structure

```text
api-gateway
├── config
├── filter
├── security
├── signer
├── route
├── properties
├── exception
├── util
└── resources
```

---

# 🤔 Why This Design?

### Why an API Gateway?

In a microservice architecture, exposing every service directly increases complexity and security risks.

A centralized gateway provides:

* Unified entry point
* Consistent security
* Centralized routing
* Better observability
* Simplified client integrations

---

### Why Validate Authentication in the Gateway?

Authentication should be enforced before requests reach business services.

Benefits:

* Reduced duplicated logic
* Consistent security model
* Smaller service responsibilities
* Faster rejection of unauthorized requests

Business services can focus entirely on domain logic.

---

### Why Request IDs?

Distributed systems make debugging significantly harder.

Request IDs provide:

* End-to-end traceability
* Faster issue investigation
* Cross-service log correlation
* Improved observability

---

### Why Redis for Request Tracking?

Redis offers fast distributed storage with TTL support.

Benefits:

* High performance
* Distributed deployment compatibility
* Automatic expiration
* Reduced memory management complexity

---

### Why Signed User Context?

Without signed context, downstream services would need to repeatedly call Auth Service.

Using signed identity propagation provides:

* Better performance
* Reduced network overhead
* Trusted user information
* Tamper protection

---

### Why RSA Signatures?

Asymmetric cryptography allows services to verify signatures without accessing private keys.

Benefits:

* Improved security
* Signature verification independence
* Reduced key exposure risk
* Strong integrity guarantees

---

### Why Spring Cloud Gateway?

Spring Cloud Gateway provides:

* Reactive architecture
* Built-in routing
* Gateway filters
* Cloud-native integration
* Service discovery support

making it a strong choice for modern microservice systems.

---

### Why Fail-Closed Security?

Security components should never allow requests when validation fails.

The gateway intentionally rejects requests whenever:

* Authentication cannot be verified
* Services become unavailable
* Security context cannot be established

This approach prioritizes security over availability for protected operations.

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
* Spring Cloud
* Spring Security
* Microservices
* Distributed Systems
* Event-Driven Architecture
* API Design
* Cloud-Native Development
* Secure Service Communication
