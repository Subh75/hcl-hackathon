package com.favouritepayee.service;

import com.favouritepayee.dto.RawPayeeDataDto;
import com.favouritepayee.dto.ScoringItemDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ScoringService {

    private final RestTemplate restTemplate;
    private final ScoreCalculatorService scoreCalculatorService;

    @Value("${payee.service.url}")
    private String payeeServiceUrl;

    public ScoringService(RestTemplate restTemplate, ScoreCalculatorService scoreCalculatorService) {
        this.restTemplate = restTemplate;
        this.scoreCalculatorService = scoreCalculatorService;
    }

    public List<ScoringItemDto> scoreForCustomer(Long customerId) {
        ResponseEntity<List<RawPayeeDataDto>> response = restTemplate.exchange(
                payeeServiceUrl + "/api/customers/{customerId}/payees/raw",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RawPayeeDataDto>>() {
                },
                customerId
        );

        List<RawPayeeDataDto> payees = Optional.ofNullable(response.getBody()).orElseGet(List::of);
        LocalDateTime now = LocalDateTime.now();

        return payees.stream()
                .map(payee -> new ScoringItemDto(
                        payee.payeeId(),
                        scoreCalculatorService.score(payee.interactions(), now)
                ))
                .toList();
    }
}
