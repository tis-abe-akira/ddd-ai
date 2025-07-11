package com.example.syndicatelending.party.entity;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.MoneyAttributeConverter;
import com.example.syndicatelending.common.statemachine.party.BorrowerState;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 借り手エンティティ（JPA Entity兼ドメインエンティティ）。
 */
@Entity
@Table(name = "borrowers")
public class Borrower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "company_id")
    private String companyId;

    @Convert(converter = MoneyAttributeConverter.class)
    @Column(name = "credit_limit")
    private Money creditLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_rating")
    private CreditRating creditRating;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BorrowerState status = BorrowerState.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    /**
     * 既存Facility総額（動的計算フィールド）
     * データベースには保存されない
     */
    @Transient
    private Double currentFacilityAmount = 0.0;

    public Borrower() {
    }

    public Borrower(String name, String email, String phoneNumber, String companyId,
            Money creditLimit, CreditRating creditRating) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.companyId = companyId;
        this.creditLimit = creditLimit != null ? creditLimit : Money.zero();
        this.creditRating = creditRating;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
        this.updatedAt = LocalDateTime.now();
    }

    public Money getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(Money creditLimit) {
        this.creditLimit = creditLimit;
        this.updatedAt = LocalDateTime.now();
    }

    public CreditRating getCreditRating() {
        return creditRating;
    }

    public void setCreditRating(CreditRating creditRating) {
        this.creditRating = creditRating;
        this.updatedAt = LocalDateTime.now();
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

    public BorrowerState getStatus() {
        return status;
    }

    public void setStatus(BorrowerState status) {
        this.status = status;
    }

    /**
     * Borrowerが制限状態かどうかを判定する
     * 
     * @return 制限状態の場合 true
     */
    public boolean isRestricted() {
        return BorrowerState.COMPLETED.equals(this.status);
    }

    /**
     * 重要フィールドの変更が可能かどうかを判定する
     * 
     * @return 変更可能な場合 true
     */
    public boolean canModifyRestrictedFields() {
        return BorrowerState.ACTIVE.equals(this.status);
    }

    public Double getCurrentFacilityAmount() {
        return currentFacilityAmount;
    }

    public void setCurrentFacilityAmount(Double currentFacilityAmount) {
        this.currentFacilityAmount = currentFacilityAmount != null ? currentFacilityAmount : 0.0;
    }
}
