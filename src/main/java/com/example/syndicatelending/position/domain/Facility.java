package com.example.syndicatelending.position.domain;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Facility（ファシリティ）エンティティ。Positionのサブタイプ。
 */
public class Facility {
    private Long id;
    private Long syndicateId;
    private Money commitment;
    private String currency; // 通貨コード（例: "JPY"）
    private LocalDate startDate;
    private LocalDate endDate;
    private String interestTerms;
    private List<SharePie> sharePies;

    public Facility(Long syndicateId, Money commitment, String currency, LocalDate startDate,
            LocalDate endDate, String interestTerms, List<SharePie> sharePies) {
        this.syndicateId = Objects.requireNonNull(syndicateId);
        this.commitment = Objects.requireNonNull(commitment);
        this.currency = Objects.requireNonNull(currency);
        this.startDate = Objects.requireNonNull(startDate);
        this.endDate = Objects.requireNonNull(endDate);
        this.interestTerms = interestTerms;
        this.sharePies = List.copyOf(sharePies);
        validateSharePie();
    }

    private void validateSharePie() {
        Percentage total = sharePies.stream()
                .map(SharePie::getShare)
                .reduce(Percentage.of(0), Percentage::add);
        if (!total.equals(Percentage.of(100))) {
            throw new IllegalArgumentException("SharePieの合計は100%でなければなりません");
        }
    }

    // getter/setter（省略、必要に応じて追加）
    public Long getSyndicateId() {
        return syndicateId;
    }

    public Money getCommitment() {
        return commitment;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getInterestTerms() {
        return interestTerms;
    }

    public List<SharePie> getSharePies() {
        return sharePies;
    }
}
