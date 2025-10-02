# Bakery Order Service

## Overview
Handles bakery order lifecycle including creation, tracking, updates, and history.

## Features
- Order submission and validation
- Payment integration hooks
- Order status change tracking
- API for order history and details

## Dependencies
- Spring WebFlux
- Spring Data JPA
- Spring Security
- Spring Boot Actuator

## Key Endpoints
- `/api/orders/`
- `/api/orders/{orderId}`

## Running
./gradlew bootRun

Runs on port 8084 by default.

## Documentation
Swagger UI: `http://localhost:8084/swagger-ui.html`

---