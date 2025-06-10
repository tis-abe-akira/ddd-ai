package com.example.syndicatelending.loan.service;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.loan.dto.CreateDrawdownRequest;
import com.example.syndicatelending.loan.entity.Drawdown;
import com.example.syndicatelending.loan.entity.Loan;
import com.example.syndicatelending.loan.entity.PaymentDetail;
import com.example.syndicatelending.loan.entity.RepaymentMethod;
import com.example.syndicatelending.loan.repository.DrawdownRepository;
import com.example.syndicatelending.loan.repository.LoanRepository;
import com.example.syndicatelending.loan.repository.PaymentDetailRepository;
import com.example.syndicatelending.party.entity.Borrower;
import com.example.syndicatelending.party.entity.Company;
import com.example.syndicatelending.party.entity.Country;
import com.example.syndicatelending.party.entity.CreditRating;
import com.example.syndicatelending.party.entity.Industry;
import com.example.syndicatelending.party.repository.BorrowerRepository;
import com.example.syndicatelending.party.repository.CompanyRepository;
import com.example.syndicatelending.syndicate.entity.Syndicate;
import com.example.syndicatelending.syndicate.repository.SyndicateRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DrawdownServiceの統合テスト
 * ドローダウンから支払いスケジュール生成までの完全なフローをテストする
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DrawdownServiceIntegrationTest {

    @Autowired
    private DrawdownService drawdownService;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private PaymentDetailRepository paymentDetailRepository;

    @Autowired
    private DrawdownRepository drawdownRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private SyndicateRepository syndicateRepository;

    @Test
    void testCreateDrawdownWithEqualInstallmentPaymentSchedule() {
        // テストデータ準備
        TestDataHelper helper = new TestDataHelper();
        Long facilityId = helper.createTestFacilityWithBorrower();

        // ドローダウンリクエスト作成（等分割返済）
        CreateDrawdownRequest request = new CreateDrawdownRequest();
        request.setFacilityId(facilityId);
        request.setBorrowerId(helper.borrowerId);
        request.setAmount(new BigDecimal("1000000")); // 100万円
        request.setCurrency("JPY");
        request.setPurpose("Equipment purchase");
        request.setAnnualInterestRate(new BigDecimal("0.05")); // 5%
        request.setDrawdownDate(LocalDate.now());
        request.setRepaymentPeriodMonths(12); // 12ヶ月
        request.setRepaymentCycle("MONTHLY");
        request.setRepaymentMethod(RepaymentMethod.EQUAL_INSTALLMENT);

        // ドローダウン実行
        Drawdown drawdown = drawdownService.createDrawdown(request);

        // 結果検証
        assertNotNull(drawdown);
        assertNotNull(drawdown.getId());
        assertNotNull(drawdown.getLoanId());

        // Loanエンティティが正しく作成されていることを確認
        Loan loan = loanRepository.findById(drawdown.getLoanId()).orElse(null);
        assertNotNull(loan);
        assertEquals(Money.of(new BigDecimal("1000000")), loan.getPrincipalAmount());
        assertEquals(RepaymentMethod.EQUAL_INSTALLMENT, loan.getRepaymentMethod());

        // PaymentDetailエンティティが正しく生成されていることを確認
        List<PaymentDetail> paymentDetails = paymentDetailRepository.findByLoanIdOrderByPaymentNumber(loan.getId());
        assertEquals(12, paymentDetails.size()); // 12ヶ月分

        // 最初の支払い詳細を検証
        PaymentDetail firstPayment = paymentDetails.get(0);
        assertEquals(1, firstPayment.getPaymentNumber());
        assertTrue(firstPayment.getPrincipalPayment().isGreaterThan(Money.zero()));
        assertTrue(firstPayment.getInterestPayment().isGreaterThan(Money.zero()));
        assertEquals(LocalDate.now().plusMonths(1), firstPayment.getDueDate());

        // 最後の支払い詳細を検証（残高が0になっていること）
        PaymentDetail lastPayment = paymentDetails.get(11);
        assertEquals(12, lastPayment.getPaymentNumber());
        assertEquals(Money.zero(), lastPayment.getRemainingBalance());

        // 元本合計が正しいことを確認
        Money totalPrincipal = paymentDetails.stream()
                .map(PaymentDetail::getPrincipalPayment)
                .reduce(Money.zero(), Money::add);
        assertEquals(loan.getPrincipalAmount(), totalPrincipal);
    }

    @Test
    void testCreateDrawdownWithBulletPaymentSchedule() {
        // テストデータ準備
        TestDataHelper helper = new TestDataHelper();
        Long facilityId = helper.createTestFacilityWithBorrower();

        // ドローダウンリクエスト作成（一括返済）
        CreateDrawdownRequest request = new CreateDrawdownRequest();
        request.setFacilityId(facilityId);
        request.setBorrowerId(helper.borrowerId);
        request.setAmount(new BigDecimal("1000000")); // 100万円
        request.setCurrency("JPY");
        request.setPurpose("Working capital");
        request.setAnnualInterestRate(new BigDecimal("0.03")); // 3%
        request.setDrawdownDate(LocalDate.now());
        request.setRepaymentPeriodMonths(6); // 6ヶ月
        request.setRepaymentCycle("MONTHLY");
        request.setRepaymentMethod(RepaymentMethod.BULLET_PAYMENT);

        // ドローダウン実行
        Drawdown drawdown = drawdownService.createDrawdown(request);

        // 結果検証
        assertNotNull(drawdown);
        Loan loan = loanRepository.findById(drawdown.getLoanId()).orElse(null);
        assertNotNull(loan);

        // PaymentDetailエンティティが正しく生成されていることを確認
        List<PaymentDetail> paymentDetails = paymentDetailRepository.findByLoanIdOrderByPaymentNumber(loan.getId());
        assertEquals(6, paymentDetails.size()); // 6ヶ月分

        // 中間回の支払い詳細を検証（利息のみ）
        PaymentDetail middlePayment = paymentDetails.get(2);
        assertEquals(Money.zero(), middlePayment.getPrincipalPayment());
        assertTrue(middlePayment.getInterestPayment().isGreaterThan(Money.zero()));
        assertEquals(loan.getPrincipalAmount(), middlePayment.getRemainingBalance());

        // 最終回の支払い詳細を検証（元本+利息）
        PaymentDetail lastPayment = paymentDetails.get(5);
        assertEquals(loan.getPrincipalAmount(), lastPayment.getPrincipalPayment());
        assertTrue(lastPayment.getInterestPayment().isGreaterThan(Money.zero()));
        assertEquals(Money.zero(), lastPayment.getRemainingBalance());
    }

    @Test
    void testAnnualInterestRatePrecisionLoss() {
        // テストデータ準備
        TestDataHelper helper = new TestDataHelper();
        Long facilityId = helper.createTestFacilityWithBorrower();

        // 精度損失が発生する可能性のある利率でドローダウンリクエスト作成
        CreateDrawdownRequest request = new CreateDrawdownRequest();
        request.setFacilityId(facilityId);
        request.setBorrowerId(helper.borrowerId);
        request.setAmount(new BigDecimal("1000000")); // 100万円
        request.setCurrency("JPY");
        request.setPurpose("Test precision loss");
        request.setAnnualInterestRate(new BigDecimal("0.024")); // 2.4% - 問題の利率
        request.setDrawdownDate(LocalDate.now());
        request.setRepaymentPeriodMonths(12);
        request.setRepaymentCycle("MONTHLY");
        request.setRepaymentMethod(RepaymentMethod.EQUAL_INSTALLMENT);

        // ドローダウン実行
        Drawdown drawdown = drawdownService.createDrawdown(request);

        // 結果検証
        assertNotNull(drawdown);
        Loan loan = loanRepository.findById(drawdown.getLoanId()).orElse(null);
        assertNotNull(loan);

        // 年利率が正確に保存されているかを確認
        BigDecimal expectedRate = new BigDecimal("0.024");
        BigDecimal actualRate = loan.getAnnualInterestRate().getValue();

        System.out.println("Expected rate: " + expectedRate);
        System.out.println("Actual rate: " + actualRate);
        System.out.println("Expected scale: " + expectedRate.scale());
        System.out.println("Actual scale: " + actualRate.scale());

        // 精度を確認
        assertEquals(expectedRate.setScale(4, RoundingMode.HALF_UP),
                actualRate.setScale(4, RoundingMode.HALF_UP),
                "Annual interest rate should be preserved with proper precision");

        // より厳密な検証：実際の値が期待値と一致するか
        assertTrue(expectedRate.compareTo(actualRate) == 0,
                "Annual interest rate should be exactly " + expectedRate + " but was " + actualRate);
    }

    @Test
    void testVariousDecimalPrecisionValues() {
        // テストデータ準備
        TestDataHelper helper = new TestDataHelper();
        Long facilityId = helper.createTestFacilityWithBorrower();

        // 様々な精度の利率をテスト
        BigDecimal[] testRates = {
                new BigDecimal("0.024"), // 2.4% - 報告された問題の値
                new BigDecimal("0.0245"), // 2.45% - 小数点以下4桁
                new BigDecimal("0.02456"), // 2.456% - 小数点以下5桁（4桁に丸められるはず）
                new BigDecimal("0.1234"), // 12.34% - 小数点以下4桁
                new BigDecimal("0.00001") // 0.001% - 非常に小さい値
        };

        for (int i = 0; i < testRates.length; i++) {
            BigDecimal testRate = testRates[i];

            CreateDrawdownRequest request = new CreateDrawdownRequest();
            request.setFacilityId(facilityId);
            request.setBorrowerId(helper.borrowerId);
            request.setAmount(new BigDecimal("1000000"));
            request.setCurrency("JPY");
            request.setPurpose("Test precision for rate " + testRate);
            request.setAnnualInterestRate(testRate);
            request.setDrawdownDate(LocalDate.now());
            request.setRepaymentPeriodMonths(12);
            request.setRepaymentCycle("MONTHLY");
            request.setRepaymentMethod(RepaymentMethod.EQUAL_INSTALLMENT);

            // ドローダウン実行
            Drawdown drawdown = drawdownService.createDrawdown(request);
            Loan loan = loanRepository.findById(drawdown.getLoanId()).orElse(null);
            assertNotNull(loan, "Loan should be created for test rate " + testRate);

            BigDecimal actualRate = loan.getAnnualInterestRate().getValue();
            BigDecimal expectedRate = testRate.setScale(4, RoundingMode.HALF_UP);

            System.out.println("Test " + (i + 1) + ":");
            System.out.println("  Input rate: " + testRate + " (scale: " + testRate.scale() + ")");
            System.out.println("  Expected rate: " + expectedRate + " (scale: " + expectedRate.scale() + ")");
            System.out.println("  Actual rate: " + actualRate + " (scale: " + actualRate.scale() + ")");
            System.out.println();

            // スケール4での比較
            assertEquals(expectedRate, actualRate.setScale(4, RoundingMode.HALF_UP),
                    "Rate " + testRate + " should be preserved with 4 decimal places");
        }
    }

    /**
     * テストデータ作成ヘルパークラス
     */
    private class TestDataHelper {
        private Long facilityId;
        private Long borrowerId;

        public Long createTestFacilityWithBorrower() {
            // 企業作成
            Company company = new Company();
            company.setCompanyName("Test Company");
            company.setRegistrationNumber("123456789");
            company.setIndustry(Industry.MANUFACTURING);
            company.setCountry(Country.JAPAN);
            company.setAddress("Tokyo, Japan");
            company = companyRepository.save(company);

            // 借手作成
            Borrower borrower = new Borrower();
            borrower.setName("Test Borrower");
            borrower.setEmail("test@example.com");
            borrower.setPhoneNumber("03-1234-5678");
            borrower.setCompanyId(company.getId().toString());
            borrower.setCreditLimit(Money.of(new BigDecimal("10000000")));
            borrower.setCreditRating(CreditRating.A);
            borrower = borrowerRepository.save(borrower);
            this.borrowerId = borrower.getId();

            // シンジケート作成
            Syndicate syndicate = new Syndicate();
            syndicate.setName("Test Syndicate");
            syndicate.setLeadBankId(1L);
            syndicate.setBorrowerId(borrower.getId());
            syndicate.setMemberInvestorIds(Arrays.asList(1L, 2L));
            syndicate = syndicateRepository.save(syndicate);

            // ファシリティ作成
            Facility facility = new Facility();
            facility.setSyndicateId(syndicate.getId());
            facility.setCommitment(Money.of(new BigDecimal("10000000")));
            facility.setCurrency("JPY");
            facility.setStartDate(LocalDate.now());
            facility.setEndDate(LocalDate.now().plusYears(1));
            facility.setInterestTerms("LIBOR + 200bp");
            facility = facilityRepository.save(facility);
            this.facilityId = facility.getId();

            return facilityId;
        }
    }
}
