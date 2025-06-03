package com.example.syndicatelending.party.dto;

import com.example.syndicatelending.party.entity.InvestorType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 投資家更新リクエストDTO。
 */
public class UpdateInvestorRequest {
    @NotBlank(message = "Name is required")
    private final String name;

    @Email(message = "Email should be valid")
    private final String email;

    private final String phoneNumber;

    private final String companyId;

    private final BigDecimal investmentCapacity;

    private final InvestorType investorType;

    @NotNull(message = "Version is required for optimistic locking")
    private final Long version;

    @JsonCreator
    public UpdateInvestorRequest(
            @JsonProperty("name") String name,
            @JsonProperty("email") String email,
            @JsonProperty("phoneNumber") String phoneNumber,
            @JsonProperty("companyId") String companyId,
            @JsonProperty("investmentCapacity") BigDecimal investmentCapacity,
            @JsonProperty("investorType") InvestorType investorType,
            @JsonProperty("version") Long version) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.companyId = companyId;
        this.investmentCapacity = investmentCapacity;
        this.investorType = investorType;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCompanyId() {
        return companyId;
    }

    public BigDecimal getInvestmentCapacity() {
        return investmentCapacity;
    }

    public InvestorType getInvestorType() {
        return investorType;
    }

    public Long getVersion() {
        return version;
    }
}
