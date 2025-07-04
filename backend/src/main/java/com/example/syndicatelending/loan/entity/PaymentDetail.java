package com.example.syndicatelending.loan.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.MoneyAttributeConverter;
import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * 返済明細エンティティ。
 * <p>
 * ローンの返済スケジュールを表す。
 * Loanエンティティに所有されるコンポーネント（集約の一部）。
 * </p>
 */
@Entity
@Table(name = "payment_detail")
public class PaymentDetail {
    /** 返済明細ID（主キー） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属するローン */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    @JsonBackReference
    private Loan loan;

    /** 返済回数 */
    @Column(nullable = false)
    private Integer paymentNumber;

    /** 元本返済額 */
    @Column(nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money principalPayment;

    /** 利息返済額 */
    @Column(nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money interestPayment;

    /** 返済期日 */
    @Column(nullable = false)
    private LocalDate dueDate;

    /** 返済後の元本残高 */
    @Column(nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money remainingBalance;

    /** レコード作成日時 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** レコード更新日時 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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

    // Constructors
    public PaymentDetail() {
    }

    public PaymentDetail(Loan loan, Integer paymentNumber, Money principalPayment,
            Money interestPayment, LocalDate dueDate, Money remainingBalance) {
        this.loan = loan;
        this.paymentNumber = paymentNumber;
        this.principalPayment = principalPayment;
        this.interestPayment = interestPayment;
        this.dueDate = dueDate;
        this.remainingBalance = remainingBalance;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    public Integer getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(Integer paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public Money getPrincipalPayment() {
        return principalPayment;
    }

    public void setPrincipalPayment(Money principalPayment) {
        this.principalPayment = principalPayment;
    }

    public Money getInterestPayment() {
        return interestPayment;
    }

    public void setInterestPayment(Money interestPayment) {
        this.interestPayment = interestPayment;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Money getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(Money remainingBalance) {
        this.remainingBalance = remainingBalance;
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

    /**
     * 総返済額（元本 + 利息）を計算する。
     */
    public Money getTotalPayment() {
        return principalPayment.add(interestPayment);
    }
}
