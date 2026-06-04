# 🏥 Healthcare System

A modern cloud-native healthcare platform built with Java and Spring Boot using a microservice architecture.

The system provides secure patient management, user authentication, AI-powered medical note processing, event-driven communication, centralized configuration, service discovery, and API gateway routing.

---

# 🚀 Key Features

### Authentication & Authorization

* JWT Access Token authentication
* Refresh Token support
* Role-based access control
* Secure API Gateway validation
* Signed user context propagation between services

### User Management

* User registration
* User profile management
* User activation workflow
* Validation and error handling

### Event-Driven Architecture

* Apache Kafka integration
* Transactional Outbox Pattern
* Idempotent Consumers
* Retry and Dead Letter Topics (DLT)
* Event cleanup and recovery mechanisms

### AI Integration

* AI-powered medical note summarization
* Medical information extraction
* Message classification
* Local LLM support via Ollama
* Spring AI integration

### Cloud-Native Infrastructure

* Spring Cloud Gateway
* Eureka Service Discovery
* Spring Cloud Config Server
* Dockerized deployment
* Centralized configuration management

---

# 🏗 Architecture

Microservice-based architecture:

```text
Client
   │
   ▼
API Gateway
   │
   ├── Auth Service
   ├── User Service
   ├── AI Service
   │
   ▼
Infrastructure
   ├── MySQL
   ├── Redis
   ├── Kafka
   ├── MinIO
   ├── Eureka
   └── Config Server
```

---

# ⚙ Technology Stack

## Backend

* Java 17
* Spring Boot 3
* Spring Security
* Spring Cloud
* Spring AI
* Hibernate / JPA
* Liquibase

## Messaging

* Apache Kafka
* Outbox Pattern

## Databases

* MySQL
* Redis

## Storage

* MinIO (S3 Compatible)

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

# 📦 Services

## API Gateway

Central entry point for all client requests.

Responsibilities:

* Routing
* Authentication validation
* Request tracking
* User context propagation

---

## Auth Service

Authentication and authorization service.

Responsibilities:

* Login
* Token generation
* Token validation
* Refresh tokens

---

## User Service

User domain management.

Responsibilities:

* Registration
* User updates
* Event publishing

---

## AI Service

Medical AI assistant.

Responsibilities:

* Medical note summaries
* Medical information extraction
* Message classification

---

## Service Registry

Netflix Eureka server for service discovery.

---

## Config Server

Centralized configuration management.

---

# 🔐 Security

Implemented security mechanisms:

* JWT Authentication
* Role-Based Access Control
* Signed User Context (JWS)
* Secure Gateway Validation
* Request Tracking via UUID

---

# 📡 Event-Driven Architecture

The system uses Apache Kafka together with the Transactional Outbox Pattern.

Benefits:

* Reliable event delivery
* Event consistency
* Retry support
* Dead Letter Topics
* Consumer idempotency

Example flow:

```text
User Registration
        │
        ▼
Database Transaction
        │
        ▼
Outbox Event
        │
        ▼
Kafka Publisher
        │
        ▼
Kafka Topic
        │
        ▼
Consumers
```

---

# 🧪 Testing

Testing strategy:

* Unit Tests
* Integration Tests
* API Tests
* Kafka Integration Tests
* Testcontainers

Covered components:

* Services
* Controllers
* Repositories
* Kafka Producers
* Kafka Consumers
* Outbox Publishing

---

# 🐳 Running Locally

Start infrastructure:

```bash
docker compose up -d
```

Run services:

```bash
mvn spring-boot:run
```

---

# 📖 API Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

---

# 📁 Project Structure

```text
healthcare-system
│
├── api-gateway
├── auth-service
├── user-service
├── ai-service
├── service-registry
├── config-server
│
├── docker-compose.yml
└── README.md
```

---

# 🔮 Future Improvements

* Notification Service
* Appointment Service
* Patient Service
* Distributed Tracing
* OpenTelemetry
* Monitoring Dashboard
* Kubernetes Deployment
* AI-powered Clinical Assistant
* Multi-tenant Support
