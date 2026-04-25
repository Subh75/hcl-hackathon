package com.favouritepayee.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.favouritepayee.dto.BankResolveResponse;
import com.favouritepayee.dto.PayeeDto;
import com.favouritepayee.dto.PayeeRequest;
import com.favouritepayee.dto.ScoringItemDto;
import com.favouritepayee.entity.FavouriteAccount;
import com.favouritepayee.exception.ResourceNotFoundException;
import com.favouritepayee.repository.FavouriteAccountRepository;
import com.favouritepayee.repository.PayeeInteractionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
        when(restTemplate.getForEntity(anyString(), eq(BankResolveResponse.class), anyString()))
                .thenReturn(ResponseEntity.ok(new BankResolveResponse("Nairobi Bank")));
        
        PayeeDto result = payeeService.createPayee(1L, sampleRequest);
        
        assertNotNull(result);
        assertEquals("John Doe", result.name());
        verify(payeeRepository, times(1)).save(any());
    }

    @Test
    void testGetPayeeById_Success() {
        when(payeeRepository.findByIdAndCustomerId(1L, 1L)).thenReturn(Optional.of(samplePayee));
        mockScoresResponse();
        
        PayeeDto result = payeeService.getPayeeById(1L, 1L);
        
        assertEquals(1L, result.id());
        assertEquals("John Doe", result.name());
    }

    @Test
    void testGetPayeeById_NotFound() {
        when(payeeRepository.findByIdAndCustomerId(99L, 1L)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> payeeService.getPayeeById(1L, 99L));
    }

    @Test
    void testUpdatePayee_Success() {
        when(payeeRepository.findByIdAndCustomerId(1L, 1L)).thenReturn(Optional.of(samplePayee));
        when(payeeRepository.save(any(FavouriteAccount.class))).thenReturn(samplePayee);
        when(restTemplate.getForEntity(anyString(), eq(BankResolveResponse.class), anyString()))
                .thenReturn(ResponseEntity.ok(new BankResolveResponse("Nairobi Bank")));
        mockScoresResponse();
        
        PayeeRequest updateRequest = new PayeeRequest("Jane Doe", "WXYZ9876543210987654");
        
        PayeeDto result = payeeService.updatePayee(1L, 1L, updateRequest);
        
        assertNotNull(result);
        verify(payeeRepository).save(any());
    }

    @Test
    void testUpdatePayee_NotFound() {
        when(payeeRepository.findByIdAndCustomerId(1L, 1L)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> payeeService.updatePayee(1L, 1L, sampleRequest));
    }

    @Test
    void testDeletePayee_Success() {
        when(payeeRepository.findByIdAndCustomerId(1L, 1L)).thenReturn(Optional.of(samplePayee));
        doNothing().when(payeeRepository).delete(any());
        
        assertDoesNotThrow(() -> payeeService.deletePayee(1L, 1L));
        verify(payeeRepository, times(1)).delete(any());
    }

    @Test
    void testDeletePayee_NotFound() {
        when(payeeRepository.findByIdAndCustomerId(1L, 1L)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> payeeService.deletePayee(1L, 1L));
    }

    @Test
    void testMapToDto_InternalLogic() {
        when(payeeRepository.findByIdAndCustomerId(1L, 1L)).thenReturn(Optional.of(samplePayee));
        mockScoresResponse();
        
        PayeeDto result = payeeService.getPayeeById(1L, 1L);
        
        assertEquals(samplePayee.getName(), result.name());
        assertEquals(samplePayee.getIban(), result.iban());
    }

    private void mockScoresResponse() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class), anyLong()))
                .thenReturn(ResponseEntity.ok(List.of(new ScoringItemDto(1L, 0.5))));
    }
}
