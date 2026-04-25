package com.favouritepayee.controller;

import com.favouritepayee.dto.PayeeDto;
import com.favouritepayee.dto.PayeeListResponse;
import com.favouritepayee.dto.PayeeRequest;
import com.favouritepayee.exception.BadRequestException;
import com.favouritepayee.service.PayeeService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers/{customerId}/payees")
public class PayeeController {

    private final PayeeService payeeService;

    public PayeeController(PayeeService payeeService) {
        this.payeeService = payeeService;
    }

    @GetMapping
    public ResponseEntity<PayeeListResponse> getPayees(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "") String search,
            Authentication authentication
    ) {
        assertCustomerAccess(customerId, authentication);
        return ResponseEntity.ok(payeeService.getPayees(customerId, page, size, search));
    }

    @GetMapping("/{payeeId}")
    public ResponseEntity<PayeeDto> getPayeeById(
            @PathVariable Long customerId,
            @PathVariable Long payeeId,
            Authentication authentication
    ) {
        assertCustomerAccess(customerId, authentication);
        return ResponseEntity.ok(payeeService.getPayeeById(customerId, payeeId));
    }

    @PostMapping
    public ResponseEntity<PayeeDto> createPayee(
            @PathVariable Long customerId,
            @Valid @RequestBody PayeeRequest request,
            Authentication authentication
    ) {
        assertCustomerAccess(customerId, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(payeeService.createPayee(customerId, request));
    }

    @PutMapping("/{payeeId}")
    public ResponseEntity<PayeeDto> updatePayee(
            @PathVariable Long customerId,
            @PathVariable Long payeeId,
            @Valid @RequestBody PayeeRequest request,
            Authentication authentication
    ) {
        assertCustomerAccess(customerId, authentication);
        return ResponseEntity.ok(payeeService.updatePayee(customerId, payeeId, request));
    }

    @DeleteMapping("/{payeeId}")
    public ResponseEntity<Void> deletePayee(
            @PathVariable Long customerId,
            @PathVariable Long payeeId,
            Authentication authentication
    ) {
        assertCustomerAccess(customerId, authentication);
        payeeService.deletePayee(customerId, payeeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{payeeId}/interact")
    public ResponseEntity<Void> logInteraction(
            @PathVariable Long customerId,
            @PathVariable Long payeeId,
            Authentication authentication
    ) {
        assertCustomerAccess(customerId, authentication);
        payeeService.logInteraction(customerId, payeeId);
        return ResponseEntity.noContent().build();
    }

    private void assertCustomerAccess(Long customerId, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BadRequestException("Unauthorized", Map.of("token", "missing or invalid token"));
        }
        Long authenticatedCustomerId = Long.valueOf(authentication.getPrincipal().toString());
        if (!authenticatedCustomerId.equals(customerId)) {
            throw new BadRequestException("Customer mismatch", Map.of("customerId", "token does not match route customer"));
        }
    }
}
