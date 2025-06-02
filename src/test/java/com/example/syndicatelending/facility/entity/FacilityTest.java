package com.example.syndicatelending.facility.entity;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class FacilityTest {

    @Test
    void SharePieの合計が100パーセントの場合はバリデーションが成功する() {
        // Given
        Facility facility = createTestFacility();
        
        SharePie pie1 = new SharePie();
        pie1.setShare(Percentage.of(BigDecimal.valueOf(0.4))); // 40%
        pie1.setFacility(facility);
        
        SharePie pie2 = new SharePie();
        pie2.setShare(Percentage.of(BigDecimal.valueOf(0.35))); // 35%
        pie2.setFacility(facility);
        
        SharePie pie3 = new SharePie();
        pie3.setShare(Percentage.of(BigDecimal.valueOf(0.25))); // 25%
        pie3.setFacility(facility);
        
        List<SharePie> sharePies = Arrays.asList(pie1, pie2, pie3);
        facility.setSharePies(sharePies);

        // When & Then
        assertThatCode(() -> facility.validateSharePie())
                .doesNotThrowAnyException();
    }

    @Test
    void SharePieの合計が100パーセント未満の場合はバリデーションでエラーになる() {
        // Given
        Facility facility = createTestFacility();
        
        SharePie pie1 = new SharePie();
        pie1.setShare(Percentage.of(BigDecimal.valueOf(0.4))); // 40%
        pie1.setFacility(facility);
        
        SharePie pie2 = new SharePie();
        pie2.setShare(Percentage.of(BigDecimal.valueOf(0.35))); // 35%
        pie2.setFacility(facility);
        
        SharePie pie3 = new SharePie();
        pie3.setShare(Percentage.of(BigDecimal.valueOf(0.2))); // 20% (合計95%)
        pie3.setFacility(facility);
        
        List<SharePie> sharePies = Arrays.asList(pie1, pie2, pie3);
        facility.setSharePies(sharePies);

        // When & Then
        assertThatThrownBy(() -> facility.validateSharePie())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SharePieの合計は100%でなければなりません");
    }

    @Test
    void SharePieの合計が100パーセント超過の場合はバリデーションでエラーになる() {
        // Given
        Facility facility = createTestFacility();
        
        SharePie pie1 = new SharePie();
        pie1.setShare(Percentage.of(BigDecimal.valueOf(0.4))); // 40%
        pie1.setFacility(facility);
        
        SharePie pie2 = new SharePie();
        pie2.setShare(Percentage.of(BigDecimal.valueOf(0.35))); // 35%
        pie2.setFacility(facility);
        
        SharePie pie3 = new SharePie();
        pie3.setShare(Percentage.of(BigDecimal.valueOf(0.3))); // 30% (合計105%)
        pie3.setFacility(facility);
        
        List<SharePie> sharePies = Arrays.asList(pie1, pie2, pie3);
        facility.setSharePies(sharePies);

        // When & Then
        assertThatThrownBy(() -> facility.validateSharePie())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SharePieの合計は100%でなければなりません");
    }

    @Test
    void SharePieが空の場合はバリデーションでエラーになる() {
        // Given
        Facility facility = createTestFacility();
        
        // When & Then
        assertThatThrownBy(() -> facility.validateSharePie())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SharePieが設定されていません");
    }

    private Facility createTestFacility() {
        return new Facility(
                1L, // syndicateId
                Money.of(BigDecimal.valueOf(5000000)), // commitment
                "USD", // currency
                LocalDate.of(2025, 1, 1), // startDate
                LocalDate.of(2026, 1, 1), // endDate
                "LIBOR + 2%" // interestTerms
        );
    }
}
