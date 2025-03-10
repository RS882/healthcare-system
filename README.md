# Healthcare System

## 📌 Description
This is a microservices-based system for managing medical records, authentication, and notifications.

## 📂 Project Structure
- **auth-service** – Authentication and authorization service (Spring Security, JWT)
- **patient-service** – Patient management (Spring Boot, PostgreSQL)
- **appointment-service** – Appointment scheduling (Spring Boot, Kafka)
- **notification-service** – Notification handling (Spring Boot, RabbitMQ)
- **api-gateway** – API Gateway (Spring Cloud Gateway)
- **service-registry** – Service discovery and registration (Eureka)
- **config-server** – Centralized configuration management (Spring Cloud Config)

## 🚀 How to Run the Project
1. Start the infrastructure:
   ```bash
   docker-compose up -d