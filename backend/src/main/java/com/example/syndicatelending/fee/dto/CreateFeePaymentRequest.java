package com.example.syndicatelending.fee.dto;

import com.example.syndicatelending.fee.entity.FeeType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 手数料支払い作成リクエスト
 * 
 * 手数料支払い作成時のパラメータを定義し、
 * バリデーションルールを適用する。
 */
public class CreateFeePaymentRequest {

    @NotNull(message = "Facility ID is required")
    private Long facilityId;

    @NotNull(message = "Borrower ID is required")
    private Long borrowerId;

    @NotNull(message = "Fee type is required")
    private FeeType feeType;

    @NotNull(message = "Fee date is required")
    private LocalDate feeDate;

    @NotNull(message = "Fee amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Fee amount must be positive")
    private BigDecimal feeAmount;

    @NotNull(message = "Calculation base is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Calculation base must be positive")
    private BigDecimal calculationBase;

    @NotNull(message = "Fee rate is required")
    @DecimalMin(value = "0.0", message = "Fee rate must be non-negative")
    @DecimalMax(value = "100.0", message = "Fee rate must not exceed 100%")
    private Double feeRate;

    @NotBlank(message = "Recipient type is required")
    @Pattern(regexp = "BANK|INVESTOR|BORROWER", message = "Recipient type must be BANK, INVESTOR, or BORROWER")
    private String recipientType;

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    public CreateFeePaymentRequest() {}

    // Getters and Setters

    public Long getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public FeeType getFeeType() {
        return feeType;
    }

    public void setFeeType(FeeType feeType) {
        this.feeType = feeType;
    }

    public LocalDate getFeeDate() {
        return feeDate;
    }

    public void setFeeDate(LocalDate feeDate) {
        this.feeDate = feeDate;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public BigDecimal getCalculationBase() {
        return calculationBase;
    }

    public void setCalculationBase(BigDecimal calculationBase) {
        this.calculationBase = calculationBase;
    }

    public Double getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(Double feeRate) {
        this.feeRate = feeRate;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}