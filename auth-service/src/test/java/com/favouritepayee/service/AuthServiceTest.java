package com.favouritepayee.service;

import com.favouritepayee.dto.LoginResponse;
import com.favouritepayee.dto.RefreshResponse;
import com.favouritepayee.dto.RegisterResponse;
import com.favouritepayee.entity.Customer;
import com.favouritepayee.entity.RefreshToken;
import com.favouritepayee.entity.Role;
import com.favouritepayee.exception.BadRequestException;
import com.favouritepayee.repository.CustomerRepository;
import com.favouritepayee.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private Customer customer;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        customer = new Customer("testuser", "encodedPassword", Role.USER);
        customer.setId(1L);

        refreshToken = new RefreshToken("token-uuid", 1L, Instant.now().plusSeconds(3600));
    }

    @Test
    void login_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "USER")).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(refreshToken);

        LoginResponse response = authService.login(1L, "password");

        assertNotNull(response);
        assertEquals("access-token", response.token());
        assertEquals("token-uuid", response.refreshToken());
        assertEquals("USER", response.role());
    }

    @Test
    void login_InvalidCustomerId_ThrowsException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.login(99L, "password"));
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.login(1L, "wrongpassword"));
    }

    @Test
    void register_Success() {
        when(customerRepository.existsByName("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        
        Customer savedCustomer = new Customer("newuser", "encodedPassword", Role.USER);
        savedCustomer.setId(2L);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        RegisterResponse response = authService.register("newuser", "password");

        assertNotNull(response);
        assertEquals(2L, response.customerId());
        assertEquals("newuser", response.name());
        assertEquals("USER", response.role());
    }

    @Test
    void register_UsernameAlreadyExists_ThrowsException() {
        when(customerRepository.existsByName("existinguser")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register("existinguser", "password"));
    }

    @Test
    void refresh_Success() {
        when(refreshTokenService.validateRefreshToken("valid-token")).thenReturn(Optional.of(refreshToken));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        
        RefreshToken newRefreshToken = new RefreshToken("new-token-uuid", 1L, Instant.now().plusSeconds(3600));
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(newRefreshToken);
        when(jwtUtil.generateToken(1L, "USER")).thenReturn("new-access-token");

        RefreshResponse response = authService.refresh("valid-token");

        assertNotNull(response);
        assertEquals("new-access-token", response.token());
        assertEquals("new-token-uuid", response.refreshToken());
    }

    @Test
    void refresh_InvalidToken_ThrowsException() {
        when(refreshTokenService.validateRefreshToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.refresh("invalid-token"));
    }

    @Test
    void logout_Success() {
        authService.logout("valid-token");
        
        verify(refreshTokenService, times(1)).deleteByToken("valid-token");
    }
}
