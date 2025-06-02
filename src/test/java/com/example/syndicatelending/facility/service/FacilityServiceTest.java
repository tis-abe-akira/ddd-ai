package com.example.syndicatelending.facility.service;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import com.example.syndicatelending.facility.dto.CreateFacilityRequest;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @InjectMocks
    private FacilityService facilityService;

    @Test
    void SharePieの合計が100パーセントの場合は正常にFacilityが作成される() {
        // Given
        CreateFacilityRequest request = createValidFacilityRequest();

        Facility savedFacility = new Facility();
        savedFacility.setId(1L);
        when(facilityRepository.save(any(Facility.class))).thenReturn(savedFacility);

        // When & Then
        assertThatCode(() -> facilityService.createFacility(request))
                .doesNotThrowAnyException();
    }

    @Test
    void SharePieの合計が100パーセント未満の場合はバリデーションエラーになる() {
        // Given
        CreateFacilityRequest request = createInvalidFacilityRequest();

        // When & Then
        assertThatThrownBy(() -> facilityService.createFacility(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SharePieの合計は100%でなければなりません");
    }

    private CreateFacilityRequest createValidFacilityRequest() {
        CreateFacilityRequest request = new CreateFacilityRequest();
        request.setSyndicateId(1L);
        request.setCommitment(Money.of(BigDecimal.valueOf(5000000)));
        request.setCurrency("USD");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2026, 1, 1));
        request.setInterestTerms("LIBOR + 2%");

        // 合計100%のSharePie
        CreateFacilityRequest.SharePieRequest pie1 = new CreateFacilityRequest.SharePieRequest();
        pie1.setInvestorId(1L);
        pie1.setShare(Percentage.of(BigDecimal.valueOf(0.4))); // 40%

        CreateFacilityRequest.SharePieRequest pie2 = new CreateFacilityRequest.SharePieRequest();
        pie2.setInvestorId(2L);
        pie2.setShare(Percentage.of(BigDecimal.valueOf(0.35))); // 35%

        CreateFacilityRequest.SharePieRequest pie3 = new CreateFacilityRequest.SharePieRequest();
        pie3.setInvestorId(3L);
        pie3.setShare(Percentage.of(BigDecimal.valueOf(0.25))); // 25%

        List<CreateFacilityRequest.SharePieRequest> sharePies = Arrays.asList(pie1, pie2, pie3);
        request.setSharePies(sharePies);

        return request;
    }

    private CreateFacilityRequest createInvalidFacilityRequest() {
        CreateFacilityRequest request = new CreateFacilityRequest();
        request.setSyndicateId(1L);
        request.setCommitment(Money.of(BigDecimal.valueOf(5000000)));
        request.setCurrency("USD");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2026, 1, 1));
        request.setInterestTerms("LIBOR + 2%");

        // 合計95%のSharePie（不正）
        CreateFacilityRequest.SharePieRequest pie1 = new CreateFacilityRequest.SharePieRequest();
        pie1.setInvestorId(1L);
        pie1.setShare(Percentage.of(BigDecimal.valueOf(0.4))); // 40%

        CreateFacilityRequest.SharePieRequest pie2 = new CreateFacilityRequest.SharePieRequest();
        pie2.setInvestorId(2L);
        pie2.setShare(Percentage.of(BigDecimal.valueOf(0.35))); // 35%

        CreateFacilityRequest.SharePieRequest pie3 = new CreateFacilityRequest.SharePieRequest();
        pie3.setInvestorId(3L);
        pie3.setShare(Percentage.of(BigDecimal.valueOf(0.2))); // 20%

        List<CreateFacilityRequest.SharePieRequest> sharePies = Arrays.asList(pie1, pie2, pie3);
        request.setSharePies(sharePies);

        return request;
    }
}
