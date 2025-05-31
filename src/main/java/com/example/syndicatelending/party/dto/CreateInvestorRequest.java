package com.example.syndicatelending.party.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * 投資家作成リクエストDTO。
 */
public class CreateInvestorRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Email should be valid")
    private String email;

    private String phoneNumber;
    private String companyId;

    @PositiveOrZero(message = "Investment capacity must be positive or zero")
    private BigDecimal investmentCapacity;

    private String investorType;

    public CreateInvestorRequest() {}

    public CreateInvestorRequest(String name, String email, String phoneNumber,
                                String companyId, BigDecimal investmentCapacity, String investorType) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.companyId = companyId;
        this.investmentCapacity = investmentCapacity;
        this.investorType = investorType;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public BigDecimal getInvestmentCapacity() { return investmentCapacity; }
    public void setInvestmentCapacity(BigDecimal investmentCapacity) { this.investmentCapacity = investmentCapacity; }

    public String getInvestorType() { return investorType; }
    public void setInvestorType(String investorType) { this.investorType = investorType; }
}