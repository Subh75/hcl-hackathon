# Favourite Payee - Technical Documentation

## Project Summary
The **Favourite Payee** application is a microservice-based banking platform that simplifies payee management through intelligent automation. Its standout feature is the **Smart Favourites** system, which uses behavioral analytics to predict and surface the most relevant accounts based on a user's historical interaction patterns and specific time-of-day habits.

---

## 1. System Architecture
The application follows a microservices architecture with a centralized API Gateway and a dynamic scoring engine.

| Service | Port | Technology | Role |
| :--- | :--- | :--- | :--- |
| **Frontend** | `4200` | Angular 17 | User interface for payee management and interactions. |
| **API Gateway** | `8080` | Spring Cloud Gateway | Central entry point, CORS handler, and route orchestrator. |
| **Auth Service** | `8081` | Spring Boot (Java 21) | Manages `customers` and issues JWT security tokens. |
| **Payee Service** | `8082` | Spring Boot (Java 21) | Manages payee accounts and logs user interactions. |
| **Scoring Service**| `8083` | Spring Boot (Java 21) | Calculates payee scores based on interaction habits. |

---

## 2. Core Application Flows

### 2.1 The "Smart Favourite" Cycle
The application uses a dynamic ranking system rather than static favourites:
1. **Interaction**: When a user clicks a payee card, the frontend calls the `/interact` endpoint.
2. **Persistence**: The Payee Service logs the timestamp and customer-payee pair.
3. **Scoring**: The Scoring Service analyzes these logs using three weights:
   - **Frequency**: How many times you've interacted with them.
   - **Recency**: How long ago was the last interaction.
   - **Time habit**: Does the current time match your historical pattern for this payee?
4. **Promotion**: High-scoring payees are automatically promoted to the "Smart Favourites" section on the dashboard.

### 2.2 Security & Authentication
- **JWT**: The Auth service issues a token upon login.
- **Validation**: Every request to the Gateway must include a `Bearer` token.
- **Propagation**: The Gateway passes the token to downstream services, which validate that the `customerId` in the path matches the `customerId` in the token.

---

## 3. API Endpoints

### Auth Service (8081)
- `POST /auth/login`: Authenticates a user and returns a JWT token.

### Payee Service (8082)
- `GET /api/customers/{id}/payees`: Returns a paginated list of all payees and the top 3 Smart Favourites.
- `GET /api/customers/{id}/payees/{payeeId}`: Returns details for a specific payee.
- `POST /api/customers/{id}/payees`: Adds a new payee.
- `PUT /api/customers/{id}/payees/{payeeId}`: Updates an existing payee.
- `DELETE /api/customers/{id}/payees/{payeeId}`: Removes a payee.
- `POST /api/customers/{id}/payees/{payeeId}/interact`: Logs a user interaction for scoring.

### Bank/Scoring Service (8083)
- `GET /banks/resolve?iban=...`: Resolves the bank name from the 4-7th digits of an IBAN.
- `GET /scoring/{customerId}`: Internal endpoint used by Payee Service to get calculated scores.

---

## 4. Frontend Routing (Angular)

- `/login`: Login screen.
- `/payees`: Main dashboard with Smart Favourites and the All Accounts list.
- `/payees/add`: Form to create a new payee.
- `/payees/edit/:id`: Form to update an existing payee.
- `/payees/view/:id`: **Read-only detail view** (triggered by clicking a payee card).

---

## 5. Critical Stability Fixes Implemented

1. **JWT Secret Strength**: Fixed `WeakKeyException` by increasing the secret key length to 256-bit (HS256 requirement).
2. **H2 Initialization**: Resolved `Table not found` errors by removing `MODE=PostgreSQL` and ensuring `spring.sql.init.mode=always` runs after Hibernate DDL.
3. **CORS Centralization**: Fixed duplicate header errors by disabling CORS on microservices and managing it entirely at the API Gateway level.
4. **Clean Builds**: Updated `run-all.ps1` to force `mvn clean` on every start, preventing stale configuration or empty `data.sql` crashes.
