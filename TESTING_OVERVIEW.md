# Favourite Payee - Testing Strategy & Documentation

This document provides a comprehensive overview of how the "Favourite Payee" application is validated across different layers of the technology stack.

---

## 1. Unit Testing (Backend - JUnit & Mockito)
We use a **Mock-driven Unit Testing** strategy for our microservices. This ensures that business logic is tested in isolation without relying on a database or network.

### Key Scenarios Tested in `PayeeServiceTest`:
- **Data Integrity**: Verifies that when a payee is created, all fields (Name, IBAN, Bank) are correctly mapped and saved.
- **Security Validation**: Ensures that a user can *only* retrieve or delete payees that belong to their own `customerId`.
- **Error Resiliency**: Validates that the system throws clear, structured exceptions (e.g., `BadRequestException`) when an invalid ID is requested.
- **Mocking**: We mock the `FavouriteAccountRepository` and `PayeeInteractionRepository` to simulate various database states (Success, Not Found, Duplicate) instantly.

---

## 2. Functional & Behavioral Testing (UI)
These tests verify that the frontend and backend work together to provide a "Smart" user experience.

### Scenario A: The Smart Promotion Loop
- **Test**: Log multiple interactions for a payee at the current hour.
- **Logic**: The **Scoring Service** calculates a "Time Habit" score. 
- **Verification**: Ensure the payee is automatically moved from the "All Accounts" list to the "Smart Favourites" section.

### Scenario B: Intelligent Data Resolution
- **Test**: Enter the first 8 digits of a Nairobi Bank IBAN (`ABCD1234`).
- **Logic**: The frontend calls the **Bank Resolver API**.
- **Verification**: The "Bank" field must update to "Nairobi Bank" automatically, reducing user data entry errors.

### Scenario C: Read-Only Data Interaction
- **Test**: Click on a payee card body.
- **Logic**: Navigate to the `view` route.
- **Verification**: The system must display the data but **disable all inputs**, preventing accidental modifications while browsing.

---

## 3. Security & API Gateway Testing
- **JWT Protection**: We verify that calling any API without a valid `Authorization: Bearer <token>` header results in a `401 Unauthorized` error.
- **CORS Safety**: We test that the API Gateway correctly allows requests from our Netlify/Local frontend while blocking unknown domains.
- **Route Guarding**: We verify that the Angular frontend automatically kicks users back to the `/login` screen if their session expires.

---

## 4. How to Execute the Suite
- **Unit Tests**: Run `mvn test` in the `payee-service` directory.
- **Functional Tests**: Follow the step-by-step guide in `TEST_CASES.md` using the UI.
