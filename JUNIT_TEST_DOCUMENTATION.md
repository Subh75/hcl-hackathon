# JUnit Testing Documentation: Payee Service

This document provides a technical breakdown of the unit testing strategy for the **Payee Service**. We use **JUnit 5** and **Mockito** to ensure the reliability of our core CRUD operations.

---

## 1. Testing Strategy
Our goal is to test the **Service Layer** in isolation.
- **Mockito**: We mock the `PayeeRepository` so that we don't need a real database connection during testing.
- **Independence**: Each test is "stateless," meaning it sets up its own data and cleans up after itself.
- **Coverage**: We cover both "Happy Path" (success) and "Edge Case" (failure/exception) scenarios.

---

## 2. Test Case Breakdown

### 2.1 Creation & Retrieval
1.  **testCreatePayee_Success**: Ensures that when a valid `PayeeRequest` is sent, the service correctly maps it to an entity, saves it via the repository, and returns a DTO.
2.  **testGetPayeeById_Success**: Verifies that we can retrieve a specific payee using both the `customerId` and `payeeId` for security.
3.  **testGetPayeeById_NotFound**: Validates that the service throws a `BadRequestException` if a user tries to access a payee that doesn't exist.

### 2.2 Updates & Data Integrity
4.  **testUpdatePayee_Success**: Confirms that updating an existing record correctly triggers the `save()` method with the new data.
5.  **testUpdatePayee_NotFound**: Ensures that attempting to update a non-existent ID fails gracefully with an exception.
6.  **testMapToDto_InternalLogic**: A specialized test to ensure our internal mapping logic correctly transfers every field (ID, Name, IBAN, Bank) from the database to the API response.

### 2.3 Deletion
7.  **testDeletePayee_Success**: Verifies that the service calls the repository's `delete()` method when a valid ID is provided.
8.  **testDeletePayee_NotFound**: Ensures that we cannot delete a record that doesn't exist, preventing silent failures.

---

## 3. How to Run the Tests
You can execute this test suite from the root of the `payee-service` directory using Maven:

```bash
mvn test -Dtest=PayeeServiceTest
```

The output will show a summary of passed/failed tests, ensuring that your core business logic remains stable during future updates.
