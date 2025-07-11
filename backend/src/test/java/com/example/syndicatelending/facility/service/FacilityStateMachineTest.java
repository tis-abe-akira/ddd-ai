package com.example.syndicatelending.facility.service;

import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import com.example.syndicatelending.common.statemachine.facility.FacilityState;
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
import com.example.syndicatelending.loan.repository.DrawdownRepository;
import com.example.syndicatelending.common.statemachine.party.InvestorState;
import com.example.syndicatelending.facility.dto.UpdateFacilityRequest;
import com.example.syndicatelending.common.domain.model.Money;
import com.example.syndicatelending.common.domain.model.Percentage;
import com.example.syndicatelending.common.statemachine.facility.FacilityEvent;
import org.springframework.statemachine.StateMachine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
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

    @Autowired
    private StateMachine<FacilityState, FacilityEvent> stateMachine;

    @Autowired
    private DrawdownRepository drawdownRepository;

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
        testInvestor.setStatus(InvestorState.ACTIVE);
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
        
        // StateMachineを各テスト前にリセット
        resetStateMachine();
    }
    
    @AfterEach
    void tearDown() {
        // StateMachineを各テスト後に停止
        try {
            stateMachine.stop();
        } catch (Exception e) {
            // StateMachineが既に停止している場合は無視
        }
    }
    
    private void resetStateMachine() {
        try {
            stateMachine.stop();
        } catch (Exception e) {
            // StateMachineが既に停止している場合は無視
        }
        // StateMachineを完全にリセット
        stateMachine.getStateMachineAccessor().doWithAllRegions(access -> {
            access.resetStateMachine(null);
        });
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
        assertEquals(FacilityState.ACTIVE, updatedFacility.getStatus());
        assertFalse(updatedFacility.canBeModified());
        assertTrue(updatedFacility.isFixed());
    }

    @Test
    void testFixFacilityFromFixedStateThrowsException() {
        // 既にFIXED状態のFacilityをFIXEDにしようとする場合は例外が発生する
        testFacility.setStatus(FacilityState.ACTIVE);
        facilityRepository.save(testFacility);

        // BusinessRuleViolationExceptionがスローされることを確認
        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class,
            () -> facilityService.fixFacility(testFacility.getId())
        );
        
        assertTrue(exception.getMessage().contains("FIXED状態のFacilityに対して2度目のドローダウンはできません"));
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
        testFacility.setStatus(FacilityState.ACTIVE);
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
        testFacility.setStatus(FacilityState.ACTIVE);
        assertFalse(testFacility.canBeModified());
    }

    @Test
    void testIsFixedMethod() {
        // DRAFT状態ではFixed = false
        testFacility.setStatus(FacilityState.DRAFT);
        assertFalse(testFacility.isFixed());

        // FIXED状態ではFixed = true
        testFacility.setStatus(FacilityState.ACTIVE);
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

    @Test
    void testStateMachineConfigurationExists() {
        // StateMachineが適切に設定されていることを確認
        assertNotNull(stateMachine);
        // 設定の存在を確認（詳細な状態テストは統合テストで行う）
    }

    @Test
    void testMultipleDrawdownPrevention() {
        // 1度目のfixFacility（ドローダウン）は成功
        assertEquals(FacilityState.DRAFT, testFacility.getStatus());
        
        assertDoesNotThrow(() -> facilityService.fixFacility(testFacility.getId()));
        
        Facility updatedFacility = facilityRepository.findById(testFacility.getId()).orElseThrow();
        assertEquals(FacilityState.ACTIVE, updatedFacility.getStatus());
        
        // 2度目のfixFacility（ドローダウン）は例外
        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class,
            () -> facilityService.fixFacility(testFacility.getId())
        );
        
        assertTrue(exception.getMessage().contains("FIXED状態のFacilityに対して2度目のドローダウンはできません"));
    }

    @Test
    void testBusinessRuleIntegration() {
        // ビジネスルールとStateMachineの統合テスト
        
        // 初期状態: DRAFT - 変更可能
        assertTrue(testFacility.canBeModified());
        assertFalse(testFacility.isFixed());
        
        // ドローダウン実行により状態変更
        facilityService.fixFacility(testFacility.getId());
        
        // 更新後: FIXED - 変更不可
        Facility facility = facilityRepository.findById(testFacility.getId()).orElseThrow();
        assertFalse(facility.canBeModified());
        assertTrue(facility.isFixed());
        
        // FIXED状態での更新はBusinessRuleViolationException
        UpdateFacilityRequest request = createUpdateRequest();
        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class,
            () -> facilityService.updateFacility(testFacility.getId(), request)
        );
        
        assertTrue(exception.getMessage().contains("FIXED状態のFacilityは変更できません"));
    }

    @Test
    void testRevertToDraftWithoutDrawdowns() {
        // 修正されたrevertToDraft機能のテスト: Drawdownが存在しない場合
        
        // 最初にFacilityをFIXED状態にする
        facilityService.fixFacility(testFacility.getId());
        Facility fixedFacility = facilityRepository.findById(testFacility.getId()).orElseThrow();
        assertEquals(FacilityState.ACTIVE, fixedFacility.getStatus());
        
        // Drawdownが存在しないことを確認
        assertTrue(drawdownRepository.findByFacilityId(testFacility.getId()).isEmpty());
        
        // revertToDraftが成功することを確認
        assertDoesNotThrow(() -> facilityService.revertToDraft(testFacility.getId()));
        
        // DRAFT状態に戻ったことを確認
        Facility revertedFacility = facilityRepository.findById(testFacility.getId()).orElseThrow();
        assertEquals(FacilityState.DRAFT, revertedFacility.getStatus());
        assertTrue(revertedFacility.canBeModified());
    }

    @Test
    void testRevertToDraftFromDraftStateDoesNothing() {
        // DRAFT状態からのrevertToDraftは何もしない
        assertEquals(FacilityState.DRAFT, testFacility.getStatus());
        
        // revertToDraftを実行
        assertDoesNotThrow(() -> facilityService.revertToDraft(testFacility.getId()));
        
        // 状態は変わらずDRAFTのまま
        Facility facility = facilityRepository.findById(testFacility.getId()).orElseThrow();
        assertEquals(FacilityState.DRAFT, facility.getStatus());
    }

    @Test
    void testRevertToDraftFromFixedStateSucceeds() {
        // FIXED状態からのrevertToDraftは成功する（状態ベース判定）
        
        // FacilityをFIXED状態にする
        facilityService.fixFacility(testFacility.getId());
        Facility fixedFacility = facilityRepository.findById(testFacility.getId()).orElseThrow();
        assertEquals(FacilityState.ACTIVE, fixedFacility.getStatus());
        
        // revertToDraftは成功する（Cross-Context依存を排除したため）
        assertDoesNotThrow(() -> facilityService.revertToDraft(testFacility.getId()));
        
        // DRAFT状態に戻ったことを確認
        Facility revertedFacility = facilityRepository.findById(testFacility.getId()).orElseThrow();
        assertEquals(FacilityState.DRAFT, revertedFacility.getStatus());
        assertTrue(revertedFacility.canBeModified());
    }
}