# 🤖 AI Service

Artificial Intelligence Service of the Healthcare System platform.

The AI Service provides healthcare-focused AI capabilities through a dedicated microservice architecture, enabling medical note summarization, medical information extraction, and message classification.

Built using Spring AI, the service follows a structured-output approach, allowing AI responses to be mapped directly into strongly typed Java DTOs.

The service is designed to isolate AI-related concerns from business services while providing a scalable foundation for future AI-powered healthcare functionality.

---

# 📖 Overview

AI Service acts as the centralized AI integration layer of the Healthcare System.

Responsibilities include:

* Medical note summarization
* Medical information extraction
* Patient message classification
* Prompt management
* AI provider abstraction
* Structured AI responses
* AI usage monitoring
* AI response validation

The service exposes domain-specific AI capabilities through REST APIs while keeping AI implementation details hidden from consuming services.

---

# 🚀 Core Features

## 🩺 Medical Note Summarization

Transforms unstructured medical notes into structured summaries.

Capabilities:

* Clinical summary generation
* Diagnosis extraction
* Medication extraction
* Recommendation extraction

Example output:

* Summary
* Diagnoses
* Medications
* Recommendations

This enables healthcare professionals to process large volumes of medical information more efficiently.

---

## 🔬 Medical Information Extraction

Extracts structured medical data from free-text clinical notes.

Examples:

* Symptoms
* Diagnoses
* Medications
* Medical observations

Benefits:

* Structured data generation
* Faster information retrieval
* Improved data consistency
* Reduced manual processing

---

## 📬 Message Classification

Classifies healthcare-related messages into predefined categories.

Potential use cases:

* Appointment requests
* Administrative inquiries
* Medical questions
* Urgent messages

This functionality supports intelligent workflow automation.

---

## 🧠 Structured AI Responses

AI responses are mapped directly into Java DTOs.

Benefits:

* Strong typing
* Validation support
* Reduced parsing complexity
* Safer integrations

Instead of returning raw text, the service produces predictable structured responses suitable for backend systems.

---

## 🧩 Prompt Provider Architecture

Prompt generation is separated from business logic.

Each feature contains its own:

* Prompt provider
* Request DTO
* Response DTO
* Service implementation

Benefits:

* Better maintainability
* Easier prompt evolution
* Clear separation of concerns
* Improved testability

---

## 🔄 AI Provider Abstraction

The service uses an abstraction layer between business features and AI providers.

Current implementation supports:

* Spring AI
* Local LLM providers
* Future cloud-based providers

Benefits:

* Reduced vendor lock-in
* Easier provider replacement
* Improved flexibility

---

# 🏗 Architecture

```text
Client
   │
   ▼
AI Controller
   │
   ▼
Feature Service
   │
   ▼
Prompt Provider
   │
   ▼
AI Client Abstraction
   │
   ▼
Spring AI
   │
   ▼
LLM Provider
```

This architecture isolates AI-specific implementation details from business functionality.

---

# 🧠 AI Processing Flow

```text
User Request
       │
       ▼
Request DTO
       │
       ▼
Prompt Provider
       │
       ▼
AI Client
       │
       ▼
LLM Response
       │
       ▼
Structured Output Mapping
       │
       ▼
Response DTO
```

This approach creates predictable and strongly typed AI integrations.

---

# 📊 AI Usage Monitoring

The service contains dedicated AI usage logging mechanisms.

Tracked information may include:

* Feature name
* Execution time
* Request metadata
* Response processing information

Benefits:

* Operational visibility
* AI usage analytics
* Performance monitoring
* Troubleshooting support

---

# 📝 AI Parsing Error Handling

AI output is inherently probabilistic.

The service contains dedicated handling for:

* Response mapping failures
* Structured output parsing errors
* Invalid AI responses

Benefits:

* Improved reliability
* Better diagnostics
* Safer AI integration

---

# 🔒 Security Design

The service operates behind API Gateway and relies on platform-level authentication and authorization mechanisms.

Security considerations include:

* Controlled API exposure
* DTO validation
* Structured response contracts
* Centralized authentication

This minimizes risks associated with AI-driven processing.

---

# ⚙ Technology Stack

## Backend

* Java
* Spring Boot

## AI

* Spring AI
* ChatClient API
* Structured Output Mapping

## Architecture

* DTO-based contracts
* Prompt Provider Pattern
* AI Client Abstraction

## Documentation

* OpenAPI
* Swagger UI

## Testing

* JUnit 5
* Mockito
* Spring Boot Test

---

# 🧪 Testing Strategy

The service includes tests for:

### Business Features

* Medical summaries
* Medical extraction
* Message classification

### Prompt Providers

* Prompt generation
* Prompt formatting

### AI Integration

* AI client abstraction
* Structured response mapping

### Error Handling

* Parsing failures
* Validation scenarios

Testing tools:

* JUnit 5
* Mockito
* Spring Boot Test

---

# 📡 Available AI Features

## Medical Summary

Generate structured summaries from medical notes.

---

## Medical Information Extraction

Extract healthcare-related information from unstructured text.

---

## Message Classification

Classify patient and healthcare communication.

---

## Planned Features

Future capabilities may include:

* Medical explanation generation
* Doctor preparation assistance
* Question generation
* Message rewriting
* Appointment preparation
* Clinical decision support

The architecture is designed to support additional AI features with minimal changes.

---

# ⚙ Configuration

Configuration is externalized through Spring Cloud Config Server.

Examples:

* AI model settings
* Provider configuration
* Feature toggles
* Prompt settings
* Request limits

This enables centralized management across environments.

---

# 📜 API Documentation

Swagger UI:

```text
http://localhost:8087/swagger-ui.html
```

---

# 🐳 Infrastructure Dependencies

Required services:

* Config Server
* Service Registry

Optional:

* Ollama
* OpenAI-compatible providers
* Future AI providers

---

# 📁 Project Structure

```text
ai-service
├── common
│   ├── medical_summary
│   ├── medical_extraction
│   ├── message_classification
│   ├── prompt
│   └── provider
│
├── config
├── exception
├── validation
└── resources
```

---

# 🤔 Why This Design?

### Why a Dedicated AI Service?

AI functionality evolves much faster than traditional business logic.

Separating AI into its own service provides:

* Independent deployment
* Easier experimentation
* Reduced coupling
* Better maintainability

Business services remain focused on domain logic while AI concerns stay isolated.

---

### Why Structured Output Instead of Raw Text?

Backend systems require predictable contracts.

Structured outputs provide:

* Type safety
* Validation support
* Easier integrations
* Reduced parsing complexity

This makes AI functionality suitable for production systems.

---

### Why Prompt Providers?

Embedding prompts directly inside services quickly becomes difficult to maintain.

Prompt Providers offer:

* Separation of concerns
* Easier prompt evolution
* Better testing
* Cleaner architecture

---

### Why an AI Client Abstraction?

Direct dependency on a single AI provider creates vendor lock-in.

An abstraction layer provides:

* Provider flexibility
* Easier migrations
* Better maintainability
* Future extensibility

---

### Why Spring AI?

Spring AI provides a unified programming model for LLM integrations.

Benefits:

* Native Spring integration
* Structured output support
* Reduced boilerplate
* Provider abstraction

---

### Why Separate AI Features by Domain?

Each AI capability has distinct requirements.

Feature-oriented organization improves:

* Scalability
* Readability
* Maintainability
* Team collaboration

while keeping business concerns isolated.

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
* Spring AI
* Microservices
* Distributed Systems
* API Design
* Cloud-Native Development
* AI Integration
* Event-Driven Architecture
