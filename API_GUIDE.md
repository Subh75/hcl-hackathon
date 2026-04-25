# API Development & Architectural Design

This document outlines the design principles and patterns used to build the "Favourite Payee" microservices.

---

## 1. Architectural Pattern: Layered Architecture
Each microservice is built using a strict layered approach to ensure separation of concerns:

1. **Controller Layer**: Handles HTTP requests, validates input, and maps data to/from the Service layer.
2. **Service Layer**: Contains the core business logic. It is independent of the transport layer (HTTP) and the data storage layer.
3. **Repository Layer**: Interfaces with the H2 database using Spring Data JPA.
4. **Entity Layer**: Represents the database schema as Java objects.

---

## 2. Key Design Principles

### Data Transfer Objects (DTOs)
We never expose our database entities directly to the API. We use DTOs to:
- Prevent data leakage (e.g., hiding internal database IDs or metadata).
- Decouple the frontend from the database schema.
- Optimize the payload by sending only the required fields.

### Global Exception Handling
Every service includes a `GlobalExceptionHandler`. This component catches all exceptions (both custom and system) and transforms them into a standardized JSON format:
```json
{
  "timestamp": "2024-04-25T10:00:00Z",
  "status": 400,
  "message": "Validation failed",
  "errors": { "name": "Name is required" },
  "path": "/api/payees"
}
```

### JSR-303 Validation
We use standard Java validation annotations (`@NotBlank`, `@Size`, `@Pattern`) in our DTOs. This ensures that invalid data is rejected at the "door" (the Controller) before any business logic is executed.

---

## 3. Inter-Service Communication
The services communicate over REST. For instance:
- The **Payee Service** requests interaction scores from the **Scoring Service**.
- The **Payee Service** requests bank name resolution from the **Scoring Service**.

This decoupling allows each service to scale and be updated independently.
