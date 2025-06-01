package com.example.syndicatelending.party.entity;

import com.example.syndicatelending.common.domain.model.Money;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CreditRatingTest {
    @Test
    void 上限以内ならtrue() {
        assertTrue(CreditRating.AA.isLimitSatisfied(Money.of(new BigDecimal("50000000"))));
        assertTrue(CreditRating.B.isLimitSatisfied(Money.of(new BigDecimal("1000000"))));
    }

    @Test
    void 上限超過ならfalse() {
        assertFalse(CreditRating.AA.isLimitSatisfied(Money.of(new BigDecimal("60000000"))));
        assertFalse(CreditRating.B.isLimitSatisfied(Money.of(new BigDecimal("3000000"))));
    }

    @Test
    void nullや未定義格付はtrue() {
        assertTrue(CreditRating.CCC.isLimitSatisfied(Money.of(new BigDecimal("999999999"))));
        assertTrue(CreditRating.AA.isLimitSatisfied((Money) null));
    }
}
