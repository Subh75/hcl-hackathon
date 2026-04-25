package com.favouritepayee.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.favouritepayee.entity.PayeeInteraction;

public interface PayeeInteractionRepository extends JpaRepository<PayeeInteraction, Long> {

    List<PayeeInteraction> findByCustomerIdAndPayeeId(Long customerId, Long payeeId);

    List<PayeeInteraction> findByCustomerIdAndPayeeIdIn(Long customerId, Collection<Long> payeeIds);

    void deleteByCustomerIdAndPayeeId(Long customerId, Long payeeId);
}
