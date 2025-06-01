package com.example.syndicatelending.party.entity;

import java.math.BigDecimal;

public enum CreditRating {
    AAA(new BigDecimal("100000000")),
    AA(new BigDecimal("50000000")),
    A(new BigDecimal("20000000")),
    BBB(new BigDecimal("10000000")),
    BB(new BigDecimal("5000000")),
    B(new BigDecimal("2000000")),
    CCC(null), CC(null), C(null), D(null);

    private final BigDecimal limit;

    CreditRating(BigDecimal limit) {
        this.limit = limit;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public boolean isLimitSatisfied(BigDecimal creditLimit) {
        if (limit == null || creditLimit == null)
            return true;
        return creditLimit.compareTo(limit) <= 0;
    }
}
