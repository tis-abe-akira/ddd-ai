package com.example.syndicatelending.facility.service;

import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import com.example.syndicatelending.facility.domain.FacilityValidator;
import com.example.syndicatelending.facility.dto.CreateFacilityRequest;
import com.example.syndicatelending.facility.dto.UpdateFacilityRequest;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private FacilityValidator facilityValidator;

    @Mock
    private com.example.syndicatelending.facility.repository.SharePieRepository sharePieRepository;

    @Mock
    private com.example.syndicatelending.facility.repository.FacilityInvestmentRepository facilityInvestmentRepository;

    @Mock
    private com.example.syndicatelending.syndicate.repository.SyndicateRepository syndicateRepository;

    @InjectMocks
    private FacilityService facilityService;

    @Test
    void SharePieの合計が100パーセントの場合は正常にFacilityが作成される() {
        // Given
        CreateFacilityRequest request = createValidFacilityRequest();

        Facility savedFacility = new Facility();
        savedFacility.setId(1L);
        savedFacility.setSyndicateId(1L);
        savedFacility.setCommitment(Money.of(BigDecimal.valueOf(5000000)));
        savedFacility.setSharePies(Arrays.asList()); // SharePieのモック設定
        when(facilityRepository.save(any(Facility.class))).thenReturn(savedFacility);

        // Syndicateのモック設定
        com.example.syndicatelending.syndicate.entity.Syndicate syndicate = 
            new com.example.syndicatelending.syndicate.entity.Syndicate();
        syndicate.setBorrowerId(100L); // borrowerIdを設定
        when(syndicateRepository.findById(1L)).thenReturn(java.util.Optional.of(syndicate));

        // When
        facilityService.createFacility(request);

        // Then
        verify(facilityValidator).validateCreateFacilityRequest(request);
        verify(facilityRepository).save(any(Facility.class));
        verify(syndicateRepository).findById(1L); // Syndicate取得確認
        verify(facilityInvestmentRepository).saveAll(any(List.class)); // FacilityInvestment保存確認
    }

    @Test
    void SharePieの合計が100パーセント未満の場合はバリデーションエラーになる() {
        // Given
        CreateFacilityRequest request = createInvalidFacilityRequest();
        doThrow(new BusinessRuleViolationException("SharePieの合計は100%である必要があります"))
                .when(facilityValidator).validateCreateFacilityRequest(request);

        // When & Then
        assertThatThrownBy(() -> facilityService.createFacility(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("SharePieの合計は100%である必要があります");

        verify(facilityValidator).validateCreateFacilityRequest(request);
        verify(facilityRepository, never()).save(any(Facility.class));
    }

    @Test
    void 正常にFacilityが更新される() {
        // Given
        Long facilityId = 1L;
        UpdateFacilityRequest request = createValidUpdateFacilityRequest();
        request.setSyndicateId(1L);
        request.setCommitment(Money.of(BigDecimal.valueOf(6000000))); // 更新: Commitmentを6000000に
        request.setVersion(1L); // バージョン情報

        Facility existingFacility = new Facility();
        existingFacility.setId(facilityId);
        existingFacility.setVersion(1L); // 同じバージョン
        when(facilityRepository.findById(facilityId)).thenReturn(java.util.Optional.of(existingFacility));
        
        Facility savedFacility = new Facility();
        savedFacility.setId(facilityId);
        savedFacility.setSyndicateId(1L);
        savedFacility.setCommitment(Money.of(BigDecimal.valueOf(6000000)));
        savedFacility.setSharePies(Arrays.asList()); // SharePieのモック設定
        when(facilityRepository.save(any(Facility.class))).thenReturn(savedFacility);

        // Syndicateのモック設定
        com.example.syndicatelending.syndicate.entity.Syndicate syndicate = 
            new com.example.syndicatelending.syndicate.entity.Syndicate();
        syndicate.setBorrowerId(100L); // borrowerIdを設定
        when(syndicateRepository.findById(1L)).thenReturn(java.util.Optional.of(syndicate));

        // When
        facilityService.updateFacility(facilityId, request);

        // Then
        verify(facilityValidator).validateUpdateFacilityRequest(eq(request), eq(facilityId));
        verify(facilityRepository).findById(facilityId);
        verify(facilityRepository).save(any(Facility.class));
        verify(facilityInvestmentRepository).deleteByFacilityId(facilityId); // FacilityInvestment削除確認
        verify(syndicateRepository).findById(1L); // Syndicate取得確認
        verify(facilityInvestmentRepository).saveAll(any(List.class)); // FacilityInvestment再生成確認
    }

    @Test
    void 存在しないFacilityを更新しようとした場合はエラーになる() {
        // Given
        Long facilityId = 1L;
        UpdateFacilityRequest request = createValidUpdateFacilityRequest();

        when(facilityRepository.findById(facilityId)).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() -> facilityService.updateFacility(facilityId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Facility not found");

        verify(facilityRepository).findById(facilityId);
        verify(facilityRepository, never()).save(any(Facility.class));
    }

    @Test
    void バージョンが異なる場合は楽観的排他制御でエラーになる() {
        // Given
        Long facilityId = 1L;
        UpdateFacilityRequest request = createValidUpdateFacilityRequest();
        request.setVersion(1L); // リクエストのバージョン

        Facility existingFacility = new Facility();
        existingFacility.setId(facilityId);
        existingFacility.setVersion(2L); // 現在のバージョンが異なる
        when(facilityRepository.findById(facilityId)).thenReturn(java.util.Optional.of(existingFacility));
        // Spring Data JPAのOptimisticLockingFailureExceptionをシミュレート
        when(facilityRepository.save(any(Facility.class)))
                .thenThrow(new OptimisticLockingFailureException("Version mismatch"));

        // When & Then
        assertThatThrownBy(() -> facilityService.updateFacility(facilityId, request))
                .isInstanceOf(OptimisticLockingFailureException.class);

        verify(facilityRepository).findById(facilityId);
        verify(facilityRepository).save(any(Facility.class));
    }

    @Test
    void 正常にFacilityが削除される() {
        // Given
        Long facilityId = 1L;

        when(facilityRepository.existsById(facilityId)).thenReturn(true);

        // When
        facilityService.deleteFacility(facilityId);

        // Then
        verify(facilityRepository).existsById(facilityId);
        verify(facilityRepository).deleteById(facilityId);
    }

    @Test
    void 存在しないFacilityを削除しようとした場合はエラーになる() {
        // Given
        Long facilityId = 1L;

        when(facilityRepository.existsById(facilityId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> facilityService.deleteFacility(facilityId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Facility not found");

        verify(facilityRepository).existsById(facilityId);
        verify(facilityRepository, never()).deleteById(facilityId);
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

    private UpdateFacilityRequest createValidUpdateFacilityRequest() {
        UpdateFacilityRequest request = new UpdateFacilityRequest();
        request.setSyndicateId(1L);
        request.setCommitment(Money.of(BigDecimal.valueOf(5000000)));
        request.setCurrency("USD");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2026, 1, 1));
        request.setInterestTerms("LIBOR + 2%");
        request.setVersion(1L); // デフォルトバージョン

        // 合計100%のSharePie
        UpdateFacilityRequest.SharePieRequest pie1 = new UpdateFacilityRequest.SharePieRequest();
        pie1.setInvestorId(1L);
        pie1.setShare(Percentage.of(BigDecimal.valueOf(0.4))); // 40%

        UpdateFacilityRequest.SharePieRequest pie2 = new UpdateFacilityRequest.SharePieRequest();
        pie2.setInvestorId(2L);
        pie2.setShare(Percentage.of(BigDecimal.valueOf(0.35))); // 35%

        UpdateFacilityRequest.SharePieRequest pie3 = new UpdateFacilityRequest.SharePieRequest();
        pie3.setInvestorId(3L);
        pie3.setShare(Percentage.of(BigDecimal.valueOf(0.25))); // 25%

        List<UpdateFacilityRequest.SharePieRequest> sharePies = Arrays.asList(pie1, pie2, pie3);
        request.setSharePies(sharePies);

        return request;
    }
}
