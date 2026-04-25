# Routing Architecture Guide

This document explains how requests travel through the "Favourite Payee" system, from the user's browser to the individual microservices.

---

## 1. Unified Entry Point: API Gateway
The system uses **Spring Cloud Gateway** (running on Port **8080**) as the single entry point for all frontend requests.

### Advantages:
- **Simplified Frontend**: The Angular app only needs to know one base URL (`http://localhost:8080`).
- **CORS Management**: Cross-Origin Resource Sharing is handled centrally at the Gateway, preventing security blocks.
- **Service Discovery**: The Gateway maps paths to specific internal services.

### Route Definitions:
| Path Prefix | Destination Service | Internal Port |
| :--- | :--- | :--- |
| `/auth/**` | Auth Service | `8081` |
| `/api/customers/**` | Payee Service | `8082` |
| `/banks/**` | Bank/Scoring Service | `8083` |
| `/scoring/**` | Bank/Scoring Service | `8083` |

---

## 2. Path Rewriting & Compatibility
To ensure the backend APIs remain clean while supporting different frontend requirements, we use **Rewrite Rules**.

**Example**:
When the frontend calls `http://localhost:8080/customers/1`, the Gateway uses a `RewritePath` filter:
1. Matches `/customers/(?<segment>.*)`
2. Rewrites to `/api/customers/${segment}`
3. Forwards to `payee-service` on port `8082`.

This abstraction allows us to change the internal API structure without breaking the frontend.

---

## 3. Frontend Routing (Angular)
The frontend is a **Single Page Application (SPA)**. Routing is handled in the browser using the Angular Router.

### Core Routes:
- `login`: Authentication page.
- `payees`: The main dashboard (Smart Favourites + All Accounts).
- `payees/view/:id`: Read-only detail view of a payee.
- `payees/edit/:id`: Edit form for an existing payee.
- `payees/add`: Form to create a new payee.

### Guarding:
All routes except `/login` are protected by an `AuthGuard`. If a user attempts to access them without a valid JWT token, they are automatically redirected to the login page.
