package com.example.syndicatelending.facility.dto;

import com.example.syndicatelending.common.domain.model.Money;
import java.time.LocalDate;

public class FacilityInvestmentResponse {
    private Long id;
    private Long facilityId;
    private Long investorId;
    private Money amount;
    private LocalDate transactionDate;
    private String transactionType;

    public FacilityInvestmentResponse() {}

    public FacilityInvestmentResponse(Long id, Long facilityId, Long investorId, Money amount, LocalDate transactionDate, String transactionType) {
        this.id = id;
        this.facilityId = facilityId;
        this.investorId = investorId;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.transactionType = transactionType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
    }

    public Long getInvestorId() {
        return investorId;
    }

    public void setInvestorId(Long investorId) {
        this.investorId = investorId;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
}
