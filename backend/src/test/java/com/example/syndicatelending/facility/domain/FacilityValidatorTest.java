package com.example.syndicatelending.facility.domain;

import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import com.example.syndicatelending.facility.dto.CreateFacilityRequest;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.party.entity.Borrower;
import com.example.syndicatelending.party.entity.Investor;
import com.example.syndicatelending.party.entity.InvestorType;
import com.example.syndicatelending.party.repository.BorrowerRepository;
import com.example.syndicatelending.party.repository.InvestorRepository;
import com.example.syndicatelending.syndicate.entity.Syndicate;
import com.example.syndicatelending.syndicate.repository.SyndicateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

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

        // Syndicateのモック設定
        Syndicate mockSyndicate = new Syndicate("Test Syndicate", 1L, 1L, Arrays.asList(1L, 2L, 3L));
        mockSyndicate.setId(1L);
        when(syndicateRepository.existsById(1L)).thenReturn(true);
        when(syndicateRepository.findById(1L)).thenReturn(Optional.of(mockSyndicate));

        // Investorのモック設定
        Investor investor1 = new Investor("Investor 1", null, null, null, null, InvestorType.BANK);
        investor1.setId(1L);
        investor1.setIsActive(true);
        Investor investor2 = new Investor("Investor 2", null, null, null, null, InvestorType.BANK);
        investor2.setId(2L);
        investor2.setIsActive(true);
        Investor investor3 = new Investor("Investor 3", null, null, null, null, InvestorType.BANK);
        investor3.setId(3L);
        investor3.setIsActive(true);

        when(investorRepository.findById(1L)).thenReturn(Optional.of(investor1));
        when(investorRepository.findById(2L)).thenReturn(Optional.of(investor2));
        when(investorRepository.findById(3L)).thenReturn(Optional.of(investor3));

        // Borrowerのモック設定
        Borrower mockBorrower = new Borrower("Test Borrower", null, null, null, Money.of(10000000), null);
        mockBorrower.setId(1L);
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(mockBorrower));

        // 既存Facilityのモック設定（空リスト）
        when(facilityRepository.findBySyndicateId(1L)).thenReturn(Collections.emptyList());

        // When & Then
        assertThatCode(() -> facilityValidator.validateCreateFacilityRequest(request))
                .doesNotThrowAnyException();
    }

    @Test
    void SharePieの合計が100パーセント未満の場合はバリデーションでエラーになる() {
        // Given
        CreateFacilityRequest request = createInvalidFacilityRequest();
        setupBasicMocks();

        // When & Then
        assertThatThrownBy(() -> facilityValidator.validateCreateFacilityRequest(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("SharePieの合計は100%である必要があります");
    }

    @Test
    void SharePieの合計が100パーセント超過の場合はバリデーションでエラーになる() {
        // Given
        CreateFacilityRequest request = createOverFacilityRequest();
        setupBasicMocks();

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

        // Syndicateのモック設定
        Syndicate mockSyndicate = new Syndicate("Test Syndicate", 1L, 1L, Arrays.asList(1L));
        mockSyndicate.setId(1L);
        when(syndicateRepository.existsById(1L)).thenReturn(true);
        when(syndicateRepository.findById(1L)).thenReturn(Optional.of(mockSyndicate));

        // 重複しているInvestor(ID=1)のモック設定のみ
        Investor investor1 = new Investor("Investor 1", null, null, null, null, InvestorType.BANK);
        investor1.setId(1L);
        investor1.setIsActive(true);
        when(investorRepository.findById(1L)).thenReturn(Optional.of(investor1));

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
        // 基本バリデーションで止まるため、最小限のモックのみ設定

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
        // 基本バリデーションで止まるため、最小限のモックのみ設定

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

    @Test
    void Investorが存在しない場合はバリデーションでエラーになる() {
        // Given
        CreateFacilityRequest request = createValidFacilityRequest();

        // Syndicateのモック設定
        Syndicate mockSyndicate = new Syndicate("Test Syndicate", 1L, 1L, Arrays.asList(1L, 2L, 3L));
        mockSyndicate.setId(1L);
        when(syndicateRepository.existsById(1L)).thenReturn(true);
        when(syndicateRepository.findById(1L)).thenReturn(Optional.of(mockSyndicate));

        // Investor 1のみ存在しない設定
        when(investorRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> facilityValidator.validateCreateFacilityRequest(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("指定されたInvestorが存在しません: id=1");
    }

    @Test
    void InvestorがSyndicateに所属していない場合はバリデーションでエラーになる() {
        // Given
        CreateFacilityRequest request = createValidFacilityRequest();

        // Syndicateのモック設定（Investor 1が所属していない）
        Syndicate mockSyndicate = new Syndicate("Test Syndicate", 1L, 1L, Arrays.asList(2L, 3L));
        mockSyndicate.setId(1L);
        when(syndicateRepository.existsById(1L)).thenReturn(true);
        when(syndicateRepository.findById(1L)).thenReturn(Optional.of(mockSyndicate));

        // Investorのモック設定
        Investor investor1 = new Investor("Investor 1", null, null, null, null, InvestorType.BANK);
        investor1.setId(1L);
        investor1.setIsActive(true);
        when(investorRepository.findById(1L)).thenReturn(Optional.of(investor1));

        // When & Then
        assertThatThrownBy(() -> facilityValidator.validateCreateFacilityRequest(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("InvestorはSyndicateメンバーではありません: investorId=1");
    }

    @Test
    void Investorが非アクティブの場合はバリデーションでエラーになる() {
        // Given
        CreateFacilityRequest request = createValidFacilityRequest();

        // Syndicateのモック設定
        Syndicate mockSyndicate = new Syndicate("Test Syndicate", 1L, 1L, Arrays.asList(1L, 2L, 3L));
        mockSyndicate.setId(1L);
        when(syndicateRepository.existsById(1L)).thenReturn(true);
        when(syndicateRepository.findById(1L)).thenReturn(Optional.of(mockSyndicate));

        // Investor 1が非アクティブの設定
        Investor investor1 = new Investor("Investor 1", null, null, null, null, InvestorType.BANK);
        investor1.setId(1L);
        investor1.setIsActive(false); // 非アクティブ
        when(investorRepository.findById(1L)).thenReturn(Optional.of(investor1));

        // When & Then
        assertThatThrownBy(() -> facilityValidator.validateCreateFacilityRequest(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("非アクティブなInvestorは投資できません: investorId=1");
    }

    @Test
    void コミットメント金額がBorrowerのクレジット限度額を超える場合はバリデーションでエラーになる() {
        // Given
        CreateFacilityRequest request = createValidFacilityRequest();
        request.setCommitment(Money.of(BigDecimal.valueOf(15000000))); // クレジット限度額を超える金額

        // Syndicateのモック設定
        Syndicate mockSyndicate = new Syndicate("Test Syndicate", 1L, 1L, Arrays.asList(1L, 2L, 3L));
        mockSyndicate.setId(1L);
        lenient().when(syndicateRepository.existsById(1L)).thenReturn(true);
        lenient().when(syndicateRepository.findById(1L)).thenReturn(Optional.of(mockSyndicate));

        // Investorのモック設定（requestで使用される1,2,3のみ）
        Investor investor1 = new Investor("Investor 1", null, null, null, null, InvestorType.BANK);
        investor1.setId(1L);
        investor1.setIsActive(true);
        Investor investor2 = new Investor("Investor 2", null, null, null, null, InvestorType.BANK);
        investor2.setId(2L);
        investor2.setIsActive(true);
        Investor investor3 = new Investor("Investor 3", null, null, null, null, InvestorType.BANK);
        investor3.setId(3L);
        investor3.setIsActive(true);

        lenient().when(investorRepository.findById(1L)).thenReturn(Optional.of(investor1));
        lenient().when(investorRepository.findById(2L)).thenReturn(Optional.of(investor2));
        lenient().when(investorRepository.findById(3L)).thenReturn(Optional.of(investor3));

        // Borrowerのモック設定（クレジット限度額: 10,000,000）
        Borrower mockBorrower = new Borrower("Test Borrower", null, null, null, Money.of(10000000), null);
        mockBorrower.setId(1L);
        lenient().when(borrowerRepository.findById(1L)).thenReturn(Optional.of(mockBorrower));

        // 既存Facilityのモック設定（空リスト）
        lenient().when(facilityRepository.findBySyndicateId(1L)).thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> facilityValidator.validateCreateFacilityRequest(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("FacilityのCommitment(15000000.00)がBorrowerのCreditLimit(10000000.00)を超えています");
    }

    @Test
    void 既存Facilityとの合計コミットメント金額がBorrowerのクレジット限度額を超える場合はバリデーションでエラーになる() {
        // Given
        CreateFacilityRequest request = createValidFacilityRequest();
        request.setCommitment(Money.of(BigDecimal.valueOf(6000000))); // 既存と合わせて限度額を超える

        // Syndicateのモック設定
        Syndicate mockSyndicate = new Syndicate("Test Syndicate", 1L, 1L, Arrays.asList(1L, 2L, 3L));
        mockSyndicate.setId(1L);
        lenient().when(syndicateRepository.existsById(1L)).thenReturn(true);
        lenient().when(syndicateRepository.findById(1L)).thenReturn(Optional.of(mockSyndicate));

        // Investorのモック設定
        Investor investor1 = new Investor("Investor 1", null, null, null, null, InvestorType.BANK);
        investor1.setId(1L);
        investor1.setIsActive(true);
        Investor investor2 = new Investor("Investor 2", null, null, null, null, InvestorType.BANK);
        investor2.setId(2L);
        investor2.setIsActive(true);
        Investor investor3 = new Investor("Investor 3", null, null, null, null, InvestorType.BANK);
        investor3.setId(3L);
        investor3.setIsActive(true);

        lenient().when(investorRepository.findById(1L)).thenReturn(Optional.of(investor1));
        lenient().when(investorRepository.findById(2L)).thenReturn(Optional.of(investor2));
        lenient().when(investorRepository.findById(3L)).thenReturn(Optional.of(investor3));

        // Borrowerのモック設定（クレジット限度額: 10,000,000）
        Borrower mockBorrower = new Borrower("Test Borrower", null, null, null, Money.of(10000000), null);
        mockBorrower.setId(1L);
        lenient().when(borrowerRepository.findById(1L)).thenReturn(Optional.of(mockBorrower));

        // 既存Facilityのモック設定（既に5,000,000のコミットメントが存在）
        Facility existingFacility = new Facility(1L, Money.of(5000000), "USD",
                LocalDate.of(2024, 1, 1), LocalDate.of(2025, 12, 31), "LIBOR + 1%");
        lenient().when(facilityRepository.findBySyndicateId(1L)).thenReturn(Arrays.asList(existingFacility));

        // When & Then
        assertThatThrownBy(() -> facilityValidator.validateCreateFacilityRequest(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("総Commitment(11000000.00)がBorrowerのCreditLimit(10000000.00)を超えています");
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

    /**
     * 基本的なモック設定（Syndicate、Investor、Borrower、Facility）
     */
    private void setupBasicMocks() {
        // Syndicateのモック設定
        Syndicate mockSyndicate = new Syndicate("Test Syndicate", 1L, 1L, Arrays.asList(1L, 2L, 3L));
        mockSyndicate.setId(1L);
        when(syndicateRepository.existsById(1L)).thenReturn(true);
        when(syndicateRepository.findById(1L)).thenReturn(Optional.of(mockSyndicate));

        // Investorのモック設定
        Investor investor1 = new Investor("Investor 1", null, null, null, null, InvestorType.BANK);
        investor1.setId(1L);
        investor1.setIsActive(true);
        Investor investor2 = new Investor("Investor 2", null, null, null, null, InvestorType.BANK);
        investor2.setId(2L);
        investor2.setIsActive(true);
        Investor investor3 = new Investor("Investor 3", null, null, null, null, InvestorType.BANK);
        investor3.setId(3L);
        investor3.setIsActive(true);

        when(investorRepository.findById(1L)).thenReturn(Optional.of(investor1));
        when(investorRepository.findById(2L)).thenReturn(Optional.of(investor2));
        when(investorRepository.findById(3L)).thenReturn(Optional.of(investor3));

        // Borrowerのモック設定
        Borrower mockBorrower = new Borrower("Test Borrower", null, null, null, Money.of(10000000), null);
        mockBorrower.setId(1L);
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(mockBorrower));

        // 既存Facilityのモック設定（空リスト）
        when(facilityRepository.findBySyndicateId(1L)).thenReturn(Collections.emptyList());
    }
}
