package com.example.syndicatelending.loan.entity;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.MoneyAttributeConverter;
import com.example.syndicatelending.transaction.entity.Transaction;
import com.example.syndicatelending.transaction.entity.TransactionType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "payments")
public class Payment extends Transaction {
    @Column(name = "loan_id", nullable = false)
    private Long loanId;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "total_amount", nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money totalAmount;

    @Column(name = "principal_amount", nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money principalAmount;

    @Column(name = "interest_amount", nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money interestAmount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<PaymentDistribution> paymentDistributions = new ArrayList<>();

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (getTransactionType() == null) {
            setTransactionType(TransactionType.PAYMENT);
        }
        // Set totalAmount as the main amount field inherited from Transaction
        if (getAmount() == null && totalAmount != null) {
            setAmount(totalAmount);
        }
    }

    public Payment() {}

    public Payment(Long loanId, LocalDate paymentDate, Money totalAmount, 
                   Money principalAmount, Money interestAmount, String currency) {
        this.loanId = loanId;
        this.paymentDate = paymentDate;
        this.totalAmount = totalAmount;
        this.principalAmount = principalAmount;
        this.interestAmount = interestAmount;
        this.currency = currency;
        
        // Initialize inherited fields from Transaction
        setTransactionType(TransactionType.PAYMENT);
        setTransactionDate(paymentDate);
        setAmount(totalAmount);
    }

    // Getters and Setters

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
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

    public List<PaymentDistribution> getPaymentDistributions() {
        return paymentDistributions;
    }

    public void setPaymentDistributions(List<PaymentDistribution> paymentDistributions) {
        this.paymentDistributions = paymentDistributions;
    }

}