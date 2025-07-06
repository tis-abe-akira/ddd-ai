package com.example.syndicatelending.loan.dto;

import com.example.syndicatelending.loan.entity.RepaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Drawdown更新リクエストDTO
 * 
 * PENDING, FAILED状態のDrawdownのみ更新可能
 */
public class UpdateDrawdownRequest {
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @NotBlank(message = "Purpose is required")
    private String purpose;
    
    @NotNull(message = "Annual interest rate is required")
    private BigDecimal annualInterestRate;
    
    @NotNull(message = "Drawdown date is required")
    private LocalDate drawdownDate;
    
    @NotNull(message = "Repayment period is required")
    @Positive(message = "Repayment period must be positive")
    private Integer repaymentPeriodMonths;
    
    @NotBlank(message = "Repayment cycle is required")
    private String repaymentCycle;
    
    @NotNull(message = "Repayment method is required")
    private RepaymentMethod repaymentMethod;
    
    @NotNull(message = "Version is required for optimistic locking")
    private Long version;
    
    private List<AmountPieDto> amountPies;

    // Constructors
    public UpdateDrawdownRequest() {}

    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(BigDecimal annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public LocalDate getDrawdownDate() {
        return drawdownDate;
    }

    public void setDrawdownDate(LocalDate drawdownDate) {
        this.drawdownDate = drawdownDate;
    }

    public Integer getRepaymentPeriodMonths() {
        return repaymentPeriodMonths;
    }

    public void setRepaymentPeriodMonths(Integer repaymentPeriodMonths) {
        this.repaymentPeriodMonths = repaymentPeriodMonths;
    }

    public String getRepaymentCycle() {
        return repaymentCycle;
    }

    public void setRepaymentCycle(String repaymentCycle) {
        this.repaymentCycle = repaymentCycle;
    }

    public RepaymentMethod getRepaymentMethod() {
        return repaymentMethod;
    }

    public void setRepaymentMethod(RepaymentMethod repaymentMethod) {
        this.repaymentMethod = repaymentMethod;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public List<AmountPieDto> getAmountPies() {
        return amountPies;
    }

    public void setAmountPies(List<AmountPieDto> amountPies) {
        this.amountPies = amountPies;
    }
}