package com.example.syndicatelending.position.domain;

import com.example.syndicatelending.common.domain.model.Percentage;
import java.util.Objects;

/**
 * FacilityやLoanなどPositionに対する投資家の持分比率を表す値オブジェクト。
 */
public class SharePie {
    private final Long investorId;
    private final Percentage share;

    public SharePie(Long investorId, Percentage share) {
        this.investorId = Objects.requireNonNull(investorId);
        this.share = Objects.requireNonNull(share);
    }

    public Long getInvestorId() {
        return investorId;
    }

    public Percentage getShare() {
        return share;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharePie sharePie = (SharePie) o;
        return investorId.equals(sharePie.investorId) && share.equals(sharePie.share);
    }

    @Override
    public int hashCode() {
        return Objects.hash(investorId, share);
    }

    @Override
    public String toString() {
        return "SharePie{" +
                "investorId=" + investorId +
                ", share=" + share +
                '}';
    }
}
