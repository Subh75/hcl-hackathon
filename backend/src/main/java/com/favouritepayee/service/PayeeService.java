package com.favouritepayee.service;

import com.favouritepayee.dto.PageResponse;
import com.favouritepayee.dto.PayeeDto;
import com.favouritepayee.dto.PayeeListResponse;
import com.favouritepayee.dto.PayeeRequest;
import com.favouritepayee.entity.Customer;
import com.favouritepayee.entity.FavouriteAccount;
import com.favouritepayee.entity.PayeeInteraction;
import com.favouritepayee.exception.ConflictException;
import com.favouritepayee.exception.ResourceNotFoundException;
import com.favouritepayee.repository.CustomerRepository;
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
import org.springframework.stereotype.Service;

@Service
public class PayeeService {

    private static final int MAX_FAVOURITES = 20;

    private final CustomerRepository customerRepository;
    private final FavouriteAccountRepository favouriteAccountRepository;
    private final PayeeInteractionRepository payeeInteractionRepository;
    private final BankResolverService bankResolverService;
    private final ScoreCalculatorService scoreCalculatorService;

    public PayeeService(
            CustomerRepository customerRepository,
            FavouriteAccountRepository favouriteAccountRepository,
            PayeeInteractionRepository payeeInteractionRepository,
            BankResolverService bankResolverService,
            ScoreCalculatorService scoreCalculatorService
    ) {
        this.customerRepository = customerRepository;
        this.favouriteAccountRepository = favouriteAccountRepository;
        this.payeeInteractionRepository = payeeInteractionRepository;
        this.bankResolverService = bankResolverService;
        this.scoreCalculatorService = scoreCalculatorService;
    }

    public PayeeListResponse getPayees(Long customerId, int page, int size, String search) {
        ensureCustomerExists(customerId);
        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? size : 5;
        String term = Optional.ofNullable(search).map(String::trim).orElse("");

        List<FavouriteAccount> matchedPayees = favouriteAccountRepository.searchByCustomer(customerId, term);
        Map<Long, List<PayeeInteraction>> interactionsByPayee = loadInteractions(customerId, matchedPayees);
        LocalDateTime now = LocalDateTime.now();

        Map<Long, Double> scoreByPayee = matchedPayees.stream()
                .collect(Collectors.toMap(
                        FavouriteAccount::getId,
                        payee -> scoreCalculatorService.score(interactionsByPayee.getOrDefault(payee.getId(), List.of()), now)
                ));

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
        FavouriteAccount payee = findPayeeOrThrow(customerId, payeeId);
        double score = scoreCalculatorService.score(
                payeeInteractionRepository.findByCustomerIdAndPayeeId(customerId, payeeId),
                LocalDateTime.now()
        );
        return toDto(payee, score);
    }

    @Transactional
    public PayeeDto createPayee(Long customerId, PayeeRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        validateCustomerPayeeLimit(customerId);

        String normalizedIban = normalizeIban(request.iban());
        if (favouriteAccountRepository.existsByCustomerIdAndIban(customerId, normalizedIban)) {
            throw new ConflictException("Duplicate iban for customer");
        }

        String bankName = bankResolverService.resolveBankNameOrThrow(normalizedIban);

        FavouriteAccount account = new FavouriteAccount();
        account.setName(request.name().trim());
        account.setIban(normalizedIban);
        account.setBank(bankName);
        account.setCustomer(customer);

        FavouriteAccount saved = favouriteAccountRepository.save(account);
        return toDto(saved, 0.0);
    }

    @Transactional
    public PayeeDto updatePayee(Long customerId, Long payeeId, PayeeRequest request) {
        FavouriteAccount existing = findPayeeOrThrow(customerId, payeeId);

        String normalizedIban = normalizeIban(request.iban());
        if (favouriteAccountRepository.existsByCustomerIdAndIbanAndIdNot(customerId, normalizedIban, payeeId)) {
            throw new ConflictException("Duplicate iban for customer");
        }

        String bankName = bankResolverService.resolveBankNameOrThrow(normalizedIban);

        existing.setName(request.name().trim());
        existing.setIban(normalizedIban);
        existing.setBank(bankName);

        FavouriteAccount saved = favouriteAccountRepository.save(existing);
        double score = scoreCalculatorService.score(
                payeeInteractionRepository.findByCustomerIdAndPayeeId(customerId, payeeId),
                LocalDateTime.now()
        );
        return toDto(saved, score);
    }

    @Transactional
    public void deletePayee(Long customerId, Long payeeId) {
        FavouriteAccount payee = findPayeeOrThrow(customerId, payeeId);
        payeeInteractionRepository.deleteByCustomerIdAndPayeeId(customerId, payeeId);
        favouriteAccountRepository.delete(payee);
    }

    @Transactional
    public void logInteraction(Long customerId, Long payeeId) {
        findPayeeOrThrow(customerId, payeeId);
        PayeeInteraction interaction = new PayeeInteraction();
        interaction.setCustomerId(customerId);
        interaction.setPayeeId(payeeId);
        interaction.setInteractedAt(LocalDateTime.now());
        payeeInteractionRepository.save(interaction);
    }

    private FavouriteAccount findPayeeOrThrow(Long customerId, Long payeeId) {
        return favouriteAccountRepository.findByIdAndCustomerId(payeeId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Payee not found"));
    }

    private void ensureCustomerExists(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found");
        }
    }

    private void validateCustomerPayeeLimit(Long customerId) {
        if (favouriteAccountRepository.countByCustomerId(customerId) >= MAX_FAVOURITES) {
            throw new ConflictException("Maximum of 20 favourite accounts reached");
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

    private PayeeDto toDto(FavouriteAccount payee, double score) {
        return new PayeeDto(payee.getId(), payee.getName(), payee.getIban(), payee.getBank(), score);
    }

    private String normalizeIban(String iban) {
        return iban.trim().toUpperCase();
    }
}
