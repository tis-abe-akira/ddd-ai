package com.example.syndicatelending.party.dto;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.party.entity.CreditRating;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 借り手更新リクエストDTO。
 */
public class UpdateBorrowerRequest {
    @NotBlank(message = "Name is required")
    private final String name;

    @Email(message = "Email should be valid")
    private final String email;

    private final String phoneNumber;

    private final String companyId;

    private final Money creditLimit;

    private final CreditRating creditRating;

    private final boolean creditLimitOverride;

    @NotNull(message = "Version is required for optimistic locking")
    private final Long version;

    @JsonCreator
    public UpdateBorrowerRequest(
            @JsonProperty("name") String name,
            @JsonProperty("email") String email,
            @JsonProperty("phoneNumber") String phoneNumber,
            @JsonProperty("companyId") String companyId,
            @JsonProperty("creditLimit") Money creditLimit,
            @JsonProperty("creditRating") CreditRating creditRating,
            @JsonProperty("creditLimitOverride") boolean creditLimitOverride,
            @JsonProperty("version") Long version) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.companyId = companyId;
        this.creditLimit = creditLimit;
        this.creditRating = creditRating;
        this.creditLimitOverride = creditLimitOverride;
        this.version = version;
    }

    public UpdateBorrowerRequest(String name, String email, String phoneNumber, String companyId,
            Money creditLimit, CreditRating creditRating, Long version) {
        this(name, email, phoneNumber, companyId, creditLimit, creditRating, false, version);
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

    public Money getCreditLimit() {
        return creditLimit;
    }

    public CreditRating getCreditRating() {
        return creditRating;
    }

    public boolean isCreditLimitOverride() {
        return creditLimitOverride;
    }

    public Long getVersion() {
        return version;
    }
}
