package com.example.syndicatelending.facility.dto;

public class CreateFacilityInvestmentRequest {
    private Long investorId;

    public CreateFacilityInvestmentRequest() {}

    public CreateFacilityInvestmentRequest(Long investorId) {
        this.investorId = investorId;
    }

    public Long getInvestorId() {
        return investorId;
    }

    public void setInvestorId(Long investorId) {
        this.investorId = investorId;
    }
}
