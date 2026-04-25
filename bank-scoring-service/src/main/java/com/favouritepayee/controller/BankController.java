package com.favouritepayee.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.favouritepayee.dto.BankResolveResponse;
import com.favouritepayee.service.BankResolverService;

@RestController
public class BankController {

    private final BankResolverService bankResolverService;

    public BankController(BankResolverService bankResolverService) {
        this.bankResolverService = bankResolverService;
    }

    @GetMapping("/banks/resolve")
    public ResponseEntity<BankResolveResponse> resolve(@RequestParam String iban) {
        return ResponseEntity.ok(new BankResolveResponse(bankResolverService.resolveBankNameOrThrow(iban)));
    }
}
