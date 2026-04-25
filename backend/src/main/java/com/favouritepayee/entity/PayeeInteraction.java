package com.favouritepayee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "payee_interactions")
public class PayeeInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "payee_id", nullable = false)
    private Long payeeId;

    @Column(name = "interacted_at", nullable = false)
    private LocalDateTime interactedAt;

    public PayeeInteraction() {
    }

    public PayeeInteraction(Long id, Long customerId, Long payeeId, LocalDateTime interactedAt) {
        this.id = id;
        this.customerId = customerId;
        this.payeeId = payeeId;
        this.interactedAt = interactedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(Long payeeId) {
        this.payeeId = payeeId;
    }

    public LocalDateTime getInteractedAt() {
        return interactedAt;
    }

    public void setInteractedAt(LocalDateTime interactedAt) {
        this.interactedAt = interactedAt;
    }
}
