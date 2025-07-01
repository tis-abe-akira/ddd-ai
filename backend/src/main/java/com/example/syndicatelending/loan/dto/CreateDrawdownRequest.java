package com.example.syndicatelending.loan.dto;

import com.example.syndicatelending.loan.entity.RepaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreateDrawdownRequest {
    private Long facilityId;
    private Long borrowerId;
    private BigDecimal amount;
    private String currency;
    private String purpose;

    // Loan生成用パラメータ
    private BigDecimal annualInterestRate; // 年利（例: 0.025 = 2.5%）
    private LocalDate drawdownDate; // ドローダウン実行日
    private Integer repaymentPeriodMonths; // 返済期間（月数）
    private String repaymentCycle; // 返済サイクル（例: "MONTHLY"）
    private RepaymentMethod repaymentMethod; // 返済方法（例: EQUAL_INSTALLMENT, BULLET）

    // 投資家ごとのAmountPie（任意指定）
    private List<AmountPieDto> amountPies;

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

    public List<AmountPieDto> getAmountPies() {
        return amountPies;
    }

    public void setAmountPies(List<AmountPieDto> amountPies) {
        this.amountPies = amountPies;
    }
}
