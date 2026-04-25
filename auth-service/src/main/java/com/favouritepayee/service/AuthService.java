package com.favouritepayee.service;

import com.favouritepayee.dto.*;
import com.favouritepayee.entity.Customer;
import com.favouritepayee.entity.RefreshToken;
import com.favouritepayee.entity.Role;
import com.favouritepayee.exception.BadRequestException;
import com.favouritepayee.repository.CustomerRepository;
import com.favouritepayee.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthService(CustomerRepository customerRepository,
                       JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder,
                       RefreshTokenService refreshTokenService) {
        this.customerRepository = customerRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    public LoginResponse login(Long customerId, String password) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BadRequestException(
                        "Invalid credentials",
                        Map.of("customerId", "customer does not exist")));

        if (!passwordEncoder.matches(password, customer.getPassword())) {
            throw new BadRequestException(
                    "Invalid credentials",
                    Map.of("password", "incorrect password"));
        }

        String accessToken = jwtUtil.generateToken(customer.getId(), customer.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(customer.getId());

        return new LoginResponse(accessToken, refreshToken.getToken(), customer.getRole().name());
    }

    public RefreshResponse refresh(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenStr)
                .orElseThrow(() -> new BadRequestException(
                        "Invalid refresh token",
                        Map.of("refreshToken", "refresh token is invalid or expired")));

        Customer customer = customerRepository.findById(refreshToken.getCustomerId())
                .orElseThrow(() -> new BadRequestException(
                        "Customer not found",
                        Map.of("customerId", "customer associated with refresh token does not exist")));

        // Rotate refresh token
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(customer.getId());
        String newAccessToken = jwtUtil.generateToken(customer.getId(), customer.getRole().name());

        return new RefreshResponse(newAccessToken, newRefreshToken.getToken());
    }

    public void logout(String refreshTokenStr) {
        refreshTokenService.deleteByToken(refreshTokenStr);
    }

    public RegisterResponse register(String name, String password) {
        if (customerRepository.existsByName(name)) {
            throw new BadRequestException(
                    "Registration failed",
                    Map.of("name", "username already exists"));
        }

        Customer customer = new Customer(
                name,
                passwordEncoder.encode(password),
                Role.USER
        );
        customer = customerRepository.save(customer);

        return new RegisterResponse(customer.getId(), customer.getName(), customer.getRole().name());
    }
}
