package com.example.syndicatelending.loan.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreatePaymentRequest {
    @NotNull
    private Long loanId;

    @NotNull
    private LocalDate paymentDate;

    @NotNull
    @Positive
    private BigDecimal principalAmount;

    @NotNull
    @Positive
    private BigDecimal interestAmount;

    @NotNull
    private String currency;

    public CreatePaymentRequest() {}

    public CreatePaymentRequest(Long loanId, LocalDate paymentDate, 
                               BigDecimal principalAmount, BigDecimal interestAmount, String currency) {
        this.loanId = loanId;
        this.paymentDate = paymentDate;
        this.principalAmount = principalAmount;
        this.interestAmount = interestAmount;
        this.currency = currency;
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

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(BigDecimal interestAmount) {
        this.interestAmount = interestAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getTotalAmount() {
        return principalAmount.add(interestAmount);
    }
}