package com.example.syndicatelending.loan.entity;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.MoneyAttributeConverter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_distributions")
public class PaymentDistribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "investor_id", nullable = false)
    private Long investorId;

    @Column(name = "principal_amount", nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money principalAmount;

    @Column(name = "interest_amount", nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money interestAmount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public PaymentDistribution() {}

    public PaymentDistribution(Long investorId, Money principalAmount, 
                              Money interestAmount, String currency) {
        this.investorId = investorId;
        this.principalAmount = principalAmount;
        this.interestAmount = interestAmount;
        this.currency = currency;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvestorId() {
        return investorId;
    }

    public void setInvestorId(Long investorId) {
        this.investorId = investorId;
    }

    public Money getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(Money principalAmount) {
        this.principalAmount = principalAmount;
    }

    public Money getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(Money interestAmount) {
        this.interestAmount = interestAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Money getTotalAmount() {
        return principalAmount.add(interestAmount);
    }
}