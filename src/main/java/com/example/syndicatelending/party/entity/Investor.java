package com.example.syndicatelending.party.entity;

import com.example.syndicatelending.common.domain.model.Money;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投資家エンティティ（JPA Entity兼ドメインエンティティ）。
 */
@Entity
@Table(name = "investors")
public class Investor {

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

    @Column(name = "investment_capacity", precision = 19, scale = 2)
    private BigDecimal investmentCapacity;

    @Column(name = "current_investment_amount", precision = 19, scale = 2)
    private Money currentInvestmentAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "investor_type")
    private InvestorType investorType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    public Investor() {
    }

    public Investor(String name, String email, String phoneNumber, String companyId,
            BigDecimal investmentCapacity, InvestorType investorType) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.companyId = companyId;
        this.investmentCapacity = investmentCapacity != null ? investmentCapacity : BigDecimal.ZERO;
        this.currentInvestmentAmount = Money.zero();
        this.investorType = investorType;
        this.isActive = true;
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

    public BigDecimal getInvestmentCapacity() {
        return investmentCapacity;
    }

    public void setInvestmentCapacity(BigDecimal investmentCapacity) {
        this.investmentCapacity = investmentCapacity;
        this.updatedAt = LocalDateTime.now();
    }

    public InvestorType getInvestorType() {
        return investorType;
    }

    public void setInvestorType(InvestorType investorType) {
        this.investorType = investorType;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    public Money getCurrentInvestmentAmount() {
        return currentInvestmentAmount;
    }

    public void setCurrentInvestmentAmount(Money currentInvestmentAmount) {
        this.currentInvestmentAmount = currentInvestmentAmount;
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseInvestmentAmount(Money amount) {
        if (amount != null && amount.isPositiveOrZero()) {
            this.currentInvestmentAmount = this.currentInvestmentAmount.add(amount);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void decreaseInvestmentAmount(Money amount) {
        if (amount != null && amount.isPositiveOrZero()) {
            this.currentInvestmentAmount = this.currentInvestmentAmount.subtract(amount);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
