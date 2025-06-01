package com.example.syndicatelending.party.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CreditRatingTest {
    @Test
    void 上限以内ならtrue() {
        assertTrue(CreditRating.AA.isLimitSatisfied(new BigDecimal("50000000")));
        assertTrue(CreditRating.B.isLimitSatisfied(new BigDecimal("1000000")));
    }

    @Test
    void 上限超過ならfalse() {
        assertFalse(CreditRating.AA.isLimitSatisfied(new BigDecimal("60000000")));
        assertFalse(CreditRating.B.isLimitSatisfied(new BigDecimal("3000000")));
    }

    @Test
    void nullや未定義格付はtrue() {
        assertTrue(CreditRating.CCC.isLimitSatisfied(new BigDecimal("999999999")));
        assertTrue(CreditRating.AA.isLimitSatisfied(null));
    }
}
