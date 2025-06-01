package com.example.syndicatelending.facility.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.MoneyAttributeConverter;

@Entity
@Table(name = "facilities")
public class FacilityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long syndicateId;

    @Convert(converter = MoneyAttributeConverter.class)
    @Column(nullable = false, precision = 19, scale = 2)
    private Money commitment;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column
    private String interestTerms;

    public FacilityEntity() {
    }

    public FacilityEntity(Long syndicateId, Money commitment, String currency, LocalDate startDate,
            LocalDate endDate, String interestTerms) {
        this.syndicateId = syndicateId;
        this.commitment = commitment;
        this.currency = currency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.interestTerms = interestTerms;
    }

    // BigDecimal互換のためのオーバーロード
    public FacilityEntity(Long syndicateId, java.math.BigDecimal commitment, String currency,
            LocalDate startDate, LocalDate endDate, String interestTerms) {
        this(syndicateId, commitment == null ? null : Money.of(commitment), currency, startDate, endDate,
                interestTerms);
    }

    // getter/setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSyndicateId() {
        return syndicateId;
    }

    public void setSyndicateId(Long syndicateId) {
        this.syndicateId = syndicateId;
    }

    public Money getCommitment() {
        return commitment;
    }

    public void setCommitment(Money commitment) {
        this.commitment = commitment;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getInterestTerms() {
        return interestTerms;
    }

    public void setInterestTerms(String interestTerms) {
        this.interestTerms = interestTerms;
    }
}
