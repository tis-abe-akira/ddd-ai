package com.example.syndicatelending.party.entity;

import com.example.syndicatelending.common.domain.model.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InvestorInvestmentAmountTest {

    private Investor investor;

    @BeforeEach
    void setUp() {
        investor = new Investor("Test Investor", "test@example.com", "123-456-7890", 
                               "COMP001", new BigDecimal("1000000"), InvestorType.BANK);
    }

    @Test
    void 初期状態では投資額がゼロ() {
        assertEquals(Money.zero(), investor.getCurrentInvestmentAmount());
    }

    @Test
    void 投資額を増加できる() {
        Money increasedAmount = Money.of(new BigDecimal("100000"));
        investor.increaseInvestmentAmount(increasedAmount);
        
        assertEquals(increasedAmount, investor.getCurrentInvestmentAmount());
    }

    @Test
    void 複数回投資額を増加できる() {
        Money firstAmount = Money.of(new BigDecimal("100000"));
        Money secondAmount = Money.of(new BigDecimal("50000"));
        
        investor.increaseInvestmentAmount(firstAmount);
        investor.increaseInvestmentAmount(secondAmount);
        
        Money expected = firstAmount.add(secondAmount);
        assertEquals(expected, investor.getCurrentInvestmentAmount());
    }

    @Test
    void 投資額を減少できる() {
        Money initialAmount = Money.of(new BigDecimal("100000"));
        Money decreaseAmount = Money.of(new BigDecimal("30000"));
        
        investor.increaseInvestmentAmount(initialAmount);
        investor.decreaseInvestmentAmount(decreaseAmount);
        
        Money expected = initialAmount.subtract(decreaseAmount);
        assertEquals(expected, investor.getCurrentInvestmentAmount());
    }

    @Test
    void 投資額を直接設定できる() {
        Money amount = Money.of(new BigDecimal("250000"));
        investor.setCurrentInvestmentAmount(amount);
        
        assertEquals(amount, investor.getCurrentInvestmentAmount());
    }

    @Test
    void null値で投資額増加を試みても変更されない() {
        Money initialAmount = investor.getCurrentInvestmentAmount();
        investor.increaseInvestmentAmount(null);
        
        assertEquals(initialAmount, investor.getCurrentInvestmentAmount());
    }

    @Test
    void null値で投資額減少を試みても変更されない() {
        Money initialAmount = Money.of(new BigDecimal("100000"));
        investor.setCurrentInvestmentAmount(initialAmount);
        investor.decreaseInvestmentAmount(null);
        
        assertEquals(initialAmount, investor.getCurrentInvestmentAmount());
    }

    @Test
    void 負の値で投資額増加を試みても変更されない() {
        Money initialAmount = investor.getCurrentInvestmentAmount();
        Money negativeAmount = Money.of(new BigDecimal("-50000"));
        
        investor.increaseInvestmentAmount(negativeAmount);
        
        assertEquals(initialAmount, investor.getCurrentInvestmentAmount());
    }

    @Test
    void 負の値で投資額減少を試みても変更されない() {
        Money initialAmount = Money.of(new BigDecimal("100000"));
        investor.setCurrentInvestmentAmount(initialAmount);
        Money negativeAmount = Money.of(new BigDecimal("-30000"));
        
        investor.decreaseInvestmentAmount(negativeAmount);
        
        assertEquals(initialAmount, investor.getCurrentInvestmentAmount());
    }
}