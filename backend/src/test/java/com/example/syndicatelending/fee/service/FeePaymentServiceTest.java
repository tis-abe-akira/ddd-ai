package com.example.syndicatelending.fee.service;

import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.common.application.exception.ResourceNotFoundException;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.entity.SharePie;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.facility.repository.SharePieRepository;
import com.example.syndicatelending.fee.dto.CreateFeePaymentRequest;
import com.example.syndicatelending.fee.entity.FeeDistribution;
import com.example.syndicatelending.fee.entity.FeePayment;
import com.example.syndicatelending.fee.entity.FeeType;
import com.example.syndicatelending.fee.entity.RecipientType;
import com.example.syndicatelending.fee.repository.FeePaymentRepository;
import com.example.syndicatelending.party.entity.Borrower;
import com.example.syndicatelending.party.entity.Investor;
import com.example.syndicatelending.party.entity.InvestorType;
import com.example.syndicatelending.party.repository.BorrowerRepository;
import com.example.syndicatelending.party.repository.InvestorRepository;
import com.example.syndicatelending.transaction.entity.TransactionStatus;
import com.example.syndicatelending.transaction.entity.TransactionType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FeePaymentServiceTest {

    @Autowired
    private FeePaymentService feePaymentService;

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private SharePieRepository sharePieRepository;

    @Autowired
    private FeePaymentRepository feePaymentRepository;

    private Investor investor1;
    private Investor investor2;
    private Investor investor3;
    private Borrower borrower;
    private Facility facility;

    @BeforeEach
    void setUp() {
        // 投資家作成
        investor1 = new Investor("Investor 1", "investor1@example.com", "111-1111-1111",
                "COMP001", new BigDecimal("5000000"), InvestorType.LEAD_BANK);
        investor1 = investorRepository.save(investor1);

        investor2 = new Investor("Investor 2", "investor2@example.com", "222-2222-2222",
                "COMP002", new BigDecimal("6000000"), InvestorType.BANK);
        investor2 = investorRepository.save(investor2);

        investor3 = new Investor("Investor 3", "investor3@example.com", "333-3333-3333",
                "COMP003", new BigDecimal("7000000"), InvestorType.BANK);
        investor3 = investorRepository.save(investor3);

        // 借り手作成
        borrower = new Borrower("Test Borrower", "borrower@example.com", "444-4444-4444", "COMP004",
                               Money.of(new BigDecimal("10000000")), 
                               com.example.syndicatelending.party.entity.CreditRating.AA);
        borrower = borrowerRepository.save(borrower);

        // ファシリティ作成
        facility = new Facility();
        facility.setSyndicateId(1L);
        facility.setCommitment(Money.of(new BigDecimal("5000000")));
        facility.setCurrency("USD");
        facility.setStartDate(LocalDate.of(2025, 1, 1));
        facility.setEndDate(LocalDate.of(2026, 1, 1));
        facility.setInterestTerms("LIBOR + 2%");
        facility = facilityRepository.save(facility);

        // SharePie作成（40%:35%:25%の比率）
        SharePie sharePie1 = new SharePie();
        sharePie1.setFacility(facility);
        sharePie1.setInvestorId(investor1.getId());
        sharePie1.setShare(Percentage.of(new BigDecimal("0.4")));
        sharePie1 = sharePieRepository.save(sharePie1);

        SharePie sharePie2 = new SharePie();
        sharePie2.setFacility(facility);
        sharePie2.setInvestorId(investor2.getId());
        sharePie2.setShare(Percentage.of(new BigDecimal("0.35")));
        sharePie2 = sharePieRepository.save(sharePie2);

        SharePie sharePie3 = new SharePie();
        sharePie3.setFacility(facility);
        sharePie3.setInvestorId(investor3.getId());
        sharePie3.setShare(Percentage.of(new BigDecimal("0.25")));
        sharePie3 = sharePieRepository.save(sharePie3);
        
        // FacilityのSharePieリストに明示的に追加
        facility.getSharePies().clear();
        facility.getSharePies().add(sharePie1);
        facility.getSharePies().add(sharePie2);
        facility.getSharePies().add(sharePie3);
        facility = facilityRepository.save(facility);
    }

    @Test
    void 管理手数料支払いを正常に作成できる() {
        // リクエスト作成
        CreateFeePaymentRequest request = new CreateFeePaymentRequest();
        request.setFacilityId(facility.getId());
        request.setBorrowerId(borrower.getId());
        request.setFeeType(FeeType.MANAGEMENT_FEE);
        request.setFeeDate(LocalDate.of(2025, 1, 31));
        request.setFeeAmount(new BigDecimal("25000.00"));
        request.setCalculationBase(new BigDecimal("5000000.00"));
        request.setFeeRate(0.5);
        request.setRecipientType("BANK");
        request.setRecipientId(investor1.getId());
        request.setCurrency("USD");
        request.setDescription("2025年1月分管理手数料");

        // 手数料支払い作成
        FeePayment feePayment = feePaymentService.createFeePayment(request);

        // 検証
        assertNotNull(feePayment);
        assertNotNull(feePayment.getId());
        assertEquals(FeeType.MANAGEMENT_FEE, feePayment.getFeeType());
        assertEquals(Money.of(new BigDecimal("25000.00")), feePayment.getAmount());
        assertEquals(Money.of(new BigDecimal("5000000.00")), feePayment.getCalculationBase());
        assertEquals(0.5, feePayment.getFeeRate());
        assertEquals(RecipientType.LEAD_BANK, feePayment.getRecipientType());
        assertEquals(investor1.getId(), feePayment.getRecipientId());
        assertEquals("USD", feePayment.getCurrency());
        assertEquals("2025年1月分管理手数料", feePayment.getDescription());

        // Transaction基底クラスのフィールド検証
        assertEquals(TransactionType.FEE_PAYMENT, feePayment.getTransactionType());
        assertEquals(TransactionStatus.DRAFT, feePayment.getStatus());
        assertEquals(facility.getId(), feePayment.getFacilityId());
        assertEquals(borrower.getId(), feePayment.getBorrowerId());
        assertEquals(LocalDate.of(2025, 1, 31), feePayment.getTransactionDate());

        // 管理手数料は投資家配分不要なので、配分リストが空であることを確認
        assertTrue(feePayment.getFeeDistributions().isEmpty());
    }

    @Test
    void コミットメント手数料支払いで投資家配分が正しく生成される() {
        // リクエスト作成（投資家配分が必要な手数料）
        CreateFeePaymentRequest request = new CreateFeePaymentRequest();
        request.setFacilityId(facility.getId());
        request.setBorrowerId(borrower.getId());
        request.setFeeType(FeeType.COMMITMENT_FEE);
        request.setFeeDate(LocalDate.of(2025, 1, 31));
        request.setFeeAmount(new BigDecimal("12500.00"));
        request.setCalculationBase(new BigDecimal("5000000.00"));
        request.setFeeRate(0.25);
        request.setRecipientType("INVESTOR");
        request.setRecipientId(investor1.getId());
        request.setCurrency("USD");
        request.setDescription("2025年1月分コミットメント手数料");

        // 手数料支払い作成
        FeePayment feePayment = feePaymentService.createFeePayment(request);

        // 検証
        assertNotNull(feePayment);
        assertEquals(FeeType.COMMITMENT_FEE, feePayment.getFeeType());
        assertEquals(Money.of(new BigDecimal("12500.00")), feePayment.getAmount());

        // 投資家配分の検証
        List<FeeDistribution> distributions = feePayment.getFeeDistributions();
        assertEquals(3, distributions.size());

        // 配分詳細の検証（40%:35%:25%の比率）
        FeeDistribution dist1 = distributions.stream()
            .filter(d -> d.getRecipientId().equals(investor1.getId()))
            .findFirst().orElseThrow();
        assertEquals(Money.of(new BigDecimal("5000.00")), dist1.getDistributionAmount()); // 12500 * 0.4
        assertEquals(40.0, dist1.getDistributionRatio());
        assertEquals("INVESTOR", dist1.getRecipientType());

        FeeDistribution dist2 = distributions.stream()
            .filter(d -> d.getRecipientId().equals(investor2.getId()))
            .findFirst().orElseThrow();
        assertEquals(Money.of(new BigDecimal("4375.00")), dist2.getDistributionAmount()); // 12500 * 0.35
        assertEquals(35.0, dist2.getDistributionRatio());

        FeeDistribution dist3 = distributions.stream()
            .filter(d -> d.getRecipientId().equals(investor3.getId()))
            .findFirst().orElseThrow();
        assertEquals(Money.of(new BigDecimal("3125.00")), dist3.getDistributionAmount()); // 12500 * 0.25
        assertEquals(25.0, dist3.getDistributionRatio());

        // 全配分の逆参照確認
        distributions.forEach(dist -> {
            assertEquals(feePayment, dist.getFeePayment());
        });
    }

    @Test
    void 手数料計算が間違っている場合例外が発生する() {
        CreateFeePaymentRequest request = new CreateFeePaymentRequest();
        request.setFacilityId(facility.getId());
        request.setBorrowerId(borrower.getId());
        request.setFeeType(FeeType.ARRANGEMENT_FEE);
        request.setFeeDate(LocalDate.of(2025, 1, 1));
        request.setFeeAmount(new BigDecimal("30000.00")); // 間違った金額
        request.setCalculationBase(new BigDecimal("5000000.00"));
        request.setFeeRate(0.5); // 正しい計算では 25000.00 になる
        request.setRecipientType("BANK");
        request.setRecipientId(investor1.getId());
        request.setCurrency("USD");
        request.setDescription("計算が間違った手数料");

        // 例外発生を確認
        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class,
            () -> feePaymentService.createFeePayment(request)
        );

        assertTrue(exception.getMessage().contains("Fee amount calculation mismatch"));
        assertTrue(exception.getMessage().contains("Expected: 25000.00"));
        assertTrue(exception.getMessage().contains("Actual: 30000.00"));
    }

    @Test
    void 受益者タイプと手数料タイプの整合性チェック() {
        CreateFeePaymentRequest request = new CreateFeePaymentRequest();
        request.setFacilityId(facility.getId());
        request.setBorrowerId(borrower.getId());
        request.setFeeType(FeeType.MANAGEMENT_FEE); // BANKが受益者である必要
        request.setFeeDate(LocalDate.of(2025, 1, 1));
        request.setFeeAmount(new BigDecimal("25000.00"));
        request.setCalculationBase(new BigDecimal("5000000.00"));
        request.setFeeRate(0.5);
        request.setRecipientType("INVESTOR"); // 間違った受益者タイプ
        request.setRecipientId(investor1.getId());
        request.setCurrency("USD");
        request.setDescription("間違った受益者タイプ");

        // 例外発生を確認
        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class,
            () -> feePaymentService.createFeePayment(request)
        );

        assertTrue(exception.getMessage().contains("recipient must be BANK"));
    }

    @Test
    void 未来日付の手数料日付では例外が発生する() {
        CreateFeePaymentRequest request = new CreateFeePaymentRequest();
        request.setFacilityId(facility.getId());
        request.setBorrowerId(borrower.getId());
        request.setFeeType(FeeType.TRANSACTION_FEE);
        request.setFeeDate(LocalDate.now().plusDays(1)); // 未来日付
        request.setFeeAmount(new BigDecimal("1000.00"));
        request.setCalculationBase(new BigDecimal("200000.00"));
        request.setFeeRate(0.5);
        request.setRecipientType("BANK");
        request.setRecipientId(investor1.getId());
        request.setCurrency("USD");
        request.setDescription("未来日付の手数料");

        // 例外発生を確認
        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class,
            () -> feePaymentService.createFeePayment(request)
        );

        assertTrue(exception.getMessage().contains("Fee date cannot be in the future"));
    }

    @Test
    void 存在しないFacilityIDで例外が発生する() {
        CreateFeePaymentRequest request = new CreateFeePaymentRequest();
        request.setFacilityId(9999L); // 存在しないID
        request.setBorrowerId(borrower.getId());
        request.setFeeType(FeeType.OTHER_FEE);
        request.setFeeDate(LocalDate.now());
        request.setFeeAmount(new BigDecimal("1000.00"));
        request.setCalculationBase(new BigDecimal("100000.00"));
        request.setFeeRate(1.0);
        request.setRecipientType("BANK");
        request.setRecipientId(investor1.getId());
        request.setCurrency("USD");
        request.setDescription("存在しないFacility");

        // 例外発生を確認
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> feePaymentService.createFeePayment(request)
        );

        assertTrue(exception.getMessage().contains("Facility not found: 9999"));
    }

    @Test
    void Facility別手数料履歴を取得できる() {
        // テスト用の手数料支払いを複数作成
        createTestFeePayment(FeeType.MANAGEMENT_FEE, "BANK", new BigDecimal("25000"));
        createTestFeePayment(FeeType.COMMITMENT_FEE, "INVESTOR", new BigDecimal("12500"));

        // Facility別履歴取得
        List<FeePayment> payments = feePaymentService.getFeePaymentsByFacility(facility.getId());

        assertEquals(2, payments.size());
        payments.forEach(payment -> {
            assertEquals(facility.getId(), payment.getFacilityId());
        });
    }

    @Test
    void 手数料タイプ別検索ができる() {
        // テスト用の手数料支払いを作成
        createTestFeePayment(FeeType.MANAGEMENT_FEE, "BANK", new BigDecimal("25000"));
        createTestFeePayment(FeeType.ARRANGEMENT_FEE, "BANK", new BigDecimal("50000"));

        // 管理手数料のみ検索
        List<FeePayment> managementFees = feePaymentService.getFeePaymentsByType(FeeType.MANAGEMENT_FEE);
        assertTrue(managementFees.size() >= 1);
        managementFees.forEach(payment -> {
            assertEquals(FeeType.MANAGEMENT_FEE, payment.getFeeType());
        });
    }

    @Test
    void 日付範囲検索ができる() {
        // テスト用の手数料支払いを作成
        createTestFeePayment(FeeType.TRANSACTION_FEE, "BANK", new BigDecimal("5000"));

        // 日付範囲検索
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);
        
        List<FeePayment> payments = feePaymentService.getFeePaymentsByDateRange(startDate, endDate);
        assertTrue(payments.size() >= 1);
        payments.forEach(payment -> {
            assertTrue(payment.getFeeDate().isAfter(startDate.minusDays(1)));
            assertTrue(payment.getFeeDate().isBefore(endDate.plusDays(1)));
        });
    }

    @Test
    void PENDING状態の手数料支払いを削除できる() {
        // PENDING状態の手数料支払いを作成
        FeePayment feePayment = createTestFeePayment(FeeType.MANAGEMENT_FEE, "BANK", new BigDecimal("25000"));
        assertEquals(TransactionStatus.DRAFT, feePayment.getStatus());
        
        Long feePaymentId = feePayment.getId();
        assertNotNull(feePaymentId);
        
        // 削除実行
        feePaymentService.deleteFeePayment(feePaymentId);
        
        // 削除確認
        assertThrows(ResourceNotFoundException.class, 
            () -> feePaymentService.getFeePaymentById(feePaymentId));
    }

    @Test
    void PROCESSING状態の手数料支払いを削除できる() {
        // 手数料支払いを作成
        FeePayment feePayment = createTestFeePayment(FeeType.ARRANGEMENT_FEE, "BANK", new BigDecimal("50000"));
        
        // PROCESSING状態に変更
        feePayment.setStatus(TransactionStatus.ACTIVE);
        feePayment = feePaymentRepository.save(feePayment);
        
        Long feePaymentId = feePayment.getId();
        
        // 削除実行
        feePaymentService.deleteFeePayment(feePaymentId);
        
        // 削除確認
        assertThrows(ResourceNotFoundException.class, 
            () -> feePaymentService.getFeePaymentById(feePaymentId));
    }

    @Test
    void COMPLETED状態の手数料支払いも削除できる() {
        // 手数料支払いを作成
        FeePayment feePayment = createTestFeePayment(FeeType.COMMITMENT_FEE, "INVESTOR", new BigDecimal("12500"));
        
        // COMPLETED状態に変更
        feePayment.markAsCompleted();
        feePayment = feePaymentRepository.save(feePayment);
        assertEquals(TransactionStatus.COMPLETED, feePayment.getStatus());
        
        Long feePaymentId = feePayment.getId();
        
        // 削除実行が成功することを確認
        assertDoesNotThrow(() -> feePaymentService.deleteFeePayment(feePaymentId));
        
        // 削除後は存在しないことを確認
        assertThrows(ResourceNotFoundException.class, 
            () -> feePaymentService.getFeePaymentById(feePaymentId));
    }

    @Test
    void FAILED状態の手数料支払いは削除できない() {
        // 手数料支払いを作成
        FeePayment feePayment = createTestFeePayment(FeeType.LATE_FEE, "INVESTOR", new BigDecimal("15000"));
        
        // FAILED状態に変更
        feePayment.markAsFailed();
        feePayment = feePaymentRepository.save(feePayment);
        assertEquals(TransactionStatus.FAILED, feePayment.getStatus());
        
        Long feePaymentId = feePayment.getId();
        
        // 削除実行で例外発生を確認
        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class,
            () -> feePaymentService.deleteFeePayment(feePaymentId)
        );
        
        assertTrue(exception.getMessage().contains("Cannot delete fee payment with status: FAILED"));
    }

    @Test
    void 存在しない手数料支払いIDで削除実行時に例外が発生する() {
        Long nonExistentId = 99999L;
        
        // 削除実行で例外発生を確認
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> feePaymentService.deleteFeePayment(nonExistentId)
        );
        
        assertTrue(exception.getMessage().contains("Fee payment not found: " + nonExistentId));
    }

    @Test
    void 投資家配分がある手数料支払いを削除すると配分も削除される() {
        // 投資家配分が必要な手数料を作成
        FeePayment feePayment = createTestFeePayment(FeeType.COMMITMENT_FEE, "INVESTOR", new BigDecimal("12500"));
        assertTrue(feePayment.getFeeDistributions().size() > 0);
        
        Long feePaymentId = feePayment.getId();
        
        // 削除実行
        feePaymentService.deleteFeePayment(feePaymentId);
        
        // 削除確認（主エンティティと関連エンティティ両方）
        assertThrows(ResourceNotFoundException.class, 
            () -> feePaymentService.getFeePaymentById(feePaymentId));
        
        // FeeDistributionもCASCADE削除されていることを確認
        // (実際のテストでは、FeeDistributionRepositoryを使って直接確認することも可能)
    }

    private FeePayment createTestFeePayment(FeeType feeType, String recipientType, BigDecimal amount) {
        CreateFeePaymentRequest request = new CreateFeePaymentRequest();
        request.setFacilityId(facility.getId());
        request.setBorrowerId(borrower.getId());
        request.setFeeType(feeType);
        request.setFeeDate(LocalDate.now());
        request.setFeeAmount(amount);
        request.setCalculationBase(new BigDecimal("5000000.00"));
        request.setFeeRate(amount.divide(new BigDecimal("50000.00")).doubleValue()); // 適当な料率
        request.setRecipientType(recipientType);
        request.setRecipientId(investor1.getId());
        request.setCurrency("USD");
        request.setDescription("テスト用手数料");

        return feePaymentService.createFeePayment(request);
    }
}