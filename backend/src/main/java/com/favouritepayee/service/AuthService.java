package com.favouritepayee.service;

import com.favouritepayee.dto.LoginResponse;
import com.favouritepayee.exception.BadRequestException;
import com.favouritepayee.repository.CustomerRepository;
import com.favouritepayee.security.JwtService;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final JwtService jwtService;

    public AuthService(CustomerRepository customerRepository, JwtService jwtService) {
        this.customerRepository = customerRepository;
        this.jwtService = jwtService;
    }

    public LoginResponse login(Long customerId) {
        if (customerId == null || !customerRepository.existsById(customerId)) {
            throw new BadRequestException("Invalid customerId", Map.of("customerId", "customer does not exist"));
        }
        return new LoginResponse(jwtService.generateToken(customerId));
    }
}
