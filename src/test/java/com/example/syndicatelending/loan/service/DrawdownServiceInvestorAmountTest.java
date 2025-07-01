package com.example.syndicatelending.loan.service;

import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.entity.SharePie;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.facility.repository.SharePieRepository;
import com.example.syndicatelending.loan.dto.CreateDrawdownRequest;
import com.example.syndicatelending.loan.entity.Drawdown;
import com.example.syndicatelending.loan.entity.RepaymentMethod;
import com.example.syndicatelending.loan.repository.DrawdownRepository;
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
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DrawdownServiceInvestorAmountTest {

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

    private Investor investor1;
    private Investor investor2;
    private Borrower borrower;
    private Facility facility;

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

        // SharePie作成（70%:30%の比率）
        SharePie sharePie1 = new SharePie();
        sharePie1.setFacility(facility);
        sharePie1.setInvestorId(investor1.getId());
        sharePie1.setShare(Percentage.of(new BigDecimal("0.7")));
        sharePieRepository.save(sharePie1);

        SharePie sharePie2 = new SharePie();
        sharePie2.setFacility(facility);
        sharePie2.setInvestorId(investor2.getId());
        sharePie2.setShare(Percentage.of(new BigDecimal("0.3")));
        sharePieRepository.save(sharePie2);
    }

    @Test
    void ドローダウン実行時に投資家の投資額が正しく増加する() {
        // 初期状態の確認
        assertEquals(Money.zero(), investor1.getCurrentInvestmentAmount());
        assertEquals(Money.zero(), investor2.getCurrentInvestmentAmount());

        // ドローダウンリクエスト作成
        CreateDrawdownRequest request = new CreateDrawdownRequest();
        request.setFacilityId(facility.getId());
        request.setBorrowerId(borrower.getId());
        request.setAmount(new BigDecimal("200000"));
        request.setCurrency("JPY");
        request.setDrawdownDate(LocalDate.now());
        request.setAnnualInterestRate(new BigDecimal("0.03"));
        request.setRepaymentPeriodMonths(12);
        request.setRepaymentCycle("MONTHLY");
        request.setRepaymentMethod(RepaymentMethod.EQUAL_INSTALLMENT);
        request.setPurpose("Test purpose");

        // ドローダウン実行
        Drawdown drawdown = drawdownService.createDrawdown(request);
        assertNotNull(drawdown);

        // 投資家エンティティを再取得
        investor1 = investorRepository.findById(investor1.getId()).orElseThrow();
        investor2 = investorRepository.findById(investor2.getId()).orElseThrow();

        // 投資額の確認（70%:30%の比率で分配）
        Money expectedInvestor1Amount = Money.of(new BigDecimal("140000")); // 200000 * 0.7
        Money expectedInvestor2Amount = Money.of(new BigDecimal("60000"));  // 200000 * 0.3

        assertEquals(expectedInvestor1Amount, investor1.getCurrentInvestmentAmount());
        assertEquals(expectedInvestor2Amount, investor2.getCurrentInvestmentAmount());
    }

    @Test
    void 複数回のドローダウンで投資額が累積される() {
        // 1回目のドローダウン（最初のFacility）
        CreateDrawdownRequest request1 = createDrawdownRequest(new BigDecimal("100000"));
        drawdownService.createDrawdown(request1);

        // 2回目のドローダウン用の新しいFacilityを作成
        Facility facility2 = new Facility();
        facility2.setSyndicateId(1L);
        facility2.setCommitment(Money.of(new BigDecimal("800000")));
        facility2.setCurrency("JPY");
        facility2.setStartDate(LocalDate.now());
        facility2.setEndDate(LocalDate.now().plusYears(1));
        facility2 = facilityRepository.save(facility2);
        
        // 新しいFacilityにSharePieを設定（同じ比率：70%:30%）
        SharePie sharePie1_2 = new SharePie();
        sharePie1_2.setFacility(facility2);
        sharePie1_2.setInvestorId(investor1.getId());
        sharePie1_2.setShare(Percentage.of(new BigDecimal("0.7")));
        sharePieRepository.save(sharePie1_2);

        SharePie sharePie2_2 = new SharePie();
        sharePie2_2.setFacility(facility2);
        sharePie2_2.setInvestorId(investor2.getId());
        sharePie2_2.setShare(Percentage.of(new BigDecimal("0.3")));
        sharePieRepository.save(sharePie2_2);
        
        // 2回目のドローダウン（新しいFacility）
        CreateDrawdownRequest request2 = createDrawdownRequest(new BigDecimal("150000"));
        request2.setFacilityId(facility2.getId());
        drawdownService.createDrawdown(request2);

        // 投資家エンティティを再取得
        investor1 = investorRepository.findById(investor1.getId()).orElseThrow();
        investor2 = investorRepository.findById(investor2.getId()).orElseThrow();

        // 累積投資額の確認
        Money expectedInvestor1Total = Money.of(new BigDecimal("175000")); // (100000 + 150000) * 0.7
        Money expectedInvestor2Total = Money.of(new BigDecimal("75000"));  // (100000 + 150000) * 0.3

        assertEquals(expectedInvestor1Total, investor1.getCurrentInvestmentAmount());
        assertEquals(expectedInvestor2Total, investor2.getCurrentInvestmentAmount());
    }

    private CreateDrawdownRequest createDrawdownRequest(BigDecimal amount) {
        CreateDrawdownRequest request = new CreateDrawdownRequest();
        request.setFacilityId(facility.getId());
        request.setBorrowerId(borrower.getId());
        request.setAmount(amount);
        request.setCurrency("JPY");
        request.setDrawdownDate(LocalDate.now());
        request.setAnnualInterestRate(new BigDecimal("0.03"));
        request.setRepaymentPeriodMonths(12);
        request.setRepaymentCycle("MONTHLY");
        request.setRepaymentMethod(RepaymentMethod.EQUAL_INSTALLMENT);
        request.setPurpose("Test purpose");
        return request;
    }
}