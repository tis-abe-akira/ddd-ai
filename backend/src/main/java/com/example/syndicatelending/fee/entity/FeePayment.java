package com.example.syndicatelending.fee.entity;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.MoneyAttributeConverter;
import com.example.syndicatelending.transaction.entity.Transaction;
import com.example.syndicatelending.transaction.entity.TransactionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * 手数料支払いエンティティ
 * 
 * シンジケートローンにおける各種手数料の支払い処理を管理し、
 * Transaction基底クラスを継承して統一的な取引管理を実現する。
 */
@Entity
@Table(name = "fee_payments")
public class FeePayment extends Transaction {

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false)
    private FeeType feeType;

    @Column(name = "fee_date", nullable = false)
    private LocalDate feeDate;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false)
    private RecipientType recipientType;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "calculation_base", nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money calculationBase; // 計算基準額

    @Column(name = "fee_rate", nullable = false)
    private Double feeRate; // 手数料率（パーセント）

    @Column(name = "currency", nullable = false)
    private String currency;

    @OneToMany(mappedBy = "feePayment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<FeeDistribution> feeDistributions = new ArrayList<>();

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (getTransactionType() == null) {
            setTransactionType(TransactionType.FEE_PAYMENT);
        }
        if (getTransactionDate() == null && feeDate != null) {
            setTransactionDate(feeDate);
        }
    }

    public FeePayment() {}

    public FeePayment(FeeType feeType, LocalDate feeDate, Money amount, 
                     Money calculationBase, Double feeRate, RecipientType recipientType,
                     Long recipientId, String currency, String description) {
        this.feeType = feeType;
        this.feeDate = feeDate;
        this.calculationBase = calculationBase;
        this.feeRate = feeRate;
        this.recipientType = recipientType;
        this.recipientId = recipientId;
        this.currency = currency;
        this.description = description;
        
        // Initialize inherited fields from Transaction
        setTransactionType(TransactionType.FEE_PAYMENT);
        setTransactionDate(feeDate);
        setAmount(amount);
    }

    // Getters and Setters

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(RecipientType recipientType) {
        this.recipientType = recipientType;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Money getCalculationBase() {
        return calculationBase;
    }

    public void setCalculationBase(Money calculationBase) {
        this.calculationBase = calculationBase;
    }

    public Double getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(Double feeRate) {
        this.feeRate = feeRate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<FeeDistribution> getFeeDistributions() {
        return feeDistributions;
    }

    public void setFeeDistributions(List<FeeDistribution> feeDistributions) {
        this.feeDistributions = feeDistributions;
    }

    // Business Methods

    /**
     * 手数料が投資家配分対象かチェック
     * @return 投資家配分が必要な場合true
     */
    public boolean requiresInvestorDistribution() {
        return FeeCalculationRule.requiresInvestorDistribution(feeType);
    }

    /**
     * 手数料がリードバンク収入かチェック
     * @return リードバンク収入の場合true
     */
    public boolean isLeadBankRevenue() {
        return FeeCalculationRule.getRecipientType(feeType) == RecipientType.LEAD_BANK;
    }

    /**
     * 手数料がエージェントバンク収入かチェック
     * @return エージェントバンク収入の場合true
     */
    public boolean isAgentBankRevenue() {
        return FeeCalculationRule.getRecipientType(feeType) == RecipientType.AGENT_BANK;
    }

    /**
     * 手数料計算結果を検証
     * @return 計算が正確な場合true
     */
    public boolean isCalculationValid() {
        if (calculationBase == null || feeRate == null) {
            return false;
        }
        BigDecimal expectedAmount = FeeCalculationRule.calculateFeeAmount(
            calculationBase.getAmount(), BigDecimal.valueOf(feeRate));
        return getAmount() != null && getAmount().getAmount().compareTo(expectedAmount) == 0;
    }
}