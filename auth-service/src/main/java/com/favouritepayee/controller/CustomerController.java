package com.favouritepayee.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.favouritepayee.repository.CustomerRepository;

@RestController
@RequestMapping("/auth/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Void> exists(@PathVariable Long id) {
        return customerRepository.existsById(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}