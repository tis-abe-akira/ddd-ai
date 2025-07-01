package com.example.syndicatelending.party.entity;

import com.example.syndicatelending.common.domain.model.Money;

import java.math.BigDecimal;

public enum CreditRating {
    AAA(new BigDecimal("100000000")),
    AA(new BigDecimal("50000000")),
    A(new BigDecimal("20000000")),
    BBB(new BigDecimal("10000000")),
    BB(new BigDecimal("5000000")),
    B(new BigDecimal("2000000")),
    CCC(null), CC(null), C(null), D(null);

    private final Money limit;

    CreditRating(BigDecimal limit) {
        this.limit = limit == null ? null : Money.of(limit);
    }

    public Money getLimit() {
        return limit;
    }

    public boolean isLimitSatisfied(Money creditLimit) {
        if (limit == null || creditLimit == null)
            return true;
        return creditLimit.isLessThan(limit) || creditLimit.equals(limit);
    }
}
