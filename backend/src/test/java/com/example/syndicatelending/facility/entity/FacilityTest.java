package com.example.syndicatelending.facility.entity;

import com.example.syndicatelending.common.domain.model.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class FacilityTest {

    @Test
    void Facilityエンティティが正常に作成される() {
        // Given & When
        Facility facility = new Facility(
                1L, // syndicateId
                Money.of(BigDecimal.valueOf(5000000)), // commitment
                "USD", // currency
                LocalDate.of(2025, 1, 1), // startDate
                LocalDate.of(2026, 1, 1), // endDate
                "LIBOR + 2%" // interestTerms
        );

        // Then
        assertThat(facility).isNotNull();
        assertThat(facility.getSyndicateId()).isEqualTo(1L);
        assertThat(facility.getCommitment()).isEqualTo(Money.of(BigDecimal.valueOf(5000000)));
        assertThat(facility.getCurrency()).isEqualTo("USD");
        assertThat(facility.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(facility.getEndDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(facility.getInterestTerms()).isEqualTo("LIBOR + 2%");
    }

    @Test
    void SharePieリストの設定と取得ができる() {
        // Given
        Facility facility = createTestFacility();
        SharePie sharePie = new SharePie();
        sharePie.setInvestorId(1L);

        // When
        facility.getSharePies().add(sharePie);

        // Then
        assertThat(facility.getSharePies()).hasSize(1);
        assertThat(facility.getSharePies().get(0)).isEqualTo(sharePie);
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
