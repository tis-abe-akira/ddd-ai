package com.example.syndicatelending.facility.service;

import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.facility.statemachine.FacilityState;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.facility.repository.SharePieRepository;
import com.example.syndicatelending.facility.repository.FacilityInvestmentRepository;
import com.example.syndicatelending.syndicate.repository.SyndicateRepository;
import com.example.syndicatelending.syndicate.entity.Syndicate;
import com.example.syndicatelending.party.entity.Investor;
import com.example.syndicatelending.party.repository.InvestorRepository;
import com.example.syndicatelending.party.entity.Borrower;
import com.example.syndicatelending.party.repository.BorrowerRepository;
import com.example.syndicatelending.facility.dto.UpdateFacilityRequest;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FacilityStateMachineTest {

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private SyndicateRepository syndicateRepository;

    @Autowired
    private SharePieRepository sharePieRepository;

    @Autowired
    private FacilityInvestmentRepository facilityInvestmentRepository;

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    private Syndicate testSyndicate;
    private Facility testFacility;
    private Investor testInvestor;
    private Borrower testBorrower;

    @BeforeEach
    void setUp() {
        // テスト用Borrowerを作成
        testBorrower = new Borrower();
        testBorrower.setName("Test Borrower");
        testBorrower.setCompanyId("1");
        testBorrower.setCreditRating(com.example.syndicatelending.party.entity.CreditRating.AAA);
        testBorrower.setCreditLimit(Money.of(new BigDecimal("50000000.00")));
        testBorrower.setCreatedAt(java.time.LocalDateTime.now());
        testBorrower.setUpdatedAt(java.time.LocalDateTime.now());
        testBorrower = borrowerRepository.save(testBorrower);

        // テスト用Investorを作成
        testInvestor = new Investor();
        testInvestor.setName("Test Investor");
        testInvestor.setCompanyId("1");
        testInvestor.setInvestmentCapacity(new BigDecimal("10000000.00"));
        testInvestor.setCurrentInvestmentAmount(Money.of(new BigDecimal("0.00")));
        testInvestor.setCreatedAt(java.time.LocalDateTime.now());
        testInvestor.setUpdatedAt(java.time.LocalDateTime.now());
        testInvestor.setIsActive(true);
        testInvestor = investorRepository.save(testInvestor);

        // テスト用Syndicateを作成
        testSyndicate = new Syndicate();
        testSyndicate.setName("Test Syndicate");
        testSyndicate.setBorrowerId(testBorrower.getId());
        testSyndicate.setLeadBankId(testInvestor.getId());
        List<Long> memberIds = new ArrayList<>();
        memberIds.add(testInvestor.getId());
        testSyndicate.setMemberInvestorIds(memberIds);
        testSyndicate = syndicateRepository.save(testSyndicate);

        // テスト用Facilityを作成
        testFacility = new Facility();
        testFacility.setSyndicateId(testSyndicate.getId());
        testFacility.setCommitment(Money.of(new BigDecimal("1000000.00")));
        testFacility.setCurrency("USD");
        testFacility.setStartDate(LocalDate.now());
        testFacility.setEndDate(LocalDate.now().plusYears(1));
        testFacility.setInterestTerms("5% annual");
        testFacility = facilityRepository.save(testFacility);
    }

    @Test
    void testFacilityInitialState() {
        // 作成直後はDRAFT状態であること
        assertEquals(FacilityState.DRAFT, testFacility.getStatus());
        assertTrue(testFacility.canBeModified());
        assertFalse(testFacility.isFixed());
    }

    @Test
    void testFacilityStateTransitionToFixed() {
        // DRAFT → FIXED への状態遷移をテスト
        assertEquals(FacilityState.DRAFT, testFacility.getStatus());

        // fixFacilityメソッドを実行
        facilityService.fixFacility(testFacility.getId());

        // 状態がFIXEDに変更されていることを確認
        Facility updatedFacility = facilityRepository.findById(testFacility.getId()).orElseThrow();
        assertEquals(FacilityState.FIXED, updatedFacility.getStatus());
        assertFalse(updatedFacility.canBeModified());
        assertTrue(updatedFacility.isFixed());
    }

    @Test
    void testFixFacilityFromNonDraftState() {
        // 既にFIXED状態のFacilityをFIXEDにしようとする場合は何もしない（エラーにならない）
        testFacility.setStatus(FacilityState.FIXED);
        facilityRepository.save(testFacility);

        // 例外がスローされないことを確認
        assertDoesNotThrow(() -> facilityService.fixFacility(testFacility.getId()));
        
        // 状態がFIXEDのまま変わらないことを確認
        Facility facility = facilityRepository.findById(testFacility.getId()).orElseThrow();
        assertEquals(FacilityState.FIXED, facility.getStatus());
    }

    @Test
    void testUpdateFacilityInDraftState() {
        // DRAFT状態では更新可能
        assertEquals(FacilityState.DRAFT, testFacility.getStatus());

        UpdateFacilityRequest request = createUpdateRequest();
        
        // 更新が成功することを確認
        assertDoesNotThrow(() -> facilityService.updateFacility(testFacility.getId(), request));
    }

    @Test
    void testUpdateFacilityInFixedState() {
        // FacilityをFIXED状態にする
        testFacility.setStatus(FacilityState.FIXED);
        facilityRepository.save(testFacility);

        UpdateFacilityRequest request = createUpdateRequest();

        // FIXED状態では更新できないことを確認
        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class,
            () -> facilityService.updateFacility(testFacility.getId(), request)
        );

        assertTrue(exception.getMessage().contains("FIXED状態のFacilityは変更できません"));
    }

    @Test
    void testCanBeModifiedMethod() {
        // DRAFT状態では変更可能
        testFacility.setStatus(FacilityState.DRAFT);
        assertTrue(testFacility.canBeModified());

        // FIXED状態では変更不可
        testFacility.setStatus(FacilityState.FIXED);
        assertFalse(testFacility.canBeModified());
    }

    @Test
    void testIsFixedMethod() {
        // DRAFT状態ではFixed = false
        testFacility.setStatus(FacilityState.DRAFT);
        assertFalse(testFacility.isFixed());

        // FIXED状態ではFixed = true
        testFacility.setStatus(FacilityState.FIXED);
        assertTrue(testFacility.isFixed());
    }

    private UpdateFacilityRequest createUpdateRequest() {
        UpdateFacilityRequest request = new UpdateFacilityRequest();
        request.setVersion(testFacility.getVersion());
        request.setSyndicateId(testFacility.getSyndicateId());
        request.setCommitment(Money.of(new BigDecimal("2000000.00")));
        request.setCurrency("USD");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusYears(2));
        request.setInterestTerms("6% annual");

        // SharePieの設定
        List<UpdateFacilityRequest.SharePieRequest> sharePies = new ArrayList<>();
        UpdateFacilityRequest.SharePieRequest sharePie = new UpdateFacilityRequest.SharePieRequest();
        sharePie.setInvestorId(testInvestor.getId());
        sharePie.setShare(Percentage.of(new BigDecimal("1.00"))); // 1.00 = 100%
        sharePies.add(sharePie);
        request.setSharePies(sharePies);

        return request;
    }
}