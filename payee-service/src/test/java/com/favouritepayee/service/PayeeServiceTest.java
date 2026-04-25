package com.favouritepayee.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.favouritepayee.dto.PayeeDto;
import com.favouritepayee.dto.PayeeRequest;
import com.favouritepayee.entity.FavouriteAccount;
import com.favouritepayee.exception.BadRequestException;
import com.favouritepayee.repository.FavouriteAccountRepository;
import com.favouritepayee.repository.PayeeInteractionRepository;
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
    private FavouriteAccountRepository payeeRepository;

    @Mock
    private PayeeInteractionRepository interactionRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PayeeService payeeService;

    private FavouriteAccount samplePayee;
    private PayeeRequest sampleRequest;

    @BeforeEach
    void setUp() {
        samplePayee = new FavouriteAccount();
        samplePayee.setId(1L);
        samplePayee.setCustomerId(1L);
        samplePayee.setName("John Doe");
        samplePayee.setIban("ABCD1234567890123456");
        samplePayee.setBank("Nairobi Bank");

        sampleRequest = new PayeeRequest("John Doe", "ABCD1234567890123456");
    }

    @Test
    void testCreatePayee_Success() {
        when(payeeRepository.save(any(FavouriteAccount.class))).thenReturn(samplePayee);
        // Mocking the bank resolver call if it's there
        // Actually, the service might call Scoring Service. Let's assume it's mocked or not needed for this unit test if it's in the service.
        
        PayeeDto result = payeeService.createPayee(1L, sampleRequest);
        
        assertNotNull(result);
        assertEquals("John Doe", result.name());
        verify(payeeRepository, times(1)).save(any());
    }

    @Test
    void testGetPayeeById_Success() {
        when(payeeRepository.findByCustomerIdAndId(1L, 1L)).thenReturn(Optional.of(samplePayee));
        
        PayeeDto result = payeeService.getPayeeById(1L, 1L);
        
        assertEquals(1L, result.id());
        assertEquals("John Doe", result.name());
    }

    @Test
    void testGetPayeeById_NotFound() {
        when(payeeRepository.findByCustomerIdAndId(1L, 99L)).thenReturn(Optional.empty());
        
        assertThrows(BadRequestException.class, () -> payeeService.getPayeeById(1L, 99L));
    }

    @Test
    void testUpdatePayee_Success() {
        when(payeeRepository.findByCustomerIdAndId(1L, 1L)).thenReturn(Optional.of(samplePayee));
        when(payeeRepository.save(any(FavouriteAccount.class))).thenReturn(samplePayee);
        
        PayeeRequest updateRequest = new PayeeRequest("Jane Doe", "WXYZ9876543210987654");
        
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
        when(payeeRepository.findByCustomerIdAndId(1L, 1L)).thenReturn(Optional.of(samplePayee));
        
        PayeeDto result = payeeService.getPayeeById(1L, 1L);
        
        assertEquals(samplePayee.getName(), result.name());
        assertEquals(samplePayee.getIban(), result.iban());
    }
}
