package com.example.syndicatelending.facility.domain;

import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import com.example.syndicatelending.facility.dto.CreateFacilityRequest;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.party.repository.BorrowerRepository;
import com.example.syndicatelending.party.repository.InvestorRepository;
import com.example.syndicatelending.syndicate.repository.SyndicateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacilityValidatorTest {

    @Mock
    private SyndicateRepository syndicateRepository;

    @Mock
    private InvestorRepository investorRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private FacilityRepository facilityRepository;

    private FacilityValidator facilityValidator;

    @BeforeEach
    void setUp() {
        facilityValidator = new FacilityValidator(
                syndicateRepository,
                investorRepository,
                borrowerRepository,
                facilityRepository);
    }

    @Test
    void SharePieの合計が100パーセントの場合はバリデーションが成功する() {
        // Given
        CreateFacilityRequest request = createValidFacilityRequest();
        when(syndicateRepository.existsById(1L)).thenReturn(true);

        // When & Then
        assertThatCode(() -> facilityValidator.validateCreateFacilityRequest(request))
                .doesNotThrowAnyException();
    }

    @Test
    void SharePieの合計が100パーセント未満の場合はバリデーションでエラーになる() {
        // Given
        CreateFacilityRequest request = createInvalidFacilityRequest();
        when(syndicateRepository.existsById(1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> facilityValidator.validateCreateFacilityRequest(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("SharePieの合計は100%である必要があります");
    }

    @Test
    void SharePieの合計が100パーセント超過の場合はバリデーションでエラーになる() {
        // Given
        CreateFacilityRequest request = createOverFacilityRequest();
        when(syndicateRepository.existsById(1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> facilityValidator.validateCreateFacilityRequest(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("SharePieの合計は100%である必要があります");
    }

    @Test
    void SharePieが空の場合はバリデーションでエラーになる() {
        // Given
        CreateFacilityRequest request = createEmptySharePieRequest();

        // When & Then
        assertThatThrownBy(() -> facilityValidator.validateCreateFacilityRequest(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("SharePieは最低1つ必要です");
    }

    @Test
    void 同一のInvestorが複数のSharePieに含まれる場合はバリデーションでエラーになる() {
        // Given
        CreateFacilityRequest request = createDuplicateInvestorRequest();
        when(syndicateRepository.existsById(1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> facilityValidator.validateCreateFacilityRequest(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("同一のInvestorが複数のSharePieに含まれています");
    }

    @Test
    void コミットメント金額が0以下の場合はバリデーションでエラーになる() {
        // Given
        CreateFacilityRequest request = createValidFacilityRequest();
        request.setCommitment(Money.of(BigDecimal.ZERO));

        // When & Then
        assertThatThrownBy(() -> facilityValidator.validateCreateFacilityRequest(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("コミットメント金額は正の値である必要があります");
    }

    @Test
    void 開始日が終了日より後の場合はバリデーションでエラーになる() {
        // Given
        CreateFacilityRequest request = createValidFacilityRequest();
        request.setStartDate(LocalDate.of(2026, 1, 1));
        request.setEndDate(LocalDate.of(2025, 1, 1));

        // When & Then
        assertThatThrownBy(() -> facilityValidator.validateCreateFacilityRequest(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("開始日は終了日より前である必要があります");
    }

    @Test
    void Syndicateが存在しない場合はバリデーションでエラーになる() {
        // Given
        CreateFacilityRequest request = createValidFacilityRequest();
        when(syndicateRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> facilityValidator.validateCreateFacilityRequest(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("指定されたSyndicateが存在しません");
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

    private CreateFacilityRequest createOverFacilityRequest() {
        CreateFacilityRequest request = new CreateFacilityRequest();
        request.setSyndicateId(1L);
        request.setCommitment(Money.of(BigDecimal.valueOf(5000000)));
        request.setCurrency("USD");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2026, 1, 1));
        request.setInterestTerms("LIBOR + 2%");

        // 合計105%のSharePie（不正）
        CreateFacilityRequest.SharePieRequest pie1 = new CreateFacilityRequest.SharePieRequest();
        pie1.setInvestorId(1L);
        pie1.setShare(Percentage.of(BigDecimal.valueOf(0.4))); // 40%

        CreateFacilityRequest.SharePieRequest pie2 = new CreateFacilityRequest.SharePieRequest();
        pie2.setInvestorId(2L);
        pie2.setShare(Percentage.of(BigDecimal.valueOf(0.35))); // 35%

        CreateFacilityRequest.SharePieRequest pie3 = new CreateFacilityRequest.SharePieRequest();
        pie3.setInvestorId(3L);
        pie3.setShare(Percentage.of(BigDecimal.valueOf(0.3))); // 30%

        List<CreateFacilityRequest.SharePieRequest> sharePies = Arrays.asList(pie1, pie2, pie3);
        request.setSharePies(sharePies);

        return request;
    }

    private CreateFacilityRequest createEmptySharePieRequest() {
        CreateFacilityRequest request = new CreateFacilityRequest();
        request.setSyndicateId(1L);
        request.setCommitment(Money.of(BigDecimal.valueOf(5000000)));
        request.setCurrency("USD");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2026, 1, 1));
        request.setInterestTerms("LIBOR + 2%");
        request.setSharePies(Arrays.asList()); // 空のSharePie

        return request;
    }

    private CreateFacilityRequest createDuplicateInvestorRequest() {
        CreateFacilityRequest request = new CreateFacilityRequest();
        request.setSyndicateId(1L);
        request.setCommitment(Money.of(BigDecimal.valueOf(5000000)));
        request.setCurrency("USD");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2026, 1, 1));
        request.setInterestTerms("LIBOR + 2%");

        // 同一のInvestorが重複
        CreateFacilityRequest.SharePieRequest pie1 = new CreateFacilityRequest.SharePieRequest();
        pie1.setInvestorId(1L);
        pie1.setShare(Percentage.of(BigDecimal.valueOf(0.5))); // 50%

        CreateFacilityRequest.SharePieRequest pie2 = new CreateFacilityRequest.SharePieRequest();
        pie2.setInvestorId(1L); // 重複
        pie2.setShare(Percentage.of(BigDecimal.valueOf(0.5))); // 50%

        List<CreateFacilityRequest.SharePieRequest> sharePies = Arrays.asList(pie1, pie2);
        request.setSharePies(sharePies);

        return request;
    }
}
