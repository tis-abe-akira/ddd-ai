package com.example.syndicatelending.party.dto;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.party.entity.CreditRating;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

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
            @JsonProperty("creditLimit") BigDecimal creditLimit,
            @JsonProperty("creditRating") CreditRating creditRating) {
        this(name, email, phoneNumber, companyId, creditLimit == null ? null : Money.of(creditLimit), creditRating);
    }

    // Money型を直接受けるコンストラクタ（内部用）
    public CreateBorrowerRequest(String name, String email, String phoneNumber,
            String companyId, Money creditLimit, CreditRating creditRating) {
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

    @JsonProperty("creditLimit")
    public BigDecimal getCreditLimit() {
        return creditLimit == null ? null : creditLimit.getAmount();
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit == null ? null : Money.of(creditLimit);
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

    // Money型のgetter/setterは@JsonIgnore推奨
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Money getCreditLimitAsMoney() {
        return creditLimit;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setCreditLimit(Money creditLimit) {
        this.creditLimit = creditLimit;
    }
}
