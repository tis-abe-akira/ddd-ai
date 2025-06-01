package com.example.syndicatelending.position.domain;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FacilityTest {
    @Test
    void 正常系_シェア合計100パーセントでFacility生成できる() {
        List<SharePie> pies = List.of(
                new SharePie(1L, Percentage.of(60)),
                new SharePie(2L, Percentage.of(40)));
        Facility f = new Facility(10L, Money.of(1000000000), "JPY",
                LocalDate.now(), LocalDate.now().plusYears(5), "LIBOR+1.0%", pies);
        assertEquals(2, f.getSharePies().size());
        assertEquals(Percentage.of(60), f.getSharePies().get(0).getShare());
    }

    @Test
    void 異常系_シェア合計100未満で例外() {
        List<SharePie> pies = List.of(
                new SharePie(1L, Percentage.of(50)),
                new SharePie(2L, Percentage.of(40)));
        Exception ex = assertThrows(IllegalArgumentException.class, () -> new Facility(10L, Money.of(1000000000), "JPY",
                LocalDate.now(), LocalDate.now().plusYears(5), "LIBOR+1.0%", pies));
        assertTrue(ex.getMessage().contains("SharePieの合計は100%"));
    }

    @Test
    void 異常系_シェア合計100超で例外() {
        List<SharePie> pies = List.of(
                new SharePie(1L, Percentage.of(70)),
                new SharePie(2L, Percentage.of(40)));
        Exception ex = assertThrows(IllegalArgumentException.class, () -> new Facility(10L, Money.of(1000000000), "JPY",
                LocalDate.now(), LocalDate.now().plusYears(5), "LIBOR+1.0%", pies));
        assertTrue(ex.getMessage().contains("SharePieの合計は100%"));
    }
}
