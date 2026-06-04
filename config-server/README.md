# ⚙️ Config Server

Centralized Configuration Service of the Healthcare System platform.

The Config Server provides a single source of truth for configuration management across all microservices, enabling consistent configuration, environment separation, simplified deployments, and operational flexibility.

Built with Spring Cloud Config Server, it eliminates configuration duplication and supports centralized management of application settings throughout the entire platform.

---

# 📖 Overview

Config Server is responsible for delivering externalized configuration to all services within the Healthcare System.

Responsibilities include:

* Centralized configuration management
* Environment-specific configuration delivery
* Configuration versioning
* Configuration consistency
* Service bootstrap configuration
* Operational flexibility

The service acts as the configuration backbone of the microservice ecosystem.

---

# 🚀 Core Features

## 📂 Centralized Configuration Management

All service configurations are maintained in a single location.

Examples:

* Database settings
* Kafka configuration
* Redis configuration
* Security settings
* JWT configuration
* Service-specific properties
* Environment settings

Benefits:

* Reduced duplication
* Easier maintenance
* Consistent configuration
* Simplified deployments

---

## 🌍 Environment Separation

Supports multiple environments.

Examples:

* Development
* Testing
* Staging
* Production

Each environment can maintain independent configuration while preserving a consistent deployment model.

---

## 🔄 Dynamic Configuration Delivery

Services retrieve configuration during startup.

This allows:

* Centralized property management
* Simplified service configuration
* Reduced deployment complexity
* Better operational control

---

## 📦 Service Bootstrap Support

The Config Server provides configuration during application bootstrap.

Examples:

* Service names
* Database connections
* Kafka topics
* Redis settings
* Security properties
* External integrations

This ensures all services start with a consistent configuration state.

---

# 🏗 Architecture

```text
                Config Repository
                        │
                        ▼
                Config Server
                        │
      ┌─────────────────┼─────────────────┐
      ▼                 ▼                 ▼
 API Gateway      Auth Service      User Service
      │                 │                 │
      └─────────────────┼─────────────────┘
                        ▼
                  Other Services
```

The Config Server acts as the centralized configuration provider for the entire platform.

---

# ⚙️ Configuration Flow

```text
Service Startup
       │
       ▼
Request Configuration
       │
       ▼
Config Server
       │
       ▼
Configuration Repository
       │
       ▼
Configuration Response
       │
       ▼
Service Initialization
```

This process ensures every service starts with the correct environment-specific configuration.

---

# ☁ Spring Cloud Integration

The service leverages Spring Cloud Config to provide:

* Centralized configuration
* Environment support
* Property versioning
* Service consistency
* Cloud-native configuration management

---

# 🔒 Security Considerations

Configuration often contains infrastructure-related settings that should not be duplicated across services.

Centralizing configuration provides:

* Better governance
* Reduced configuration drift
* Easier auditing
* Simplified maintenance

Sensitive values can be managed through environment variables or secret management solutions.

---

# ⚙ Technology Stack

## Backend

* Java
* Spring Boot

## Cloud Infrastructure

* Spring Cloud Config Server

## Configuration

* YAML
* Property Sources

## Testing

* JUnit 5
* Spring Boot Test

---

# 🧪 Testing Strategy

The service includes tests verifying:

* Application startup
* Configuration loading
* Spring context initialization

Testing tools:

* JUnit 5
* Spring Boot Test

---

# 📡 Service Integration

The Config Server provides configuration to all platform services.

Examples:

* API Gateway
* Auth Service
* User Service
* AI Service

Each service retrieves configuration during startup through Spring Cloud Config.

---

# ⚙ Example Configuration

```yaml
spring:
  application:
    name: auth-service

  cloud:
    config:
      uri: http://localhost:8888
```

This allows services to externalize configuration and avoid environment-specific values inside application code.

---

# 🐳 Infrastructure Dependencies

Required:

* Configuration Repository

Optional:

* Git-based configuration storage
* Secret management solutions

---

# 📁 Project Structure

```text
config-server
├── config
├── resources
├── bootstrap
└── tests
```

---

# 🤔 Why This Design?

### Why a Dedicated Config Server?

Managing configuration independently inside every service quickly becomes difficult as the platform grows.

A centralized Config Server provides:

* Single source of truth
* Easier maintenance
* Consistent deployments
* Better operational control

---

### Why Externalized Configuration?

Configuration changes should not require code changes.

Benefits:

* Faster deployments
* Environment flexibility
* Reduced duplication
* Cleaner codebase

---

### Why Centralized Management?

Without centralized management, configuration drift becomes a significant operational problem.

Centralized configuration ensures:

* Consistency
* Traceability
* Predictability
* Easier troubleshooting

---

### Why Spring Cloud Config?

Spring Cloud Config is a mature solution designed specifically for distributed systems.

Benefits:

* Native Spring integration
* Environment support
* Cloud-native architecture
* Simplified configuration management

---

### Why Separate Infrastructure Concerns From Business Services?

Business services should focus on business logic.

Moving configuration management into a dedicated infrastructure service improves:

* Maintainability
* Scalability
* Operational flexibility
* Architectural clarity

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
* Configuration Management
* Infrastructure Design
