package com.favouritepayee.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.favouritepayee.dto.PayeeDto;
import com.favouritepayee.dto.PayeeRequest;
import com.favouritepayee.entity.Payee;
import com.favouritepayee.exception.BadRequestException;
import com.favouritepayee.repository.PayeeRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class PayeeServiceTest {

    @Mock
    private PayeeRepository payeeRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PayeeService payeeService;

    private Payee samplePayee;
    private PayeeRequest sampleRequest;

    @BeforeEach
    void setUp() {
        samplePayee = new Payee();
        samplePayee.setId(1L);
        samplePayee.setCustomerId(1L);
        samplePayee.setName("John Doe");
        samplePayee.setIban("ABCD123456789");

        sampleRequest = new PayeeRequest();
        sampleRequest.setName("John Doe");
        sampleRequest.setIban("ABCD123456789");
    }

    @Test
    void testCreatePayee_Success() {
        when(payeeRepository.save(any(Payee.class))).thenReturn(samplePayee);
        
        PayeeDto result = payeeService.createPayee(1L, sampleRequest);
        
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(payeeRepository, times(1)).save(any());
    }

    @Test
    void testGetPayeeById_Success() {
        when(payeeRepository.findByCustomerIdAndId(1L, 1L)).thenReturn(Optional.of(samplePayee));
        
        PayeeDto result = payeeService.getPayeeById(1L, 1L);
        
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testGetPayeeById_NotFound() {
        when(payeeRepository.findByCustomerIdAndId(1L, 99L)).thenReturn(Optional.empty());
        
        assertThrows(BadRequestException.class, () -> payeeService.getPayeeById(1L, 99L));
    }

    @Test
    void testUpdatePayee_Success() {
        when(payeeRepository.findByCustomerIdAndId(1L, 1L)).thenReturn(Optional.of(samplePayee));
        when(payeeRepository.save(any(Payee.class))).thenReturn(samplePayee);
        
        PayeeRequest updateRequest = new PayeeRequest();
        updateRequest.setName("Jane Doe");
        updateRequest.setIban("WXYZ987654321");
        
        PayeeDto result = payeeService.updatePayee(1L, 1L, updateRequest);
        
        assertNotNull(result);
        verify(payeeRepository).save(any());
    }

    @Test
    void testUpdatePayee_NotFound() {
        when(payeeRepository.findByCustomerIdAndId(1L, 1L)).thenReturn(Optional.empty());
        
        assertThrows(BadRequestException.class, () -> payeeService.updatePayee(1L, 1L, sampleRequest));
    }

    @Test
    void testDeletePayee_Success() {
        when(payeeRepository.findByCustomerIdAndId(1L, 1L)).thenReturn(Optional.of(samplePayee));
        doNothing().when(payeeRepository).delete(any());
        
        assertDoesNotThrow(() -> payeeService.deletePayee(1L, 1L));
        verify(payeeRepository, times(1)).delete(any());
    }

    @Test
    void testDeletePayee_NotFound() {
        when(payeeRepository.findByCustomerIdAndId(1L, 1L)).thenReturn(Optional.empty());
        
        assertThrows(BadRequestException.class, () -> payeeService.deletePayee(1L, 1L));
    }

    @Test
    void testMapToDto_InternalLogic() {
        // This implicitly tests the private mapping logic used across the service
        when(payeeRepository.findByCustomerIdAndId(1L, 1L)).thenReturn(Optional.of(samplePayee));
        
        PayeeDto result = payeeService.getPayeeById(1L, 1L);
        
        assertEquals(samplePayee.getName(), result.getName());
        assertEquals(samplePayee.getIban(), result.getIban());
    }
}
