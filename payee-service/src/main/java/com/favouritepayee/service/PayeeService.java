package com.favouritepayee.service;

import com.favouritepayee.dto.BankResolveResponse;
import com.favouritepayee.dto.PageResponse;
import com.favouritepayee.dto.PayeeDto;
import com.favouritepayee.dto.PayeeListResponse;
import com.favouritepayee.dto.PayeeRequest;
import com.favouritepayee.dto.RawInteractionDto;
import com.favouritepayee.dto.RawPayeeDataDto;
import com.favouritepayee.dto.ScoringItemDto;
import com.favouritepayee.entity.FavouriteAccount;
import com.favouritepayee.entity.PayeeInteraction;
import com.favouritepayee.exception.BadRequestException;
import com.favouritepayee.exception.ConflictException;
import com.favouritepayee.exception.ResourceNotFoundException;
import com.favouritepayee.repository.FavouriteAccountRepository;
import com.favouritepayee.repository.PayeeInteractionRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class PayeeService {

    private static final int MAX_FAVOURITES = 20;

    private final FavouriteAccountRepository favouriteAccountRepository;
    private final PayeeInteractionRepository payeeInteractionRepository;
    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Value("${bank.service.url}")
    private String bankServiceUrl;

    @Value("${scoring.service.url}")
    private String scoringServiceUrl;

    public PayeeService(
            FavouriteAccountRepository favouriteAccountRepository,
            PayeeInteractionRepository payeeInteractionRepository,
            RestTemplate restTemplate
    ) {
        this.favouriteAccountRepository = favouriteAccountRepository;
        this.payeeInteractionRepository = payeeInteractionRepository;
        this.restTemplate = restTemplate;
    }

    public PayeeListResponse getPayees(Long customerId, int page, int size, String search) {
        ensureCustomerExists(customerId);
        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? size : 5;
        String term = Optional.ofNullable(search).map(String::trim).orElse("");

        List<FavouriteAccount> matchedPayees = favouriteAccountRepository.searchByCustomer(customerId, term);
        Map<Long, Double> scoreByPayee = fetchScores(customerId);

        List<PayeeDto> smartFavourites = matchedPayees.stream()
                .sorted(Comparator
                        .comparingDouble((FavouriteAccount payee) -> scoreByPayee.getOrDefault(payee.getId(), 0.0))
                        .reversed()
                        .thenComparing(FavouriteAccount::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(3)
                .map(payee -> toDto(payee, scoreByPayee.getOrDefault(payee.getId(), 0.0)))
                .toList();

        Set<Long> smartIds = smartFavourites.stream().map(PayeeDto::id).collect(Collectors.toSet());

        List<PayeeDto> remaining = matchedPayees.stream()
                .filter(payee -> !smartIds.contains(payee.getId()))
                .map(payee -> toDto(payee, scoreByPayee.getOrDefault(payee.getId(), 0.0)))
                .toList();

        int totalElements = remaining.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);
        int fromIndex = Math.min(safePage * safeSize, totalElements);
        int toIndex = Math.min(fromIndex + safeSize, totalElements);

        List<PayeeDto> pageContent = remaining.subList(fromIndex, toIndex);

        return new PayeeListResponse(
                smartFavourites,
                new PageResponse<>(pageContent, safePage, safeSize, totalElements, totalPages)
        );
    }

    public PayeeDto getPayeeById(Long customerId, Long payeeId) {
        ensureCustomerExists(customerId);
        FavouriteAccount payee = findPayeeOrThrow(customerId, payeeId);
        double score = fetchScores(customerId).getOrDefault(payeeId, 0.0);
        return toDto(payee, score);
    }

    @Transactional
    public PayeeDto createPayee(Long customerId, PayeeRequest request) {
        ensureCustomerExists(customerId);
        validateCustomerPayeeLimit(customerId);

        String normalizedIban = normalizeIban(request.iban());
        if (favouriteAccountRepository.existsByCustomerIdAndIban(customerId, normalizedIban)) {
            throw new ConflictException("Duplicate iban for customer");
        }

        String bankName = resolveBankNameOrThrow(normalizedIban);

        FavouriteAccount account = new FavouriteAccount();
        account.setName(request.name().trim());
        account.setIban(normalizedIban);
        account.setBank(bankName);
        account.setCustomerId(customerId);

        FavouriteAccount saved = favouriteAccountRepository.save(account);
        return toDto(saved, 0.0);
    }

    @Transactional
    public PayeeDto updatePayee(Long customerId, Long payeeId, PayeeRequest request) {
        ensureCustomerExists(customerId);
        FavouriteAccount existing = findPayeeOrThrow(customerId, payeeId);

        String normalizedIban = normalizeIban(request.iban());
        if (favouriteAccountRepository.existsByCustomerIdAndIbanAndIdNot(customerId, normalizedIban, payeeId)) {
            throw new ConflictException("Duplicate iban for customer");
        }

        String bankName = resolveBankNameOrThrow(normalizedIban);

        existing.setName(request.name().trim());
        existing.setIban(normalizedIban);
        existing.setBank(bankName);

        FavouriteAccount saved = favouriteAccountRepository.save(existing);
        double score = fetchScores(customerId).getOrDefault(payeeId, 0.0);
        return toDto(saved, score);
    }

    @Transactional
    public void deletePayee(Long customerId, Long payeeId) {
        ensureCustomerExists(customerId);
        FavouriteAccount payee = findPayeeOrThrow(customerId, payeeId);
        payeeInteractionRepository.deleteByCustomerIdAndPayeeId(customerId, payeeId);
        favouriteAccountRepository.delete(payee);
    }

    @Transactional
    public void logInteraction(Long customerId, Long payeeId) {
        ensureCustomerExists(customerId);
        findPayeeOrThrow(customerId, payeeId);
        PayeeInteraction interaction = new PayeeInteraction();
        interaction.setCustomerId(customerId);
        interaction.setPayeeId(payeeId);
        interaction.setInteractedAt(LocalDateTime.now());
        payeeInteractionRepository.save(interaction);
    }

    public List<RawPayeeDataDto> getRawPayees(Long customerId) {
        ensureCustomerExists(customerId);
        List<FavouriteAccount> payees = favouriteAccountRepository.searchByCustomer(customerId, "");
        Map<Long, List<PayeeInteraction>> interactionsByPayee = loadInteractions(customerId, payees);

        return payees.stream()
                .map(payee -> new RawPayeeDataDto(
                        payee.getId(),
                        customerId,
                        payee.getName(),
                        payee.getIban(),
                        payee.getBank(),
                        interactionsByPayee.getOrDefault(payee.getId(), List.of())
                                .stream()
                                .map(interaction -> new RawInteractionDto(interaction.getInteractedAt()))
                                .toList()
                ))
                .toList();
    }

    private FavouriteAccount findPayeeOrThrow(Long customerId, Long payeeId) {
        return favouriteAccountRepository.findByIdAndCustomerId(payeeId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Payee not found"));
    }

    private void validateCustomerPayeeLimit(Long customerId) {
        if (favouriteAccountRepository.countByCustomerId(customerId) >= MAX_FAVOURITES) {
            throw new ConflictException("Maximum of 20 favourite accounts reached");
        }
    }

    private void ensureCustomerExists(Long customerId) {
        try {
            ResponseEntity<Void> response = restTemplate.getForEntity(
                    authServiceUrl + "/auth/customers/{id}",
                    Void.class,
                    customerId
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ResourceNotFoundException("Customer not found");
            }
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException("Customer not found");
        }
    }

    private Map<Long, List<PayeeInteraction>> loadInteractions(Long customerId, List<FavouriteAccount> payees) {
        List<Long> payeeIds = payees.stream().map(FavouriteAccount::getId).toList();
        if (payeeIds.isEmpty()) {
            return Map.of();
        }

        return payeeInteractionRepository.findByCustomerIdAndPayeeIdIn(customerId, payeeIds)
                .stream()
                .collect(Collectors.groupingBy(PayeeInteraction::getPayeeId));
    }

    private String resolveBankNameOrThrow(String iban) {
        if (iban == null || iban.trim().length() < 8) {
            throw new BadRequestException("Invalid iban", Map.of("iban", "iban must be at least 8 characters"));
        }

        try {
            ResponseEntity<BankResolveResponse> response = restTemplate.getForEntity(
                    bankServiceUrl + "/banks/resolve?iban={iban}",
                    BankResolveResponse.class,
                    iban
            );
            BankResolveResponse body = response.getBody();
            if (body == null || body.bankName() == null || body.bankName().isBlank()) {
                throw new BadRequestException("Invalid bank code", Map.of("iban", "bank code not found"));
            }
            return body.bankName();
        } catch (HttpClientErrorException.BadRequest exception) {
            throw new BadRequestException("Invalid bank code", Map.of("iban", "bank code not found"));
        }
    }

    private Map<Long, Double> fetchScores(Long customerId) {
        ResponseEntity<List<ScoringItemDto>> response = restTemplate.exchange(
                scoringServiceUrl + "/scoring/{customerId}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ScoringItemDto>>() {
                },
                customerId
        );

        List<ScoringItemDto> body = Optional.ofNullable(response.getBody()).orElseGet(List::of);
        return body.stream().collect(Collectors.toMap(ScoringItemDto::payeeId, item -> Optional.ofNullable(item.score()).orElse(0.0)));
    }

    private PayeeDto toDto(FavouriteAccount payee, double score) {
        return new PayeeDto(payee.getId(), payee.getName(), payee.getIban(), payee.getBank(), score);
    }

    private String normalizeIban(String iban) {
        return iban.trim().toUpperCase();
    }
}
