package com.example.syndicatelending.loan.service;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import com.example.syndicatelending.common.statemachine.loan.LoanState;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.entity.SharePie;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.facility.repository.SharePieRepository;
import com.example.syndicatelending.loan.dto.CreateDrawdownRequest;
import com.example.syndicatelending.loan.dto.CreatePaymentRequest;
import com.example.syndicatelending.loan.entity.Drawdown;
import com.example.syndicatelending.loan.entity.Loan;
import com.example.syndicatelending.loan.entity.Payment;
import com.example.syndicatelending.loan.entity.RepaymentMethod;
import com.example.syndicatelending.loan.repository.LoanRepository;
import com.example.syndicatelending.party.entity.Borrower;
import com.example.syndicatelending.party.entity.Investor;
import com.example.syndicatelending.party.entity.InvestorType;
import com.example.syndicatelending.party.repository.BorrowerRepository;
import com.example.syndicatelending.party.repository.InvestorRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Loan・Payment統合テスト
 * ドローダウン→返済→状態変更の一連の流れを検証
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LoanPaymentIntegrationTest {

    @Autowired
    private DrawdownService drawdownService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private SharePieRepository sharePieRepository;

    @Autowired
    private LoanRepository loanRepository;

    private Facility facility;
    private Investor investor1;
    private Investor investor2;
    private Borrower borrower;

    @BeforeEach
    void setUp() {
        // 投資家1の作成
        investor1 = new Investor("Test Investor 1", "investor1@test.com", "111-1111-1111",
                "COMP001", new BigDecimal("1000000"), InvestorType.BANK);
        investor1 = investorRepository.save(investor1);

        // 投資家2の作成
        investor2 = new Investor("Test Investor 2", "investor2@test.com", "222-2222-2222",
                "COMP002", new BigDecimal("1000000"), InvestorType.FUND);
        investor2 = investorRepository.save(investor2);

        // 借り手の作成
        borrower = new Borrower("Test Borrower", "borrower@test.com", "333-3333-3333", "COMP003",
                               Money.of(new BigDecimal("2000000")), 
                               com.example.syndicatelending.party.entity.CreditRating.A);
        borrower = borrowerRepository.save(borrower);

        // ファシリティの作成
        facility = new Facility();
        facility.setSyndicateId(1L);
        facility.setCommitment(Money.of(new BigDecimal("1000000")));
        facility.setCurrency("JPY");
        facility.setStartDate(LocalDate.now());
        facility.setEndDate(LocalDate.now().plusYears(1));
        facility = facilityRepository.save(facility);

        // SharePie1の作成（60%持分）
        SharePie sharePie1 = new SharePie();
        sharePie1.setFacility(facility);
        sharePie1.setInvestorId(investor1.getId());
        sharePie1.setShare(Percentage.of(new BigDecimal("0.6")));
        sharePieRepository.save(sharePie1);

        // SharePie2の作成（40%持分）
        SharePie sharePie2 = new SharePie();
        sharePie2.setFacility(facility);
        sharePie2.setInvestorId(investor2.getId());
        sharePie2.setShare(Percentage.of(new BigDecimal("0.4")));
        sharePieRepository.save(sharePie2);
    }

    @Test
    void ドローダウンから初回返済までの完全な流れを検証する() {
        // 1. ドローダウン実行
        CreateDrawdownRequest drawdownRequest = createDrawdownRequest(new BigDecimal("500000"));
        Drawdown drawdown = drawdownService.createDrawdown(drawdownRequest);
        
        assertNotNull(drawdown);
        assertNotNull(drawdown.getLoanId());

        // 2. Loan状態確認（DRAFT状態）
        Loan loan = loanRepository.findById(drawdown.getLoanId()).orElseThrow();
        assertEquals(LoanState.DRAFT, loan.getStatus());
        assertEquals(Money.of(new BigDecimal("500000")), loan.getPrincipalAmount());
        assertEquals(Money.of(new BigDecimal("500000")), loan.getOutstandingBalance());

        // 3. 投資家の投資額確認（ドローダウン後）
        investor1 = investorRepository.findById(investor1.getId()).orElseThrow();
        investor2 = investorRepository.findById(investor2.getId()).orElseThrow();
        
        assertEquals(Money.of(new BigDecimal("300000")), investor1.getCurrentInvestmentAmount()); // 500000 * 0.6
        assertEquals(Money.of(new BigDecimal("200000")), investor2.getCurrentInvestmentAmount()); // 500000 * 0.4

        // 4. 初回返済実行
        CreatePaymentRequest paymentRequest = createPaymentRequest(
            loan.getId(), 
            new BigDecimal("100000"), // 元本返済
            new BigDecimal("10000")   // 利息支払い
        );
        Payment payment = paymentService.processPayment(paymentRequest);
        
        assertNotNull(payment);
        assertEquals(Money.of(new BigDecimal("110000")), payment.getTotalAmount());

        // 5. Loan状態確認（ACTIVE状態に変更）
        loan = loanRepository.findById(loan.getId()).orElseThrow();
        assertEquals(LoanState.ACTIVE, loan.getStatus());

        // 6. 投資家の投資額確認（返済後）
        investor1 = investorRepository.findById(investor1.getId()).orElseThrow();
        investor2 = investorRepository.findById(investor2.getId()).orElseThrow();
        
        // 元本返済100,000は投資家の持分比率で減額される
        assertEquals(Money.of(new BigDecimal("240000")), investor1.getCurrentInvestmentAmount()); // 300000 - (100000 * 0.6)
        assertEquals(Money.of(new BigDecimal("160000")), investor2.getCurrentInvestmentAmount()); // 200000 - (100000 * 0.4)
    }

    @Test
    void 複数回の返済で状態とバランスが正しく管理される() {
        // 1. ドローダウン
        CreateDrawdownRequest drawdownRequest = createDrawdownRequest(new BigDecimal("300000"));
        Drawdown drawdown = drawdownService.createDrawdown(drawdownRequest);
        Loan loan = loanRepository.findById(drawdown.getLoanId()).orElseThrow();

        // 2. 初回返済（DRAFT → ACTIVE）
        CreatePaymentRequest payment1 = createPaymentRequest(loan.getId(), new BigDecimal("50000"), new BigDecimal("5000"));
        paymentService.processPayment(payment1);
        
        loan = loanRepository.findById(loan.getId()).orElseThrow();
        assertEquals(LoanState.ACTIVE, loan.getStatus());

        // 3. 2回目返済（ACTIVE維持）
        CreatePaymentRequest payment2 = createPaymentRequest(loan.getId(), new BigDecimal("50000"), new BigDecimal("4000"));
        paymentService.processPayment(payment2);
        
        loan = loanRepository.findById(loan.getId()).orElseThrow();
        assertEquals(LoanState.ACTIVE, loan.getStatus());

        // 4. 投資家の最終投資額確認
        investor1 = investorRepository.findById(investor1.getId()).orElseThrow();
        investor2 = investorRepository.findById(investor2.getId()).orElseThrow();
        
        // 初期: 300000 * 0.6 = 180000, 300000 * 0.4 = 120000
        // 1回目返済: 50000 * 0.6 = 30000減少, 50000 * 0.4 = 20000減少
        // 2回目返済: 50000 * 0.6 = 30000減少, 50000 * 0.4 = 20000減少
        assertEquals(Money.of(new BigDecimal("120000")), investor1.getCurrentInvestmentAmount()); // 180000 - 30000 - 30000
        assertEquals(Money.of(new BigDecimal("80000")), investor2.getCurrentInvestmentAmount());  // 120000 - 20000 - 20000
    }

    @Test
    void 利息のみ返済でも状態変更される() {
        // 1. ドローダウン
        CreateDrawdownRequest drawdownRequest = createDrawdownRequest(new BigDecimal("200000"));
        Drawdown drawdown = drawdownService.createDrawdown(drawdownRequest);
        Loan loan = loanRepository.findById(drawdown.getLoanId()).orElseThrow();
        
        assertEquals(LoanState.DRAFT, loan.getStatus());

        // 2. 利息のみ返済（元本0）
        CreatePaymentRequest paymentRequest = createPaymentRequest(loan.getId(), BigDecimal.ZERO, new BigDecimal("3000"));
        Payment payment = paymentService.processPayment(paymentRequest);
        
        assertNotNull(payment);
        assertEquals(Money.of(new BigDecimal("3000")), payment.getTotalAmount());

        // 3. Loan状態確認（ACTIVE状態に変更）
        loan = loanRepository.findById(loan.getId()).orElseThrow();
        assertEquals(LoanState.ACTIVE, loan.getStatus());

        // 4. 投資家の投資額確認（利息支払いでは変更されない）
        investor1 = investorRepository.findById(investor1.getId()).orElseThrow();
        investor2 = investorRepository.findById(investor2.getId()).orElseThrow();
        
        assertEquals(Money.of(new BigDecimal("120000")), investor1.getCurrentInvestmentAmount()); // 200000 * 0.6
        assertEquals(Money.of(new BigDecimal("80000")), investor2.getCurrentInvestmentAmount());  // 200000 * 0.4
    }

    private CreateDrawdownRequest createDrawdownRequest(BigDecimal amount) {
        CreateDrawdownRequest request = new CreateDrawdownRequest();
        request.setFacilityId(facility.getId());
        request.setBorrowerId(borrower.getId());
        request.setAmount(amount);
        request.setCurrency("JPY");
        request.setDrawdownDate(LocalDate.now());
        request.setAnnualInterestRate(new BigDecimal("0.05"));
        request.setRepaymentPeriodMonths(12);
        request.setRepaymentCycle("MONTHLY");
        request.setRepaymentMethod(RepaymentMethod.EQUAL_INSTALLMENT);
        request.setPurpose("Integration test purpose");
        return request;
    }

    private CreatePaymentRequest createPaymentRequest(Long loanId, BigDecimal principalAmount, BigDecimal interestAmount) {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setLoanId(loanId);
        request.setPaymentDate(LocalDate.now());
        request.setPrincipalAmount(principalAmount);
        request.setInterestAmount(interestAmount);
        request.setCurrency("JPY");
        return request;
    }
}