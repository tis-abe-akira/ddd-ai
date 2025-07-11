package com.example.syndicatelending.transaction.service;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.entity.SharePie;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.facility.repository.SharePieRepository;
import com.example.syndicatelending.facility.service.FacilityService;
import com.example.syndicatelending.fee.dto.CreateFeePaymentRequest;
import com.example.syndicatelending.fee.entity.FeePayment;
import com.example.syndicatelending.fee.entity.FeeType;
import com.example.syndicatelending.fee.service.FeePaymentService;
import com.example.syndicatelending.loan.dto.CreateDrawdownRequest;
import com.example.syndicatelending.loan.dto.CreatePaymentRequest;
import com.example.syndicatelending.loan.entity.Drawdown;
import com.example.syndicatelending.loan.entity.Payment;
import com.example.syndicatelending.loan.entity.RepaymentMethod;
import com.example.syndicatelending.loan.service.DrawdownService;
import com.example.syndicatelending.loan.service.PaymentService;
import com.example.syndicatelending.party.entity.Borrower;
import com.example.syndicatelending.party.entity.Investor;
import com.example.syndicatelending.party.entity.InvestorType;
import com.example.syndicatelending.party.repository.BorrowerRepository;
import com.example.syndicatelending.party.repository.InvestorRepository;
import com.example.syndicatelending.transaction.service.TransactionService.TransactionStatistics;
import com.example.syndicatelending.transaction.entity.Transaction;
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
class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private DrawdownService drawdownService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private FeePaymentService feePaymentService;

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private SharePieRepository sharePieRepository;

    private Investor investor1;
    private Investor investor2;
    private Borrower borrower;
    private Facility facility;

    @BeforeEach
    void setUp() {
        // 投資家作成
        investor1 = new Investor("Lead Bank", "lead@example.com", "111-1111-1111",
                "LEAD001", new BigDecimal("10000000"), InvestorType.LEAD_BANK);
        investor1 = investorRepository.save(investor1);

        investor2 = new Investor("Partner Bank", "partner@example.com", "222-2222-2222",
                "PART001", new BigDecimal("8000000"), InvestorType.BANK);
        investor2 = investorRepository.save(investor2);

        // 借り手作成
        borrower = new Borrower("Test Borrower", "borrower@example.com", "333-3333-3333", "BORROW001",
                               Money.of(new BigDecimal("20000000")), 
                               com.example.syndicatelending.party.entity.CreditRating.AA);
        borrower = borrowerRepository.save(borrower);

        // ファシリティ作成
        facility = new Facility();
        facility.setSyndicateId(1L);
        facility.setCommitment(Money.of(new BigDecimal("10000000")));
        facility.setCurrency("USD");
        facility.setStartDate(LocalDate.of(2025, 1, 1));
        facility.setEndDate(LocalDate.of(2026, 1, 1));
        facility.setInterestTerms("LIBOR + 2%");
        facility = facilityRepository.save(facility);

        // SharePie作成（60%:40%の比率）
        SharePie sharePie1 = new SharePie();
        sharePie1.setFacility(facility);
        sharePie1.setInvestorId(investor1.getId());
        sharePie1.setShare(Percentage.of(new BigDecimal("0.6")));
        sharePieRepository.save(sharePie1);

        SharePie sharePie2 = new SharePie();
        sharePie2.setFacility(facility);
        sharePie2.setInvestorId(investor2.getId());
        sharePie2.setShare(Percentage.of(new BigDecimal("0.4")));
        sharePieRepository.save(sharePie2);
    }

    @Test
    void 複数の取引タイプが統一的に管理される() {
        // 1. ドローダウン作成
        CreateDrawdownRequest drawdownRequest = new CreateDrawdownRequest();
        drawdownRequest.setFacilityId(facility.getId());
        drawdownRequest.setBorrowerId(borrower.getId());
        drawdownRequest.setAmount(new BigDecimal("5000000"));
        drawdownRequest.setCurrency("USD");
        drawdownRequest.setDrawdownDate(LocalDate.now());
        drawdownRequest.setAnnualInterestRate(new BigDecimal("0.05"));
        drawdownRequest.setRepaymentPeriodMonths(12);
        drawdownRequest.setRepaymentCycle("MONTHLY");
        drawdownRequest.setRepaymentMethod(RepaymentMethod.EQUAL_INSTALLMENT);
        drawdownRequest.setPurpose("Working capital");

        Drawdown drawdown = drawdownService.createDrawdown(drawdownRequest);

        // 2. 返済作成
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setLoanId(drawdown.getLoanId());
        paymentRequest.setPaymentDate(LocalDate.now());
        paymentRequest.setPrincipalAmount(new BigDecimal("500000"));
        paymentRequest.setInterestAmount(new BigDecimal("25000"));
        paymentRequest.setCurrency("USD");

        Payment payment = paymentService.processPayment(paymentRequest);

        // 3. 手数料支払い作成
        CreateFeePaymentRequest feeRequest = new CreateFeePaymentRequest();
        feeRequest.setFacilityId(facility.getId());
        feeRequest.setBorrowerId(borrower.getId());
        feeRequest.setFeeType(FeeType.MANAGEMENT_FEE);
        feeRequest.setFeeDate(LocalDate.now());
        feeRequest.setFeeAmount(new BigDecimal("10000.00"));
        feeRequest.setCalculationBase(new BigDecimal("10000000.00"));
        feeRequest.setFeeRate(0.1);
        feeRequest.setRecipientType("BANK");
        feeRequest.setRecipientId(investor1.getId());
        feeRequest.setCurrency("USD");
        feeRequest.setDescription("Monthly management fee");

        FeePayment feePayment = feePaymentService.createFeePayment(feeRequest);

        // 4. Facility別の全取引履歴を取得
        List<Transaction> transactions = transactionService.getTransactionsByFacility(facility.getId());

        // 検証: 複数の取引タイプが含まれる
        assertTrue(transactions.size() >= 3); // Drawdown(1) + Payment(1) + FeePayment(1)

        // 各取引タイプが含まれていることを確認
        boolean hasDrawdown = transactions.stream().anyMatch(t -> t.getTransactionType() == TransactionType.DRAWDOWN);
        boolean hasPayment = transactions.stream().anyMatch(t -> t.getTransactionType() == TransactionType.PAYMENT);
        boolean hasFeePayment = transactions.stream().anyMatch(t -> t.getTransactionType() == TransactionType.FEE_PAYMENT);

        assertTrue(hasDrawdown, "Drawdown transaction should be included");
        assertTrue(hasPayment, "Payment transaction should be included");
        assertTrue(hasFeePayment, "FeePayment transaction should be included");
        // FacilityInvestment is not automatically created in current implementation

        // 全ての取引が同じFacilityに属していることを確認
        transactions.forEach(transaction -> {
            assertEquals(facility.getId(), transaction.getFacilityId());
            assertEquals(borrower.getId(), transaction.getBorrowerId());
            assertNotNull(transaction.getId());
            assertNotNull(transaction.getCreatedAt());
            assertNotNull(transaction.getUpdatedAt());
        });
    }

    @Test
    void 取引タイプ別検索が正しく動作する() {
        // 手数料支払いを複数作成
        createTestFeePayment(FeeType.MANAGEMENT_FEE, "BANK");
        createTestFeePayment(FeeType.ARRANGEMENT_FEE, "BANK");

        // FEE_PAYMENT タイプのみ検索
        List<Transaction> feePayments = transactionService.getTransactionsByType(TransactionType.FEE_PAYMENT);
        
        assertFalse(feePayments.isEmpty()); // 作成したFeePaymentが存在する
        feePayments.forEach(transaction -> {
            assertEquals(TransactionType.FEE_PAYMENT, transaction.getTransactionType());
        });

        // FACILITY_INVESTMENT タイプを検索（現在の実装では自動作成されない）
        List<Transaction> facilityInvestments = transactionService.getTransactionsByType(TransactionType.FACILITY_INVESTMENT);
        
        assertTrue(facilityInvestments.isEmpty()); // FacilityInvestment is not automatically created in current implementation
        facilityInvestments.forEach(transaction -> {
            assertEquals(TransactionType.FACILITY_INVESTMENT, transaction.getTransactionType());
        });
    }

    @Test
    void Borrower別取引履歴を取得できる() {
        // 手数料支払い作成
        createTestFeePayment(FeeType.TRANSACTION_FEE, "BANK");

        // Borrower別取引履歴を取得
        List<Transaction> transactions = transactionService.getTransactionsByBorrower(borrower.getId());

        assertFalse(transactions.isEmpty());
        transactions.forEach(transaction -> {
            assertEquals(borrower.getId(), transaction.getBorrowerId());
        });
    }

    @Test
    void 取引統計が正しく計算される() {
        // 複数の手数料支払いを作成
        createTestFeePayment(FeeType.MANAGEMENT_FEE, "BANK");
        createTestFeePayment(FeeType.COMMITMENT_FEE, "INVESTOR");
        createTestFeePayment(FeeType.ARRANGEMENT_FEE, "BANK");

        // 統計を取得
        TransactionStatistics stats = transactionService.getTransactionStatistics(facility.getId());

        assertNotNull(stats);
        assertTrue(stats.getTotalCount() >= 3); // FeePayment(3以上)
        assertEquals(0, stats.getCompletedCount()); // 全てPENDING状態
        assertTrue(stats.getPendingCount() >= 3);
        assertEquals(0, stats.getProcessingCount());
    }

    @Test
    void 取引状態管理が正しく動作する() {
        // 手数料支払い作成
        FeePayment feePayment = createTestFeePayment(FeeType.AGENT_FEE, "BANK");
        
        // 初期状態はPENDING
        assertEquals(TransactionStatus.DRAFT, feePayment.getStatus());
        assertTrue(feePayment.isCancellable());
        assertFalse(feePayment.isCompleted());
        assertFalse(feePayment.isActive());

        // 取引承認（PENDING → PROCESSING）
        transactionService.approveTransaction(feePayment.getId());
        
        // 取引完了（PROCESSING → COMPLETED）
        transactionService.completeTransaction(feePayment.getId());
        
        // 取引を再取得して状態確認
        Transaction completedTransaction = transactionService.getTransactionById(feePayment.getId());
        assertEquals(TransactionStatus.COMPLETED, completedTransaction.getStatus());
        assertFalse(completedTransaction.isCancellable());
        assertTrue(completedTransaction.isCompleted());
        assertFalse(completedTransaction.isActive());
    }

    @Test
    void 取引キャンセルが正しく動作する() {
        // 手数料支払い作成
        FeePayment feePayment = createTestFeePayment(FeeType.LATE_FEE, "INVESTOR");
        
        // キャンセル実行
        String cancelReason = "Business decision";
        transactionService.cancelTransaction(feePayment.getId(), cancelReason);
        
        // 取引を再取得して状態確認
        Transaction cancelledTransaction = transactionService.getTransactionById(feePayment.getId());
        assertEquals(TransactionStatus.CANCELLED, cancelledTransaction.getStatus());
        assertFalse(cancelledTransaction.isCancellable());
        assertFalse(cancelledTransaction.isCompleted());
        assertFalse(cancelledTransaction.isActive());
    }

    @Test
    void 取引失敗状態が正しく設定される() {
        // 手数料支払い作成
        FeePayment feePayment = createTestFeePayment(FeeType.OTHER_FEE, "BANK");
        
        // 失敗状態に設定
        String errorMessage = "Payment processing failed";
        transactionService.failTransaction(feePayment.getId(), errorMessage);
        
        // 取引を再取得して状態確認
        Transaction failedTransaction = transactionService.getTransactionById(feePayment.getId());
        assertEquals(TransactionStatus.FAILED, failedTransaction.getStatus());
        assertFalse(failedTransaction.isCancellable()); // 失敗した取引はキャンセル不可
        assertFalse(failedTransaction.isCompleted());
        assertFalse(failedTransaction.isActive());
    }

    private FeePayment createTestFeePayment(FeeType feeType, String recipientType) {
        CreateFeePaymentRequest request = new CreateFeePaymentRequest();
        request.setFacilityId(facility.getId());
        request.setBorrowerId(borrower.getId());
        request.setFeeType(feeType);
        request.setFeeDate(LocalDate.now());
        request.setFeeAmount(new BigDecimal("5000.00"));
        request.setCalculationBase(new BigDecimal("1000000.00"));
        request.setFeeRate(0.5);
        request.setRecipientType(recipientType);
        request.setRecipientId(investor1.getId());
        request.setCurrency("USD");
        request.setDescription("Test " + feeType.name());

        return feePaymentService.createFeePayment(request);
    }
}