package com.example.syndicatelending.fee.entity;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.MoneyAttributeConverter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 手数料配分エンティティ
 * 
 * 手数料支払いの投資家別・受益者別配分を管理し、
 * PaymentDistributionと類似の構造で手数料配分を実現する。
 */
@Entity
@Table(name = "fee_distributions")
public class FeeDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_type", nullable = false)
    private String recipientType; // INVESTOR, BANK, BORROWER

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "distribution_amount", nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money distributionAmount;

    @Column(name = "distribution_ratio", nullable = false)
    private Double distributionRatio; // 配分比率（パーセント）

    @Column(name = "currency", nullable = false)
    private String currency;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_payment_id", nullable = false)
    private FeePayment feePayment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public FeeDistribution() {}

    public FeeDistribution(String recipientType, Long recipientId, 
                          Money distributionAmount, Double distributionRatio, String currency) {
        this.recipientType = recipientType;
        this.recipientId = recipientId;
        this.distributionAmount = distributionAmount;
        this.distributionRatio = distributionRatio;
        this.currency = currency;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Money getDistributionAmount() {
        return distributionAmount;
    }

    public void setDistributionAmount(Money distributionAmount) {
        this.distributionAmount = distributionAmount;
    }

    public Double getDistributionRatio() {
        return distributionRatio;
    }

    public void setDistributionRatio(Double distributionRatio) {
        this.distributionRatio = distributionRatio;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public FeePayment getFeePayment() {
        return feePayment;
    }

    public void setFeePayment(FeePayment feePayment) {
        this.feePayment = feePayment;
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

    // Business Methods

    /**
     * 配分先が投資家かチェック
     * @return 投資家の場合true
     */
    public boolean isInvestorDistribution() {
        return "INVESTOR".equals(recipientType);
    }

    /**
     * 配分先が銀行かチェック
     * @return 銀行の場合true
     */
    public boolean isBankDistribution() {
        return "BANK".equals(recipientType);
    }

    /**
     * 配分先が借り手かチェック
     * @return 借り手の場合true
     */
    public boolean isBorrowerDistribution() {
        return "BORROWER".equals(recipientType);
    }

    /**
     * 配分比率の妥当性チェック
     * @return 比率が有効範囲内の場合true
     */
    public boolean isValidDistributionRatio() {
        return distributionRatio != null && distributionRatio >= 0.0 && distributionRatio <= 100.0;
    }
}