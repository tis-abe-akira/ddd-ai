package com.example.syndicatelending.facility.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.MoneyAttributeConverter;
import com.example.syndicatelending.common.domain.model.Percentage;

@Entity
@Table(name = "facilities")
public class Facility {
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

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SharePie> sharePies = new ArrayList<>();

    public Facility() {
    }

    public Facility(Long syndicateId, Money commitment, String currency, LocalDate startDate,
            LocalDate endDate, String interestTerms) {
        this.syndicateId = syndicateId;
        this.commitment = commitment;
        this.currency = currency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.interestTerms = interestTerms;
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

    public List<SharePie> getSharePies() {
        return sharePies;
    }

    public void setSharePies(List<SharePie> sharePies) {
        this.sharePies = sharePies;
    }

    /**
     * SharePieの合計が100%であることを検証する
     */
    public void validateSharePie() {
        if (sharePies == null || sharePies.isEmpty()) {
            throw new BusinessRuleViolationException("SharePieが設定されていません");
        }

        Percentage total = sharePies.stream()
                .map(SharePie::getShare)
                .reduce(Percentage.of(0), Percentage::add);

        // 100% = 1.0 (BigDecimal)
        Percentage expected = Percentage.of(BigDecimal.ONE);
        if (total.getValue().compareTo(expected.getValue()) != 0) {
            throw new BusinessRuleViolationException("SharePieの合計は100%でなければなりません。現在の合計: " + total);
        }
    }
}
