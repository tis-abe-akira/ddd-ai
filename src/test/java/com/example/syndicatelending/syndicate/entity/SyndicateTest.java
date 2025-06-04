package com.example.syndicatelending.syndicate.entity;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SyndicateTest {
    @Test
    void constructorAndGetterSetterWorks() {
        Syndicate s = new Syndicate("団A", 1L, 1L, List.of(2L, 3L)); // borrowerId: 1L を追加
        assertEquals("団A", s.getName());
        assertEquals(1L, s.getLeadBankId());
        assertEquals(1L, s.getBorrowerId());
        assertEquals(List.of(2L, 3L), s.getMemberInvestorIds());
        s.setName("団B");
        assertEquals("団B", s.getName());
    }

    @Test
    void equalsAndHashCodeById() {
        Syndicate s1 = new Syndicate("A", 1L, 1L, List.of(2L)); // borrowerId: 1L を追加
        Syndicate s2 = new Syndicate("A", 1L, 1L, List.of(2L)); // borrowerId: 1L を追加
        s1.setId(10L);
        s2.setId(10L);
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }
}
