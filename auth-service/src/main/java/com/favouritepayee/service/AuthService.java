package com.favouritepayee.service;

import com.favouritepayee.dto.LoginResponse;
import com.favouritepayee.exception.BadRequestException;
import com.favouritepayee.repository.CustomerRepository;
import com.favouritepayee.security.JwtUtil;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final JwtUtil jwtUtil;

    public AuthService(CustomerRepository customerRepository, JwtUtil jwtUtil) {
        this.customerRepository = customerRepository;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(Long customerId) {
        if (customerId == null || !customerRepository.existsById(customerId)) {
            throw new BadRequestException("Invalid customerId", Map.of("customerId", "customer does not exist"));
        }
        return new LoginResponse(jwtUtil.generateToken(customerId));
    }
}
