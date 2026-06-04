# 📡 Service Registry

Service Discovery Server of the Healthcare System platform.

The Service Registry provides centralized service discovery and registration capabilities for all microservices within the platform.

Built on Netflix Eureka, it enables dynamic service location, removes the need for hardcoded service addresses, and supports scalable cloud-native deployments.

---

# 📖 Overview

Service Registry acts as the central service discovery component of the Healthcare System.

Responsibilities include:

* Service registration
* Service discovery
* Service health monitoring
* Dynamic endpoint resolution
* Runtime service location
* Infrastructure coordination

The registry maintains an up-to-date view of all active services in the platform.

---

# 🚀 Core Features

## 📍 Service Registration

Every microservice registers itself automatically during startup.

Examples:

* API Gateway
* Auth Service
* User Service
* AI Service

This removes the need for manually managing service URLs.

---

## 🔍 Service Discovery

Services locate each other dynamically through the registry.

Benefits:

* No hardcoded addresses
* Simplified deployments
* Environment independence
* Better scalability

---

## ❤️ Health Monitoring

The registry continuously monitors service availability.

Capabilities:

* Heartbeat tracking
* Service status monitoring
* Availability checks
* Automatic deregistration

Only healthy services remain discoverable.

---

## ⚖ Load Balancing Support

Multiple instances of the same service can be registered simultaneously.

Benefits:

* Horizontal scaling
* Improved fault tolerance
* Better resource utilization
* High availability

---

## ☁ Cloud-Native Infrastructure

The registry provides the foundation for dynamic service communication.

It enables:

* Independent deployments
* Dynamic scaling
* Infrastructure flexibility
* Runtime service discovery

---

# 🏗 Architecture

```text
                 Service Registry
                        │
      ┌─────────────────┼─────────────────┐
      ▼                 ▼                 ▼
 API Gateway      Auth Service      User Service
      │                 │                 │
      └─────────────────┼─────────────────┘
                        ▼
                   AI Service
```

All services register themselves and discover other services through the registry.

---

# 🔄 Service Discovery Flow

```text
Service Startup
       │
       ▼
Register With Eureka
       │
       ▼
Registry Updates
       │
       ▼
Service Discovery
       │
       ▼
Inter-Service Communication
```

This process allows services to communicate without knowing physical addresses beforehand.

---

# ☁ Eureka Integration

The registry is implemented using Netflix Eureka.

Features:

* Automatic service registration
* Service discovery
* Health checks
* Dynamic endpoint resolution
* Runtime topology awareness

---

# 📡 Service Communication

Instead of communicating through fixed URLs, services use logical names.

Example:

```text
lb://auth-service
lb://user-service
lb://ai-service
```

Benefits:

* Deployment flexibility
* Environment portability
* Simplified configuration
* Improved maintainability

---

# 🔒 Reliability Features

The registry contributes to system reliability by providing:

* Dynamic service lookup
* Instance awareness
* Health monitoring
* Fault isolation
* Automatic topology updates

This reduces operational overhead and improves resilience.

---

# ⚙ Technology Stack

## Backend

* Java
* Spring Boot

## Cloud Infrastructure

* Spring Cloud Netflix Eureka Server

## Service Discovery

* Eureka

## Testing

* JUnit 5
* Spring Boot Test

---

# 🧪 Testing Strategy

The service includes tests validating:

* Application startup
* Eureka initialization
* Spring context loading

Testing tools:

* JUnit 5
* Spring Boot Test

---

# 📊 Operational Benefits

Using a Service Registry provides:

### Scalability

Services can scale horizontally without configuration changes.

### Flexibility

Services can move between environments without affecting consumers.

### Maintainability

Configuration complexity is significantly reduced.

### Resilience

Service availability is continuously monitored.

---

# ⚙ Configuration

Typical configuration includes:

```yaml
spring:
  application:
    name: service-registry

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

The registry itself acts as the central discovery server and therefore does not register with another Eureka instance.

---

# 📜 Eureka Dashboard

Service Registry Dashboard:

```text
http://localhost:8761
```

The dashboard provides visibility into:

* Registered services
* Service instances
* Availability status
* Discovery information

---

# 🐳 Infrastructure Dependencies

This service has no mandatory runtime dependencies.

Other platform services depend on the registry for service discovery.

---

# 📁 Project Structure

```text
service-registry
├── config
├── resources
├── monitoring
└── tests
```

---

# 🤔 Why This Design?

### Why Service Discovery?

In distributed systems, service locations change frequently.

Hardcoded URLs create:

* Deployment challenges
* Configuration complexity
* Scalability limitations

Service discovery solves these problems by providing dynamic endpoint resolution.

---

### Why Eureka?

Eureka is a proven service discovery solution with strong Spring Cloud integration.

Benefits:

* Native Spring support
* Easy configuration
* Dynamic registration
* Production-proven architecture

---

### Why Centralized Discovery?

Without centralized discovery, every service would need to know the location of every other service.

Centralizing discovery provides:

* Simplified architecture
* Reduced configuration
* Easier scaling
* Better maintainability

---

### Why Logical Service Names?

Using logical names such as:

```text
lb://auth-service
```

instead of:

```text
http://192.168.1.25:8081
```

provides:

* Environment independence
* Dynamic scaling
* Simplified deployments
* Reduced operational overhead

---

### Why Separate Infrastructure From Business Services?

Infrastructure concerns should remain independent from business logic.

A dedicated registry improves:

* Architectural clarity
* Reusability
* Maintainability
* Scalability

while allowing business services to focus entirely on domain functionality.

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
* Microservices
* Distributed Systems
* Cloud-Native Architecture
* Service Discovery
* Infrastructure Design
