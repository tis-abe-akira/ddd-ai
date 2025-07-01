package com.example.syndicatelending.party.dto;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.party.entity.CreditRating;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 借り手作成リクエストDTO。
 */
public class CreateBorrowerRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Email should be valid")
    private String email;

    private String phoneNumber;
    private String companyId;

    private Money creditLimit;

    private CreditRating creditRating;

    /**
     * CreditLimit上限バリデーションを無効化する場合にtrue。
     * デフォルトはfalse。
     */
    private boolean creditLimitOverride = false;

    public CreateBorrowerRequest() {
    }

    @JsonCreator
    public CreateBorrowerRequest(
            @JsonProperty("name") String name,
            @JsonProperty("email") String email,
            @JsonProperty("phoneNumber") String phoneNumber,
            @JsonProperty("companyId") String companyId,
            @JsonProperty("creditLimit") Money creditLimit,
            @JsonProperty("creditRating") CreditRating creditRating) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.companyId = companyId;
        this.creditLimit = creditLimit;
        this.creditRating = creditRating;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public Money getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(Money creditLimit) {
        this.creditLimit = creditLimit;
    }

    public CreditRating getCreditRating() {
        return creditRating;
    }

    public void setCreditRating(CreditRating creditRating) {
        this.creditRating = creditRating;
    }

    public boolean isCreditLimitOverride() {
        return creditLimitOverride;
    }

    public void setCreditLimitOverride(boolean creditLimitOverride) {
        this.creditLimitOverride = creditLimitOverride;
    }
}
