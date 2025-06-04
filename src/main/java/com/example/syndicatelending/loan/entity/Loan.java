package com.example.syndicatelending.loan.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import com.example.syndicatelending.common.domain.model.MoneyAttributeConverter;
import com.example.syndicatelending.common.domain.model.PercentageAttributeConverter;

/**
 * ローン（貸付）エンティティ。
 * <p>
 * シンジケートローンの各貸付情報を表す集約ルート。
 * 監査フィールド（createdAt, updatedAt）、バージョン（version）を持つ。
 * </p>
 */
@Entity
@Table(name = "loan")
public class Loan {
    /** ローンID（主キー） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ファシリティID（外部キー） */
    @Column(nullable = false)
    private Long facilityId;

    /** 借り手ID（外部キー） */
    @Column(nullable = false)
    private Long borrowerId;

    /** 元本金額 */
    @Column(nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money principalAmount;

    /** 現在の貸付残高 */
    @Column(nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money outstandingBalance;

    /** 年利率（%） */
    @Column(nullable = false)
    @Convert(converter = PercentageAttributeConverter.class)
    private Percentage annualInterestRate;

    /** ドローダウン日（貸付実行日） */
    @Column(nullable = false)
    private LocalDate drawdownDate;

    /** 返済期間（月単位） */
    @Column(nullable = false)
    private Integer repaymentPeriodMonths;

    /** 返済サイクル（例: MONTHLY, QUARTERLY等） */
    @Column(nullable = false)
    private String repaymentCycle;

    /** 返済方法（例: 元利均等、元金均等等） */
    @Column(nullable = false)
    private String repaymentMethod;

    /** 通貨コード（例: JPY, USD等） */
    @Column(nullable = false)
    private String currency;

    /** レコード作成日時 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** レコード更新日時 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 楽観的ロック用バージョン番号 */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * エンティティ新規作成時に作成・更新日時を自動設定。
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * エンティティ更新時に更新日時を自動設定。
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
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

    public Long getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public Money getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(Money principalAmount) {
        this.principalAmount = principalAmount;
    }

    public Money getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(Money outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public Percentage getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(Percentage annualInterestRate) {
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

    public String getRepaymentMethod() {
        return repaymentMethod;
    }

    public void setRepaymentMethod(String repaymentMethod) {
        this.repaymentMethod = repaymentMethod;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
