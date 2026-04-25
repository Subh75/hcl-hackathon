# Code Walkthrough: Anatomy of a REST API

This guide explains how our APIs are constructed using the **Spring Boot Layered Architecture**. We will use the `Payee Service` (creating a new payee) as our primary example.

---

## 1. The Controller Layer (The Entry Point)
**File**: `PayeeController.java`
The Controller defines the public contract. It specifies the URL, the HTTP method, and handles request/response mapping.

```java
@RestController
@RequestMapping("/api/customers/{customerId}/payees")
public class PayeeController {

    // POST /api/customers/1/payees
    @PostMapping
    public ResponseEntity<PayeeDto> createPayee(
            @PathVariable Long customerId,
            @Valid @RequestBody PayeeRequest request
    ) {
        // 1. Inputs are automatically validated (@Valid)
        // 2. JSON is converted to PayeeRequest object
        // 3. Request is passed to the Service layer
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(payeeService.createPayee(customerId, request));
    }
}
```

---

## 2. The DTO Layer (Data Transfer)
**File**: `PayeeRequest.java` / `PayeeDto.java`
We use separate classes for incoming requests and outgoing responses. This keeps our internal database logic hidden from the user.

```java
public class PayeeRequest {
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9 '\\-]+$")
    private String name;

    @NotBlank
    @Size(max = 20)
    private String iban;
    
    // Getters and Setters
}
```

---

## 3. The Service Layer (The Business Logic)
**File**: `PayeeService.java`
The Service layer is where the "real work" happens. It handles logic that isn't simple CRUD (Create, Read, Update, Delete).

```java
@Service
public class PayeeService {
    public PayeeDto createPayee(Long customerId, PayeeRequest request) {
        // 1. Create a new database Entity from the request
        Payee entity = new Payee();
        entity.setCustomerId(customerId);
        entity.setName(request.getName());
        entity.setIban(request.getIban());

        // 2. Persist the entity using the Repository
        Payee saved = payeeRepository.save(entity);

        // 3. Convert the saved database object back to a DTO for the response
        return mapToDto(saved);
    }
}
```

---

## 4. The Repository Layer (Data Persistence)
**File**: `PayeeRepository.java`
Using Spring Data JPA, we don't write SQL queries manually. We define an interface, and Spring handles the database interaction.

```java
@Repository
public interface PayeeRepository extends JpaRepository<Payee, Long> {
    // Standard methods like save(), findById(), and delete() 
    // are automatically provided here.
}
```

---

## 5. The Entity Layer (Database Structure)
**File**: `Payee.java`
This class represents the actual table in our H2 database.

```java
@Entity
@Table(name = "payees")
public class Payee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String iban;
}
```

---

## Why this Architecture works:
- **Decoupling**: You can change the database table (Entity) without breaking the API contract (DTO).
- **Validation**: Errors are caught at the Controller level before they reach the database.
- **Maintainability**: Each class has one specific job, making it easy for SMEs and developers to locate specific logic.
