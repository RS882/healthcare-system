# 👤 User Service

User Management Service of the Healthcare System platform.

The service is responsible for user lifecycle management, secure user data access, audit logging, event publication, and reliable integration with other services through an event-driven architecture.

It combines traditional REST-based operations with Kafka-driven asynchronous communication using the Transactional Outbox Pattern to guarantee reliable event delivery.

---

# 📖 Overview

User Service is the central owner of user-related business data.

Responsibilities include:

* User registration
* User retrieval
* User updates
* User deletion
* Audit logging
* Event publication
* Event consumption
* User data validation
* Secure user context processing

The service acts as the authoritative source of user information within the Healthcare System.

---

# 🚀 Core Features

## 👤 User Management

Provides REST APIs for managing user entities.

Capabilities:

* User registration
* User retrieval
* User updates
* User deletion
* Validation handling
* Error handling

The service follows a clear separation between API, business, and persistence layers.

---

## 🔒 Secure Service-to-Service Communication

User Service trusts only requests validated by API Gateway.

Security mechanisms include:

* Signed User Context verification
* Role-based authorization
* Request ID validation
* Security header validation

This prevents unauthorized direct access to protected resources.

---

## 📝 Audit Logging

Every important business operation can be recorded as an audit event.

Captured information may include:

* Event type
* Aggregate identifier
* User information
* Timestamp
* Additional context

Benefits:

* Traceability
* Compliance support
* Operational transparency
* Easier troubleshooting

---

# 📡 Event-Driven Architecture

The service publishes and consumes business events using Apache Kafka.

Examples:

* User Registered
* User Updated
* User Deleted

This enables asynchronous communication between services while keeping service boundaries independent.

---

## 📤 Transactional Outbox Pattern

The service implements the Transactional Outbox Pattern.

Workflow:

```text
User Transaction
        │
        ▼
Database Commit
        │
        ▼
Outbox Event
        │
        ▼
Outbox Publisher
        │
        ▼
Kafka Topic
        │
        ▼
Consumers
```

Benefits:

* Reliable event delivery
* Database and event consistency
* Reduced risk of lost messages
* Production-grade event publishing

---

## 🔄 Outbox Publishing

Outbox events are processed asynchronously.

Features:

* Event claiming
* Status tracking
* Retry support
* Publishing metrics
* Failure recovery

Supported statuses:

* NEW
* PROCESSING
* PUBLISHED
* FAILED

---

## ♻ Recovery Mechanism

The service automatically detects and recovers stuck processing events.

Recovery jobs:

* Outbox Processing Recovery
* Cleanup Jobs

Benefits:

* Self-healing behavior
* Improved reliability
* Reduced manual intervention

---

## 📨 Kafka Integration

The service uses Apache Kafka for event distribution.

Capabilities:

* Event publishing
* Event consumption
* Topic management
* Retry handling
* Dead Letter Topic support

---

## 🚫 Idempotent Consumer Processing

Consumers are protected against duplicate event processing.

Implementation:

* Processed Event table
* Composite key tracking
* Consumer-specific event registration

Benefits:

* Exactly-once business processing
* Safe retries
* Duplicate message protection

---

## ☠ Retry and Dead Letter Topics

Failed event processing is handled through retry mechanisms.

Features:

* Automatic retries
* Dead Letter Topics (DLT)
* Failure isolation
* Error visibility

This prevents message loss while improving operational stability.

---

# 🏗 Architecture

```text
Client
   │
   ▼
API Gateway
   │
   ▼
User Service
   │
   ├── REST API
   ├── Business Logic
   ├── Audit Logging
   ├── Outbox Events
   │
   ▼
MySQL
   │
   ▼
Outbox Publisher
   │
   ▼
Kafka
   │
   ▼
Consumers
```

The service combines synchronous REST operations with asynchronous event-driven integration.

---

# 🔒 Security Design

Implemented security mechanisms:

## Signed User Context Verification

Requests are validated using gateway-generated signed user context.

Benefits:

* Trusted identity propagation
* Reduced authentication overhead
* Protection against header tampering

---

## Role-Based Authorization

Access to protected operations is controlled through roles.

---

## Request Tracking

All requests include Request IDs for distributed tracing and observability.

---

# ⚙ Technology Stack

## Backend

* Java
* Spring Boot
* Spring Security
* Spring Data JPA

## Database

* MySQL
* Liquibase

## Messaging

* Apache Kafka
* Transactional Outbox Pattern

## Infrastructure

* Spring Cloud Config
* Eureka Discovery Client

## Monitoring

* Micrometer Metrics
* Custom Kafka Metrics
* Outbox Metrics

## Testing

* JUnit 5
* Mockito
* Testcontainers
* Spring Boot Test

---

# 🧪 Testing Strategy

The service contains extensive unit and integration testing.

Covered areas:

### REST Layer

* Controllers
* Validation
* Exception handling

### Business Layer

* User operations
* Audit processing
* Security logic

### Kafka

* Producer integration
* Consumer integration
* Retry scenarios
* DLT scenarios

### Outbox

* Outbox publishing
* Recovery processing
* Cleanup jobs
* Failure scenarios

### Infrastructure

* MySQL Testcontainers
* Kafka Testcontainers

Testing tools:

* JUnit 5
* Mockito
* Testcontainers
* Spring Boot Test

---

# 📊 Observability

The service exposes metrics for operational monitoring.

Examples:

### Outbox Metrics

* Pending events
* Processing events
* Failed events

### Kafka Metrics

* Consumed events
* Processing failures
* Consumer performance

Benefits:

* Improved visibility
* Faster troubleshooting
* Capacity planning

---

# ⚙ Configuration

Configuration is centralized through Spring Cloud Config Server.

Examples:

* Kafka topics
* Consumer groups
* Retry settings
* Outbox cleanup intervals
* Recovery intervals
* Security settings

---

# 📜 API Documentation

Swagger UI:

```text
http://localhost:8082/swagger-ui/index.html
```

---

# 🐳 Infrastructure Dependencies

Required services:

* MySQL
* Kafka
* API Gateway
* Config Server
* Service Registry

---

# 📁 Project Structure

```text
user-service
├── controller
├── service
├── repository
├── mapper
├── audit
├── kafka
├── outbox
├── security
├── config
├── validator
├── exception
└── resources
```

---

# 🤔 Why This Design?

### Why separate User Management into its own service?

User data is a core business domain.

Keeping ownership in a dedicated service provides:

* Clear domain boundaries
* Independent scaling
* Easier maintenance
* Reduced coupling

---

### Why Event-Driven Communication?

Direct synchronous communication increases dependencies between services.

Kafka-based integration provides:

* Loose coupling
* Better scalability
* Improved resilience
* Asynchronous processing

---

### Why the Transactional Outbox Pattern?

Publishing Kafka events directly inside business transactions risks data inconsistencies.

The Outbox Pattern guarantees:

* Reliable event delivery
* Consistent state transitions
* Failure recovery
* Production-grade messaging

---

### Why Idempotent Consumers?

Kafka can redeliver messages.

Idempotent processing ensures:

* Duplicate safety
* Reliable retries
* Consistent business state
* Predictable event handling

---

### Why Retry and DLT?

Failures are inevitable in distributed systems.

Retry and DLT mechanisms provide:

* Automatic recovery
* Error isolation
* Operational visibility
* Reduced message loss

---

### Why Audit Logging?

Business-critical systems require traceability.

Audit logs provide:

* Historical visibility
* Compliance support
* Easier investigations
* Improved transparency

---

### Why Testcontainers?

Infrastructure-related bugs frequently appear only in real environments.

Testcontainers allow:

* Real MySQL testing
* Real Kafka testing
* Repeatable integration tests
* Higher confidence before deployment

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
* Apache Kafka
* Event-Driven Architecture
* Distributed Systems
* Cloud-Native Development
* API Design
* Test Automation
