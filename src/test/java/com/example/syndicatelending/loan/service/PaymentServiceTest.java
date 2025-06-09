package com.example.syndicatelending.loan.service;

import com.example.syndicatelending.common.domain.model.Money;
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
import com.example.syndicatelending.common.domain.model.Percentage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private DrawdownService drawdownService;

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

    private Investor investor1;
    private Investor investor2;
    private Borrower borrower;
    private Facility facility;
    private Loan loan;

    @BeforeEach
    void setUp() {
        // 投資家作成
        investor1 = new Investor("Investor 1", "investor1@example.com", "111-1111-1111",
                "COMP001", new BigDecimal("500000"), InvestorType.BANK);
        investor1 = investorRepository.save(investor1);

        investor2 = new Investor("Investor 2", "investor2@example.com", "222-2222-2222",
                "COMP002", new BigDecimal("300000"), InvestorType.INSURANCE);
        investor2 = investorRepository.save(investor2);

        // 借り手作成
        borrower = new Borrower("Test Borrower", "borrower@example.com", "333-3333-3333", "COMP003",
                               Money.of(new BigDecimal("1000000")), 
                               com.example.syndicatelending.party.entity.CreditRating.A);
        borrower = borrowerRepository.save(borrower);

        // ファシリティ作成
        facility = new Facility();
        facility.setSyndicateId(1L);
        facility.setCommitment(Money.of(new BigDecimal("800000")));
        facility.setCurrency("JPY");
        facility.setStartDate(LocalDate.now());
        facility.setEndDate(LocalDate.now().plusYears(1));
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

        // ドローダウン実行
        CreateDrawdownRequest drawdownRequest = new CreateDrawdownRequest();
        drawdownRequest.setFacilityId(facility.getId());
        drawdownRequest.setBorrowerId(borrower.getId());
        drawdownRequest.setAmount(new BigDecimal("300000"));
        drawdownRequest.setCurrency("JPY");
        drawdownRequest.setDrawdownDate(LocalDate.now());
        drawdownRequest.setAnnualInterestRate(new BigDecimal("0.03"));
        drawdownRequest.setRepaymentPeriodMonths(12);
        drawdownRequest.setRepaymentCycle("MONTHLY");
        drawdownRequest.setRepaymentMethod(RepaymentMethod.EQUAL_INSTALLMENT);
        drawdownRequest.setPurpose("Test purpose");

        Drawdown drawdown = drawdownService.createDrawdown(drawdownRequest);
        loan = loanRepository.findById(drawdown.getLoanId()).orElseThrow();
    }

    @Test
    void 元本返済時に投資家の投資額が正しく減少する() {
        // ドローダウン後の投資額確認
        investor1 = investorRepository.findById(investor1.getId()).orElseThrow();
        investor2 = investorRepository.findById(investor2.getId()).orElseThrow();

        Money initialInvestor1Amount = investor1.getCurrentInvestmentAmount(); // 300000 * 0.6 = 180000
        Money initialInvestor2Amount = investor2.getCurrentInvestmentAmount(); // 300000 * 0.4 = 120000

        assertEquals(Money.of(new BigDecimal("180000")), initialInvestor1Amount);
        assertEquals(Money.of(new BigDecimal("120000")), initialInvestor2Amount);

        // 返済リクエスト作成
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setLoanId(loan.getId());
        paymentRequest.setPaymentDate(LocalDate.now());
        paymentRequest.setPrincipalAmount(new BigDecimal("60000")); // 元本返済
        paymentRequest.setInterestAmount(new BigDecimal("5000"));   // 利息支払い
        paymentRequest.setCurrency("JPY");

        // 返済実行
        Payment payment = paymentService.processPayment(paymentRequest);
        assertNotNull(payment);

        // 投資家エンティティを再取得
        investor1 = investorRepository.findById(investor1.getId()).orElseThrow();
        investor2 = investorRepository.findById(investor2.getId()).orElseThrow();

        // 投資額の確認（元本返済分が減少、60%:40%の比率で分配）
        Money expectedInvestor1Amount = Money.of(new BigDecimal("144000")); // 180000 - (60000 * 0.6)
        Money expectedInvestor2Amount = Money.of(new BigDecimal("96000"));  // 120000 - (60000 * 0.4)

        assertEquals(expectedInvestor1Amount, investor1.getCurrentInvestmentAmount());
        assertEquals(expectedInvestor2Amount, investor2.getCurrentInvestmentAmount());
    }

    @Test
    void 利息のみの支払いでは投資額が変更されない() {
        // ドローダウン後の投資額確認
        investor1 = investorRepository.findById(investor1.getId()).orElseThrow();
        investor2 = investorRepository.findById(investor2.getId()).orElseThrow();

        Money initialInvestor1Amount = investor1.getCurrentInvestmentAmount();
        Money initialInvestor2Amount = investor2.getCurrentInvestmentAmount();

        // 利息のみの返済リクエスト作成
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setLoanId(loan.getId());
        paymentRequest.setPaymentDate(LocalDate.now());
        paymentRequest.setPrincipalAmount(BigDecimal.ZERO); // 元本返済なし
        paymentRequest.setInterestAmount(new BigDecimal("5000")); // 利息のみ
        paymentRequest.setCurrency("JPY");

        // 返済実行
        Payment payment = paymentService.processPayment(paymentRequest);
        assertNotNull(payment);

        // 投資家エンティティを再取得
        investor1 = investorRepository.findById(investor1.getId()).orElseThrow();
        investor2 = investorRepository.findById(investor2.getId()).orElseThrow();

        // 投資額が変更されていないことを確認
        assertEquals(initialInvestor1Amount, investor1.getCurrentInvestmentAmount());
        assertEquals(initialInvestor2Amount, investor2.getCurrentInvestmentAmount());
    }

    @Test
    void 複数回の返済で投資額が累積的に減少する() {
        // ドローダウン後の初期投資額
        investor1 = investorRepository.findById(investor1.getId()).orElseThrow();
        investor2 = investorRepository.findById(investor2.getId()).orElseThrow();

        Money initialInvestor1Amount = investor1.getCurrentInvestmentAmount();
        Money initialInvestor2Amount = investor2.getCurrentInvestmentAmount();

        // 1回目の返済
        CreatePaymentRequest payment1 = new CreatePaymentRequest();
        payment1.setLoanId(loan.getId());
        payment1.setPaymentDate(LocalDate.now());
        payment1.setPrincipalAmount(new BigDecimal("30000"));
        payment1.setInterestAmount(new BigDecimal("3000"));
        payment1.setCurrency("JPY");

        paymentService.processPayment(payment1);

        // 2回目の返済
        CreatePaymentRequest payment2 = new CreatePaymentRequest();
        payment2.setLoanId(loan.getId());
        payment2.setPaymentDate(LocalDate.now().plusMonths(1));
        payment2.setPrincipalAmount(new BigDecimal("50000"));
        payment2.setInterestAmount(new BigDecimal("4000"));
        payment2.setCurrency("JPY");

        paymentService.processPayment(payment2);

        // 投資家エンティティを再取得
        investor1 = investorRepository.findById(investor1.getId()).orElseThrow();
        investor2 = investorRepository.findById(investor2.getId()).orElseThrow();

        // 累積的な減少を確認
        Money totalPrincipalReduction = Money.of(new BigDecimal("80000")); // 30000 + 50000
        Money expectedInvestor1Reduction = totalPrincipalReduction.multiply(new BigDecimal("0.6"));
        Money expectedInvestor2Reduction = totalPrincipalReduction.multiply(new BigDecimal("0.4"));

        Money expectedInvestor1Amount = initialInvestor1Amount.subtract(expectedInvestor1Reduction);
        Money expectedInvestor2Amount = initialInvestor2Amount.subtract(expectedInvestor2Reduction);

        assertEquals(expectedInvestor1Amount, investor1.getCurrentInvestmentAmount());
        assertEquals(expectedInvestor2Amount, investor2.getCurrentInvestmentAmount());
    }
}