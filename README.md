# 🏥 Healthcare System

A cloud-native healthcare platform built using Java, Spring Boot, Spring Cloud, Apache Kafka, and AI technologies.

The project demonstrates modern backend engineering practices including microservice architecture, centralized configuration management, service discovery, secure authentication, event-driven communication, AI integration, automated testing, and containerized deployment.

The platform is designed to simulate real-world healthcare software systems where multiple independent domains collaborate through well-defined APIs and asynchronous messaging while remaining independently deployable and scalable.

---

# 📖 Project Overview

Healthcare System is a microservice-based platform focused on secure healthcare data management, user authentication, appointment management, patient information processing, notifications, and AI-powered healthcare assistance.

The project serves as a practical demonstration of:

* Microservice Architecture
* Cloud-Native Development
* Distributed Systems Design
* Event-Driven Communication
* Secure API Design
* AI Integration
* Infrastructure Automation
* Test Automation

The system is intentionally designed around domain boundaries to ensure maintainability, scalability, and long-term extensibility.

---

# 🎯 Project Goals

The primary goals of the platform are:

* Build a production-oriented microservice ecosystem
* Demonstrate secure service-to-service communication
* Implement reliable event-driven architecture
* Explore AI integration in healthcare workflows
* Apply modern cloud-native engineering practices
* Showcase scalable software architecture patterns

---

# 🏗 High-Level Architecture

```text
                           Client
                              │
                              ▼
                      API Gateway
                              │
      ┌───────────────────────┼───────────────────────┐
      ▼                       ▼                       ▼
 Auth Service          User Service            AI Service
      │                       │
      │                       ▼
      │                   Apache Kafka
      │                       │
      │                       ▼
      │                Event Consumers
      │
      ▼
     Redis

      ▲
      │
 Service Registry
      │
      ▼
 Config Server
```

The architecture combines synchronous REST communication with asynchronous event-driven integration.

---

# 🚀 Core Platform Capabilities

## 🔐 Authentication & Authorization

Authentication is handled through a dedicated Auth Service.

Features:

* JWT Authentication
* Access Token management
* Refresh Token management
* HttpOnly Cookies
* Session control
* Logout support
* Token validation
* Redis-backed security mechanisms

---

## 🌐 API Gateway

All incoming traffic enters through API Gateway.

Responsibilities:

* Request routing
* Authentication validation
* Request correlation
* User context propagation
* Security enforcement

The gateway acts as the trusted security boundary of the platform.

---

## 👤 User Management

User Service acts as the authoritative source of user information.

Capabilities:

* User registration
* User updates
* User retrieval
* Audit logging
* Event publication
* Event consumption

---

## 📡 Event-Driven Communication

The platform uses Apache Kafka for asynchronous communication.

Features:

* Event publishing
* Event consumption
* Retry handling
* Dead Letter Topics
* Idempotent consumers
* Recovery jobs

This enables loose coupling between services while improving scalability and resilience.

---

## 🤖 AI Integration

The platform includes a dedicated AI Service built using Spring AI.

Current capabilities:

* Medical note summarization
* Medical information extraction
* Message classification

Planned capabilities:

* Doctor preparation assistance
* Medical explanation generation
* Question generation
* Appointment preparation

---

## 📅 Appointment Management

**Status: 🚧 Under Development**

Planned capabilities:

* Appointment scheduling
* Availability management
* Appointment lifecycle processing
* Reminder integration

---

## 🏥 Patient Management

**Status: 🚧 Under Development**

Planned capabilities:

* Patient profiles
* Medical records
* Healthcare history
* Patient search

---

## 🔔 Notification Management

**Status: 🚧 Under Development**

Planned capabilities:

* Email notifications
* Appointment reminders
* Event-driven communication workflows
* Notification templates

---

# 🏛 Microservice Landscape

## 🚪 API Gateway

Central entry point of the platform.

Responsibilities:

* Routing
* Authentication validation
* Request tracking
* Signed user context propagation

---

## 🛡 Auth Service

Dedicated authentication and authorization service.

Responsibilities:

* JWT generation
* Token validation
* Session management
* Security enforcement

---

## 👤 User Service

Core user domain service.

Responsibilities:

* User lifecycle management
* Audit logging
* Kafka integration
* Outbox Pattern implementation

---

## 🤖 AI Service

Dedicated AI integration layer.

Responsibilities:

* Prompt orchestration
* AI provider abstraction
* Structured AI responses
* Healthcare AI functionality

---

## ⚙ Config Server

Centralized configuration management service.

Responsibilities:

* Environment-specific configuration
* Service bootstrap configuration
* Configuration consistency

---

## 📡 Service Registry

Service discovery infrastructure.

Responsibilities:

* Service registration
* Service discovery
* Health monitoring

---

## 📅 Appointment Service

**Under Development**

Future appointment scheduling domain.

---

## 🏥 Patient Service

**Under Development**

Future patient information domain.

---

## 🔔 Notification Service

**Under Development**

Future communication and notification domain.

---

# 📡 Event-Driven Architecture

The platform uses Apache Kafka together with the Transactional Outbox Pattern.

```text
Business Transaction
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
* Database consistency
* Failure recovery
* Reduced message loss

---

# 🔒 Security Architecture

Security is implemented as a platform-wide concern.

Implemented mechanisms:

## JWT Authentication

Stateless authentication using signed JWT tokens.

---

## Refresh Token Strategy

Long-lived sessions through secure token renewal.

---

## HttpOnly Cookies

Secure Refresh Token storage.

---

## Request Correlation

Distributed tracing through Request IDs.

---

## Signed User Context

RSA-signed identity propagation between services.

---

## Fail-Closed Security

Authentication failures always result in request rejection.

---

# 📊 Observability

The platform includes monitoring and observability capabilities.

Examples:

* Request tracking
* Kafka metrics
* Outbox metrics
* Audit logs
* Processing statistics

Benefits:

* Easier debugging
* Operational visibility
* Performance monitoring

---

# 🧪 Testing Strategy

The project emphasizes automated testing.

Testing approaches:

## Unit Testing

Coverage of business logic and utility components.

Tools:

* JUnit 5
* Mockito

---

## Integration Testing

Validation of infrastructure integrations.

Examples:

* Database integration
* Kafka integration
* Security integration

Tools:

* Spring Boot Test
* Testcontainers

---

## API Testing

Validation of REST endpoints and request processing.

---

# ⚙ Technology Stack

## Backend

* Java
* Spring Boot
* Spring Security
* Spring Data JPA

## Cloud Infrastructure

* Spring Cloud Gateway
* Spring Cloud Config
* Eureka Service Discovery

## Messaging

* Apache Kafka
* Transactional Outbox Pattern

## AI

* Spring AI
* Structured Output Mapping
* Ollama

## Databases

* MySQL
* Redis

## Storage

* MinIO

## DevOps

* Docker
* Docker Compose
* GitHub Actions

## Testing

* JUnit 5
* Mockito
* REST Assured
* Testcontainers

---

# 🤔 Architecture Decisions

### Why Microservices?

The platform is intentionally designed around independent business domains.

Benefits:

* Independent deployment
* Independent scalability
* Clear ownership boundaries
* Better maintainability

---

### Why API Gateway?

A centralized gateway provides:

* Unified entry point
* Consistent security
* Request tracing
* Simplified client integration

---

### Why Dedicated Auth Service?

Authentication evolves independently from business domains.

Separating authentication provides:

* Clear responsibility boundaries
* Better security management
* Easier maintenance

---

### Why Kafka?

Healthcare workflows frequently span multiple domains.

Kafka enables:

* Loose coupling
* Asynchronous communication
* Better scalability
* Improved resilience

---

### Why the Transactional Outbox Pattern?

Publishing events directly inside business transactions can create inconsistencies.

The Outbox Pattern guarantees:

* Reliable event delivery
* Consistent state transitions
* Failure recovery

---

### Why Idempotent Consumers?

Distributed systems must tolerate duplicate message delivery.

Idempotent consumers provide:

* Safe retries
* Duplicate protection
* Predictable business outcomes

---

### Why Spring AI?

AI functionality is becoming increasingly important in modern healthcare systems.

Spring AI provides:

* Provider abstraction
* Structured outputs
* Native Spring integration
* Reduced implementation complexity

---

### Why Dedicated AI Service?

AI concerns evolve independently from traditional business logic.

A separate AI Service provides:

* Independent deployment
* Easier experimentation
* Reduced coupling
* Future extensibility

---

### Why Centralized Configuration?

Managing configuration across multiple services becomes difficult at scale.

Config Server provides:

* Single source of truth
* Environment separation
* Simplified deployments

---

### Why Service Discovery?

Hardcoded service addresses do not scale.

Service discovery provides:

* Dynamic routing
* Deployment flexibility
* Simplified infrastructure management

---

### Why Cloud-Native Architecture?

Modern systems must support growth, resilience, and operational flexibility.

The platform is designed around:

* Scalability
* Resilience
* Automation
* Maintainability

---

# 🚧 Development Roadmap

## Completed

* API Gateway
* Auth Service
* User Service
* AI Service
* Config Server
* Service Registry

---

## In Progress

* Appointment Service
* Patient Service
* Notification Service

---

## Planned

* Distributed tracing
* OpenTelemetry integration
* Monitoring dashboards
* Kubernetes deployment
* Advanced AI workflows
* Healthcare analytics

---

# 🐳 Local Development

Infrastructure is containerized using Docker.

Core infrastructure:

* MySQL
* Redis
* Kafka
* MinIO

Start infrastructure:

```bash
docker compose up -d
```

---

# 📜 API Documentation

Swagger documentation is available for implemented services.

API documentation can be accessed through individual service Swagger UI endpoints.

---

# 👨‍💻 Author

**Ruslan Senkin**

Java Backend Developer

Specialization:

* Java
* Spring Boot
* Spring Cloud
* Spring Security
* Apache Kafka
* Event-Driven Architecture
* Distributed Systems
* Spring AI
* Cloud-Native Development
* Microservice Architecture

---

# ⭐ Project Purpose

This project serves as a practical demonstration of modern backend engineering practices and production-oriented software architecture.

It showcases real-world approaches to:

* Secure authentication
* Distributed systems
* Event-driven communication
* AI integration
* Infrastructure automation
* Cloud-native application design

with a strong focus on maintainability, scalability, reliability, and clean architectural boundaries.
