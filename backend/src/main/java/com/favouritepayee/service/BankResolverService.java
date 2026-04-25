package com.favouritepayee.service;

import com.favouritepayee.entity.Bank;
import com.favouritepayee.exception.BadRequestException;
import com.favouritepayee.repository.BankRepository;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BankResolverService {

    private final BankRepository bankRepository;

    public BankResolverService(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    public Optional<String> resolveBankName(String iban) {
        return Optional.ofNullable(iban)
                .map(String::trim)
                .filter(value -> value.length() >= 8)
                .map(value -> value.substring(4, 8))
                .flatMap(bankRepository::findById)
                .map(Bank::getBankName);
    }

    public String resolveBankNameOrThrow(String iban) {
        if (iban == null || iban.trim().length() < 8) {
            throw new BadRequestException("Invalid iban", Map.of("iban", "iban must be at least 8 characters"));
        }
        return resolveBankName(iban)
                .orElseThrow(() -> new BadRequestException("Invalid bank code", Map.of("iban", "bank code not found")));
    }
}
