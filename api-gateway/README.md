# API Gateway

## Overview

The API Gateway serves as the single entry point to the Healthcare System microservice ecosystem.

Built with Spring Cloud Gateway and WebFlux, it provides centralized routing, authentication validation, request tracking, security context propagation, and infrastructure-level cross-cutting concerns.

The gateway follows a security-first approach and acts as a trusted boundary between external clients and internal services.

---

## Responsibilities

### Request Routing

Routes incoming requests to the appropriate downstream microservice.

Examples:

* Auth Service
* User Service
* AI Service

---

### Authentication Validation

Protected endpoints are validated through the Auth Service.

Flow:

1. Client sends JWT token.
2. Gateway calls Auth Service validation endpoint.
3. Auth Service verifies token.
4. User identity and roles are extracted.
5. Request proceeds only if validation succeeds.

The gateway follows a fail-closed model:

* Validation failure → request rejected
* Auth Service unavailable → request rejected

No downstream service receives unauthenticated requests.

---

### Request Correlation

Every incoming request receives a unique Request ID.

Features:

* Existing request IDs are validated
* Missing IDs are generated automatically
* Request IDs are stored in Redis
* Request IDs are propagated across services

Benefits:

* Distributed request tracing
* Easier debugging
* Improved observability
* Cross-service log correlation

---

### Secure User Context Propagation

After successful authentication, the gateway creates a signed user context.

The generated context contains:

* User ID
* User Roles
* Request ID
* Issue Time
* Expiration Time

The payload is cryptographically signed using RSA.

Benefits:

* Downstream services trust the gateway
* No repeated authentication calls
* Protection against header tampering
* Reduced service coupling

---

## Architecture

```text
Client
   │
   ▼
API Gateway
   │
   ├── RequestId Filter
   │
   ├── Auth Validation Filter
   │
   ├── User Context Signing Filter
   │
   ▼
Downstream Services
```

---

## Security Model

The gateway acts as the security perimeter of the platform.

Implemented mechanisms:

### JWT Validation

Authentication tokens are validated centrally through Auth Service.

### Signed User Context

Internal user context is signed using RSA JWS.

### Trusted Headers

Security-sensitive headers are removed and recreated by the gateway.

Clients cannot inject:

* User ID
* Roles
* Internal Security Context

### Fail-Closed Design

Security checks never fail open.

Unauthorized requests are blocked immediately.

---

## Key Components

### RequestIdGlobalFilter

Responsible for:

* Request correlation
* UUID generation
* Redis persistence
* Context propagation

---

### AuthValidationGatewayFilterFactory

Responsible for:

* Auth Service integration
* Token validation
* User identity extraction
* Role extraction

Stores authenticated user information in Gateway Exchange Attributes.

---

### AddSignedUserContextGatewayFilterFactory

Responsible for:

* Building security context
* RSA signing
* Secure downstream propagation

Creates a signed X-User-Context header.

---

### RsaJwsUserContextSigner

Provides cryptographic signing using RSA private keys.

Responsibilities:

* JWS generation
* Claim creation
* Signature integrity

---

## Technology Stack

### Frameworks

* Java
* Spring Boot
* Spring Cloud Gateway
* Spring WebFlux

### Security

* JWT
* RSA Signatures
* JWS

### Infrastructure

* Redis
* Eureka Discovery Client
* Spring Cloud Config

### Testing

* JUnit 5
* Mockito
* Integration Tests

---

## Configuration

Configuration is externalized through Spring Cloud Config Server.

Examples:

* Route definitions
* Header names
* Security settings
* Validation endpoints
* Context expiration policies

---

## Testing Strategy

The gateway contains both unit and integration tests.

Covered areas:

* Request ID generation
* Request ID validation
* Auth validation flow
* User context signing
* Gateway filter behavior
* Property validation

The goal is to verify both business behavior and infrastructure-level concerns.

---

## Why This Design?

The gateway centralizes authentication, routing, request tracking, and security context propagation.

Benefits:

* Reduced duplication across services
* Consistent security model
* Better observability
* Improved scalability
* Clear separation of responsibilities

The implementation demonstrates practical experience with distributed systems, API security, Spring Cloud infrastructure, and production-oriented microservice architecture.
