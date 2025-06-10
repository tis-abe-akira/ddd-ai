package com.example.syndicatelending.common.statemachine;

import com.example.syndicatelending.common.statemachine.facility.FacilityState;
import com.example.syndicatelending.common.statemachine.facility.FacilityEvent;
import com.example.syndicatelending.common.statemachine.party.BorrowerState;
import com.example.syndicatelending.common.statemachine.party.BorrowerEvent;
import com.example.syndicatelending.common.statemachine.party.InvestorState;
import com.example.syndicatelending.common.statemachine.party.InvestorEvent;
import com.example.syndicatelending.party.entity.Borrower;
import com.example.syndicatelending.party.entity.Investor;
import com.example.syndicatelending.party.repository.BorrowerRepository;
import com.example.syndicatelending.party.repository.InvestorRepository;
import com.example.syndicatelending.syndicate.entity.Syndicate;
import com.example.syndicatelending.syndicate.repository.SyndicateRepository;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.entity.SharePie;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * エンティティ間の状態管理を統合的に行うサービス
 * 
 * Facility組成時のBorrower/Investor状態遷移を管理し、
 * StateMachineとビジネスフローの整合性を保つ。
 */
@Service
@Transactional
public class EntityStateService {

    private final BorrowerRepository borrowerRepository;
    private final InvestorRepository investorRepository;
    private final SyndicateRepository syndicateRepository;
    
    private final StateMachine<BorrowerState, BorrowerEvent> borrowerStateMachine;
    private final StateMachine<InvestorState, InvestorEvent> investorStateMachine;

    public EntityStateService(
            BorrowerRepository borrowerRepository,
            InvestorRepository investorRepository, 
            SyndicateRepository syndicateRepository,
            @Qualifier("borrowerStateMachine") StateMachine<BorrowerState, BorrowerEvent> borrowerStateMachine,
            @Qualifier("investorStateMachine") StateMachine<InvestorState, InvestorEvent> investorStateMachine) {
        this.borrowerRepository = borrowerRepository;
        this.investorRepository = investorRepository;
        this.syndicateRepository = syndicateRepository;
        this.borrowerStateMachine = borrowerStateMachine;
        this.investorStateMachine = investorStateMachine;
    }

    /**
     * Facility組成時の連鎖的状態変更
     * 
     * 1. Borrowerを ACTIVE → RESTRICTED に遷移
     * 2. 関連するInvestorを ACTIVE → RESTRICTED に遷移
     * 
     * @param facility 組成されたFacility
     */
    public void onFacilityCreated(Facility facility) {
        // Syndicateから関連エンティティを取得
        Syndicate syndicate = syndicateRepository.findById(facility.getSyndicateId())
            .orElseThrow(() -> new IllegalStateException("Syndicate not found: " + facility.getSyndicateId()));

        // 1. Borrower状態遷移
        transitionBorrowerToRestricted(syndicate.getBorrowerId());

        // 2. 関連Investor状態遷移
        List<Long> investorIds = getInvestorIdsFromFacility(facility);
        for (Long investorId : investorIds) {
            transitionInvestorToRestricted(investorId);
        }
    }

    /**
     * BorrowerをRESTRICTED状態に遷移
     * 
     * @param borrowerId Borrower ID
     */
    private void transitionBorrowerToRestricted(Long borrowerId) {
        Borrower borrower = borrowerRepository.findById(borrowerId)
            .orElseThrow(() -> new IllegalStateException("Borrower not found: " + borrowerId));

        // 既にRESTRICTED状態の場合はスキップ
        if (borrower.getStatus() == BorrowerState.RESTRICTED) {
            return;
        }

        // StateMachine実行
        if (executeBorrowerTransition(borrower, BorrowerEvent.FACILITY_PARTICIPATION)) {
            // エンティティ状態更新
            borrower.setStatus(BorrowerState.RESTRICTED);
            borrowerRepository.save(borrower);
        }
    }

    /**
     * InvestorをRESTRICTED状態に遷移
     * 
     * @param investorId Investor ID
     */
    private void transitionInvestorToRestricted(Long investorId) {
        Investor investor = investorRepository.findById(investorId)
            .orElseThrow(() -> new IllegalStateException("Investor not found: " + investorId));

        // 既にRESTRICTED状態の場合はスキップ
        if (investor.getStatus() == InvestorState.RESTRICTED) {
            return;
        }

        // StateMachine実行
        if (executeInvestorTransition(investor, InvestorEvent.FACILITY_PARTICIPATION)) {
            // エンティティ状態更新
            investor.setStatus(InvestorState.RESTRICTED);
            investorRepository.save(investor);
        }
    }

    /**
     * Borrower StateMachine遷移実行
     * 
     * @param borrower Borrowerエンティティ
     * @param event 発火イベント
     * @return 遷移成功時 true
     */
    private boolean executeBorrowerTransition(Borrower borrower, BorrowerEvent event) {
        try {
            // StateMachineを現在状態に設定
            borrowerStateMachine.getStateMachineAccessor().doWithAllRegions(access -> {
                access.resetStateMachine(null);
            });

            // 現在状態を設定（ACTIVE状態から開始）
            borrowerStateMachine.getExtendedState().getVariables().put("borrowerId", borrower.getId());
            
            // イベント送信
            return borrowerStateMachine.sendEvent(event);
        } catch (Exception e) {
            throw new IllegalStateException("Borrower state transition failed for ID: " + borrower.getId(), e);
        }
    }

    /**
     * Investor StateMachine遷移実行
     * 
     * @param investor Investorエンティティ
     * @param event 発火イベント
     * @return 遷移成功時 true
     */
    private boolean executeInvestorTransition(Investor investor, InvestorEvent event) {
        try {
            // StateMachineを現在状態に設定
            investorStateMachine.getStateMachineAccessor().doWithAllRegions(access -> {
                access.resetStateMachine(null);
            });

            // 現在状態を設定（ACTIVE状態から開始）
            investorStateMachine.getExtendedState().getVariables().put("investorId", investor.getId());
            
            // イベント送信
            return investorStateMachine.sendEvent(event);
        } catch (Exception e) {
            throw new IllegalStateException("Investor state transition failed for ID: " + investor.getId(), e);
        }
    }

    /**
     * FacilityのSharePieからInvestor IDリストを取得
     * 
     * @param facility Facilityエンティティ
     * @return Investor IDのリスト
     */
    private List<Long> getInvestorIdsFromFacility(Facility facility) {
        return facility.getSharePies().stream()
            .map(SharePie::getInvestorId)
            .distinct()
            .toList();
    }
}