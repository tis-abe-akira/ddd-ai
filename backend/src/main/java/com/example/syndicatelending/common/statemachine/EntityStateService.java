package com.example.syndicatelending.common.statemachine;

import com.example.syndicatelending.common.statemachine.party.BorrowerState;
import com.example.syndicatelending.common.statemachine.party.BorrowerEvent;
import com.example.syndicatelending.common.statemachine.party.InvestorState;
import com.example.syndicatelending.common.statemachine.party.InvestorEvent;
import com.example.syndicatelending.common.statemachine.syndicate.SyndicateState;
import com.example.syndicatelending.common.statemachine.syndicate.SyndicateEvent;
import com.example.syndicatelending.party.entity.Borrower;
import com.example.syndicatelending.party.entity.Investor;
import com.example.syndicatelending.party.repository.BorrowerRepository;
import com.example.syndicatelending.party.repository.InvestorRepository;
import com.example.syndicatelending.syndicate.entity.Syndicate;
import com.example.syndicatelending.syndicate.repository.SyndicateRepository;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.entity.SharePie;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.common.statemachine.facility.FacilityState;
import com.example.syndicatelending.common.statemachine.facility.FacilityEvent;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * エンティティ間の状態管理を統合的に行うサービス
 * 
 * Facility組成時のBorrower/Investor状態遷移を管理し、
 * StateMachineとビジネスフローの整合性を保つ。
 */
@Service
@Transactional
public class EntityStateService {
    
    private static final Logger logger = LoggerFactory.getLogger(EntityStateService.class);

    private final BorrowerRepository borrowerRepository;
    private final InvestorRepository investorRepository;
    private final SyndicateRepository syndicateRepository;
    private final FacilityRepository facilityRepository;
    
    private final StateMachine<BorrowerState, BorrowerEvent> borrowerStateMachine;
    private final StateMachine<InvestorState, InvestorEvent> investorStateMachine;
    private final StateMachine<SyndicateState, SyndicateEvent> syndicateStateMachine;
    private final StateMachine<FacilityState, FacilityEvent> facilityStateMachine;

    public EntityStateService(
            BorrowerRepository borrowerRepository,
            InvestorRepository investorRepository, 
            SyndicateRepository syndicateRepository,
            FacilityRepository facilityRepository,
            @Qualifier("borrowerStateMachine") StateMachine<BorrowerState, BorrowerEvent> borrowerStateMachine,
            @Qualifier("investorStateMachine") StateMachine<InvestorState, InvestorEvent> investorStateMachine,
            @Qualifier("syndicateStateMachine") StateMachine<SyndicateState, SyndicateEvent> syndicateStateMachine,
            @Qualifier("facilityStateMachine") StateMachine<FacilityState, FacilityEvent> facilityStateMachine) {
        this.borrowerRepository = borrowerRepository;
        this.investorRepository = investorRepository;
        this.syndicateRepository = syndicateRepository;
        this.facilityRepository = facilityRepository;
        this.borrowerStateMachine = borrowerStateMachine;
        this.investorStateMachine = investorStateMachine;
        this.syndicateStateMachine = syndicateStateMachine;
        this.facilityStateMachine = facilityStateMachine;
    }

    /**
     * Facility組成時の連鎖的状態変更
     * 
     * 1. Syndicateを DRAFT → ACTIVE に遷移
     * 2. Borrowerを ACTIVE → RESTRICTED に遷移
     * 3. 関連するInvestorを ACTIVE → RESTRICTED に遷移
     * 
     * @param facility 組成されたFacility
     */
    public void onFacilityCreated(Facility facility) {
        // Syndicateから関連エンティティを取得
        Syndicate syndicate = syndicateRepository.findById(facility.getSyndicateId())
            .orElseThrow(() -> new IllegalStateException("Syndicate not found: " + facility.getSyndicateId()));

        // 1. Syndicate状態遷移（DRAFT → ACTIVE）
        transitionSyndicateToActive(syndicate);

        // 2. Borrower状態遷移
        transitionBorrowerToRestricted(syndicate.getBorrowerId());

        // 3. 関連Investor状態遷移
        List<Long> investorIds = getInvestorIdsFromFacility(facility);
        for (Long investorId : investorIds) {
            transitionInvestorToRestricted(investorId);
        }
    }

    /**
     * Facility削除時の連鎖的状態復旧
     * 
     * 1. Syndicateを ACTIVE → DRAFT に遷移
     * 2. Borrowerを RESTRICTED → ACTIVE に遷移
     * 3. 関連するInvestorを RESTRICTED → ACTIVE に遷移
     * 
     * @param facility 削除されるFacility
     */
    public void onFacilityDeleted(Facility facility) {
        logger.info("Starting facility deletion state recovery for facility ID: {}", facility.getId());
        
        // Syndicateから関連エンティティを取得
        Syndicate syndicate = syndicateRepository.findById(facility.getSyndicateId())
            .orElseThrow(() -> new IllegalStateException("Syndicate not found: " + facility.getSyndicateId()));

        // 1. Syndicate状態復旧（ACTIVE → DRAFT）
        transitionSyndicateToDraft(syndicate);

        // 2. Borrower状態復旧
        transitionBorrowerToActive(syndicate.getBorrowerId());

        // 3. 関連Investor状態復旧
        List<Long> investorIds = getInvestorIdsFromFacility(facility);
        for (Long investorId : investorIds) {
            transitionInvestorToActive(investorId);
        }
        
        logger.info("Facility deletion state recovery completed for facility ID: {}", facility.getId());
    }

    /**
     * Drawdown作成時の状態変更処理
     * 
     * Drawdown作成時にFacilityをFIXED状態に遷移させる
     * 
     * @param facilityId 関連するFacility ID
     */
    public void onDrawdownCreated(Long facilityId) {
        logger.info("Starting Drawdown creation state management for facility ID: {}", facilityId);
        
        // FacilityをFIXED状態に遷移
        transitionFacilityToFixed(facilityId);
        
        logger.info("Drawdown creation state management completed for facility ID: {}", facilityId);
    }

    /**
     * Drawdown削除時の状態復旧処理
     * 
     * Drawdown削除時にFacilityをDRAFT状態に復旧させる
     * 
     * @param facilityId 関連するFacility ID
     */
    public void onDrawdownDeleted(Long facilityId) {
        logger.info("Starting Drawdown deletion state recovery for facility ID: {}", facilityId);
        
        // FacilityをDRAFT状態に復旧
        transitionFacilityToDraft(facilityId);
        
        logger.info("Drawdown deletion state recovery completed for facility ID: {}", facilityId);
    }

    /**
     * SyndicateをACTIVE状態に遷移
     * 
     * @param syndicate Syndicateエンティティ
     */
    private void transitionSyndicateToActive(Syndicate syndicate) {
        logger.info("Starting Syndicate state transition for ID: {}, current status: {}", 
                   syndicate.getId(), syndicate.getStatus());
        
        // 既にACTIVE状態の場合はスキップ
        if (syndicate.getStatus() == SyndicateState.ACTIVE) {
            logger.info("Syndicate ID {} is already ACTIVE, skipping transition", syndicate.getId());
            return;
        }

        // StateMachine実行
        boolean success = executeSyndicateTransition(syndicate, SyndicateEvent.FACILITY_CREATED);
        logger.info("Syndicate state machine transition result: {}", success);
        
        if (success) {
            // エンティティ状態更新
            syndicate.setStatus(SyndicateState.ACTIVE);
            syndicateRepository.save(syndicate);
            logger.info("Syndicate ID {} successfully transitioned to ACTIVE status", syndicate.getId());
        } else {
            logger.warn("Failed to transition Syndicate ID {} to ACTIVE status", syndicate.getId());
        }
    }

    /**
     * SyndicateをDRAFT状態に遷移（削除時の状態復旧）
     * 
     * @param syndicate Syndicateエンティティ
     */
    private void transitionSyndicateToDraft(Syndicate syndicate) {
        logger.info("Starting Syndicate state recovery for ID: {}, current status: {}", 
                   syndicate.getId(), syndicate.getStatus());
        
        // 既にDRAFT状態の場合はスキップ
        if (syndicate.getStatus() == SyndicateState.DRAFT) {
            logger.info("Syndicate ID {} is already DRAFT, skipping recovery", syndicate.getId());
            return;
        }

        // StateMachine実行（FACILITY_DELETED イベントで ACTIVE → DRAFT）
        boolean success = executeSyndicateTransition(syndicate, SyndicateEvent.FACILITY_DELETED);
        logger.info("Syndicate state machine recovery result: {}", success);
        
        if (success) {
            // エンティティ状態更新
            syndicate.setStatus(SyndicateState.DRAFT);
            syndicateRepository.save(syndicate);
            logger.info("Syndicate ID {} successfully recovered to DRAFT status", syndicate.getId());
        } else {
            logger.warn("Failed to recover Syndicate ID {} to DRAFT status", syndicate.getId());
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
     * BorrowerをACTIVE状態に遷移（削除時の状態復旧）
     * 
     * @param borrowerId Borrower ID
     */
    private void transitionBorrowerToActive(Long borrowerId) {
        Borrower borrower = borrowerRepository.findById(borrowerId)
            .orElseThrow(() -> new IllegalStateException("Borrower not found: " + borrowerId));

        // 既にACTIVE状態の場合はスキップ
        if (borrower.getStatus() == BorrowerState.ACTIVE) {
            logger.info("Borrower ID {} is already ACTIVE, skipping recovery", borrowerId);
            return;
        }

        logger.info("Starting Borrower state recovery for ID: {}, current status: {}", 
                   borrowerId, borrower.getStatus());

        // StateMachine実行（FACILITY_DELETED イベントで RESTRICTED → ACTIVE）
        if (executeBorrowerTransition(borrower, BorrowerEvent.FACILITY_DELETED)) {
            // エンティティ状態更新
            borrower.setStatus(BorrowerState.ACTIVE);
            borrowerRepository.save(borrower);
            logger.info("Borrower ID {} successfully recovered to ACTIVE status", borrowerId);
        } else {
            logger.warn("Failed to recover Borrower ID {} to ACTIVE status", borrowerId);
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
     * InvestorをACTIVE状態に遷移（削除時の状態復旧）
     * 
     * @param investorId Investor ID
     */
    private void transitionInvestorToActive(Long investorId) {
        Investor investor = investorRepository.findById(investorId)
            .orElseThrow(() -> new IllegalStateException("Investor not found: " + investorId));

        // 既にACTIVE状態の場合はスキップ
        if (investor.getStatus() == InvestorState.ACTIVE) {
            logger.info("Investor ID {} is already ACTIVE, skipping recovery", investorId);
            return;
        }

        logger.info("Starting Investor state recovery for ID: {}, current status: {}", 
                   investorId, investor.getStatus());

        // StateMachine実行（FACILITY_DELETED イベントで RESTRICTED → ACTIVE）
        if (executeInvestorTransition(investor, InvestorEvent.FACILITY_DELETED)) {
            // エンティティ状態更新
            investor.setStatus(InvestorState.ACTIVE);
            investorRepository.save(investor);
            logger.info("Investor ID {} successfully recovered to ACTIVE status", investorId);
        } else {
            logger.warn("Failed to recover Investor ID {} to ACTIVE status", investorId);
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
            logger.info("Executing Borrower state machine transition for ID: {}, event: {}, current status: {}", 
                       borrower.getId(), event, borrower.getStatus());
            
            // エンティティ用の新しいState Machineインスタンスを作成
            StateMachine<BorrowerState, BorrowerEvent> entityStateMachine = createBorrowerStateMachine(borrower);
            
            // State Machineを開始
            entityStateMachine.start();
            
            logger.info("Borrower state machine started for entity state: {}", 
                       entityStateMachine.getState().getId());
            
            // コンテキスト設定
            entityStateMachine.getExtendedState().getVariables().put("borrowerId", borrower.getId());
            
            // イベント送信
            boolean result = entityStateMachine.sendEvent(event);
            
            if (result) {
                BorrowerState newState = entityStateMachine.getState().getId();
                logger.info("Borrower state machine transition successful: {} -> {} for ID {}", 
                           borrower.getStatus(), newState, borrower.getId());
            } else {
                logger.warn("Borrower state machine transition failed for ID {}: cannot execute {} from {}", 
                           borrower.getId(), event, borrower.getStatus());
            }
            
            // State Machine停止
            entityStateMachine.stop();
            
            return result;
            
        } catch (Exception e) {
            logger.error("Borrower state transition failed for ID: {}", borrower.getId(), e);
            throw new IllegalStateException("Borrower state transition failed for ID: " + borrower.getId(), e);
        }
    }
    
    /**
     * Borrower用のState Machineインスタンスを現在のエンティティ状態で作成
     * 
     * @param borrower Borrowerエンティティ
     * @return 現在の状態に設定されたStateMachine
     */
    private StateMachine<BorrowerState, BorrowerEvent> createBorrowerStateMachine(Borrower borrower) {
        try {
            // 新しいState Machineインスタンスを作成
            StateMachine<BorrowerState, BorrowerEvent> machine = borrowerStateMachine;
            
            // State Machineを現在のエンティティ状態に設定
            machine.getStateMachineAccessor().doWithAllRegions(access -> {
                access.resetStateMachine(new org.springframework.statemachine.support.DefaultStateMachineContext<>(
                    borrower.getStatus(), null, null, null));
            });
            
            return machine;
        } catch (Exception e) {
            logger.error("Failed to create Borrower state machine for entity state: {}", borrower.getStatus(), e);
            throw new IllegalStateException("Failed to create Borrower state machine", e);
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
            logger.info("Executing Investor state machine transition for ID: {}, event: {}, current status: {}", 
                       investor.getId(), event, investor.getStatus());
            
            // エンティティ用の新しいState Machineインスタンスを作成
            StateMachine<InvestorState, InvestorEvent> entityStateMachine = createInvestorStateMachine(investor);
            
            // State Machineを開始
            entityStateMachine.start();
            
            logger.info("Investor state machine started for entity state: {}", 
                       entityStateMachine.getState().getId());
            
            // コンテキスト設定
            entityStateMachine.getExtendedState().getVariables().put("investorId", investor.getId());
            
            // イベント送信
            boolean result = entityStateMachine.sendEvent(event);
            
            if (result) {
                InvestorState newState = entityStateMachine.getState().getId();
                logger.info("Investor state machine transition successful: {} -> {} for ID {}", 
                           investor.getStatus(), newState, investor.getId());
            } else {
                logger.warn("Investor state machine transition failed for ID {}: cannot execute {} from {}", 
                           investor.getId(), event, investor.getStatus());
            }
            
            // State Machine停止
            entityStateMachine.stop();
            
            return result;
            
        } catch (Exception e) {
            logger.error("Investor state transition failed for ID: {}", investor.getId(), e);
            throw new IllegalStateException("Investor state transition failed for ID: " + investor.getId(), e);
        }
    }
    
    /**
     * Investor用のState Machineインスタンスを現在のエンティティ状態で作成
     * 
     * @param investor Investorエンティティ
     * @return 現在の状態に設定されたStateMachine
     */
    private StateMachine<InvestorState, InvestorEvent> createInvestorStateMachine(Investor investor) {
        try {
            // 新しいState Machineインスタンスを作成
            StateMachine<InvestorState, InvestorEvent> machine = investorStateMachine;
            
            // State Machineを現在のエンティティ状態に設定
            machine.getStateMachineAccessor().doWithAllRegions(access -> {
                access.resetStateMachine(new org.springframework.statemachine.support.DefaultStateMachineContext<>(
                    investor.getStatus(), null, null, null));
            });
            
            return machine;
        } catch (Exception e) {
            logger.error("Failed to create Investor state machine for entity state: {}", investor.getStatus(), e);
            throw new IllegalStateException("Failed to create Investor state machine", e);
        }
    }

    /**
     * Syndicate StateMachine遷移実行
     * 
     * @param syndicate Syndicateエンティティ
     * @param event 発火イベント
     * @return 遷移成功時 true
     */
    private boolean executeSyndicateTransition(Syndicate syndicate, SyndicateEvent event) {
        try {
            logger.info("Executing Syndicate state machine transition for ID: {}, event: {}, current status: {}", 
                       syndicate.getId(), event, syndicate.getStatus());
            
            // State Machineの制約上、実際のエンティティの状態をチェックして、
            // 適切な遷移かどうかをここで確認する
            boolean canTransition = false;
            
            if (event == SyndicateEvent.FACILITY_CREATED && syndicate.getStatus() == SyndicateState.DRAFT) {
                canTransition = true;
            } else if (event == SyndicateEvent.FACILITY_DELETED && syndicate.getStatus() == SyndicateState.ACTIVE) {
                canTransition = true;
            }
            
            if (!canTransition) {
                logger.warn("Invalid transition: Cannot execute {} from state {} for Syndicate ID {}", 
                           event, syndicate.getStatus(), syndicate.getId());
                return false;
            }
            
            logger.info("Transition validation passed for Syndicate ID {}: {} -> {}", 
                       syndicate.getId(), syndicate.getStatus(), event);
            
            // State Machineを実際に実行する代わりに、
            // ビジネスルールを確認して直接遷移を許可する
            return true;
            
        } catch (Exception e) {
            logger.error("Syndicate state transition failed for ID: {}", syndicate.getId(), e);
            throw new IllegalStateException("Syndicate state transition failed for ID: " + syndicate.getId(), e);
        }
    }

    /**
     * FacilityのSharePieからInvestor IDリストを取得
     * 
     * LeadBankとSharePieの投資家IDを両方含めて返す
     * 
     * @param facility Facilityエンティティ
     * @return Investor IDのリスト（LeadBank含む）
     */
    private List<Long> getInvestorIdsFromFacility(Facility facility) {
        // SyndicateからLeadBank IDを取得
        Syndicate syndicate = syndicateRepository.findById(facility.getSyndicateId())
            .orElseThrow(() -> new IllegalStateException("Syndicate not found: " + facility.getSyndicateId()));
        
        // SharePieからInvestor IDを取得
        List<Long> investorIds = facility.getSharePies().stream()
            .map(SharePie::getInvestorId)
            .distinct()
            .collect(Collectors.toList());
        
        // LeadBank IDを追加（重複を避けるため、まず存在チェック）
        if (syndicate.getLeadBankId() != null && !investorIds.contains(syndicate.getLeadBankId())) {
            investorIds.add(syndicate.getLeadBankId());
        }
        
        return investorIds;
    }

    /**
     * FacilityをFIXED状態に遷移
     * 
     * @param facilityId Facility ID
     */
    private void transitionFacilityToFixed(Long facilityId) {
        Facility facility = facilityRepository.findById(facilityId)
            .orElseThrow(() -> new IllegalStateException("Facility not found: " + facilityId));

        // 既にFIXED状態の場合は2度目のドローダウンを禁止
        if (facility.getStatus() == FacilityState.FIXED) {
            logger.warn("Attempted second drawdown on FIXED facility ID: {}", facilityId);
            throw new com.example.syndicatelending.common.application.exception.BusinessRuleViolationException(
                "FIXED状態のFacilityに対して2度目のドローダウンはできません。現在の状態: " + facility.getStatus());
        }

        logger.info("Starting Facility state transition for ID: {}, current status: {}", 
                   facilityId, facility.getStatus());

        // StateMachine実行
        if (executeFacilityTransition(facility, FacilityEvent.DRAWDOWN_EXECUTED)) {
            // エンティティ状態更新
            facility.setStatus(FacilityState.FIXED);
            facilityRepository.save(facility);
            logger.info("Facility ID {} successfully transitioned to FIXED status", facilityId);
        } else {
            logger.warn("Failed to transition Facility ID {} to FIXED status", facilityId);
        }
    }

    /**
     * FacilityをDRAFT状態に遷移（削除時の状態復旧）
     * 
     * @param facilityId Facility ID
     */
    private void transitionFacilityToDraft(Long facilityId) {
        Facility facility = facilityRepository.findById(facilityId)
            .orElseThrow(() -> new IllegalStateException("Facility not found: " + facilityId));

        // 既にDRAFT状態の場合はスキップ
        if (facility.getStatus() == FacilityState.DRAFT) {
            logger.info("Facility ID {} is already DRAFT, skipping recovery", facilityId);
            return;
        }

        logger.info("Starting Facility state recovery for ID: {}, current status: {}", 
                   facilityId, facility.getStatus());

        // StateMachine実行（REVERT_TO_DRAFT イベントで FIXED → DRAFT）
        if (executeFacilityTransition(facility, FacilityEvent.REVERT_TO_DRAFT)) {
            // エンティティ状態更新
            facility.setStatus(FacilityState.DRAFT);
            facilityRepository.save(facility);
            logger.info("Facility ID {} successfully recovered to DRAFT status", facilityId);
        } else {
            logger.warn("Failed to recover Facility ID {} to DRAFT status", facilityId);
        }
    }

    /**
     * Facility StateMachine遷移実行
     * 
     * @param facility Facilityエンティティ
     * @param event 発火イベント
     * @return 遷移成功時 true
     */
    private boolean executeFacilityTransition(Facility facility, FacilityEvent event) {
        try {
            logger.info("Executing Facility state machine transition for ID: {}, event: {}, current status: {}", 
                       facility.getId(), event, facility.getStatus());
            
            // エンティティ用の新しいState Machineインスタンスを作成
            StateMachine<FacilityState, FacilityEvent> entityStateMachine = createFacilityStateMachine(facility);
            
            // State Machineを開始
            entityStateMachine.start();
            
            logger.info("Facility state machine started for entity state: {}", 
                       entityStateMachine.getState().getId());
            
            // コンテキスト設定
            entityStateMachine.getExtendedState().getVariables().put("facilityId", facility.getId());
            
            // イベント送信
            boolean result = entityStateMachine.sendEvent(event);
            
            if (result) {
                FacilityState newState = entityStateMachine.getState().getId();
                logger.info("Facility state machine transition successful: {} -> {} for ID {}", 
                           facility.getStatus(), newState, facility.getId());
            } else {
                logger.warn("Facility state machine transition failed for ID {}: cannot execute {} from {}", 
                           facility.getId(), event, facility.getStatus());
            }
            
            // State Machine停止
            entityStateMachine.stop();
            
            return result;
            
        } catch (Exception e) {
            logger.error("Facility state transition failed for ID: {}", facility.getId(), e);
            throw new IllegalStateException("Facility state transition failed for ID: " + facility.getId(), e);
        }
    }
    
    /**
     * Facility用のState Machineインスタンスを現在のエンティティ状態で作成
     * 
     * @param facility Facilityエンティティ
     * @return 現在の状態に設定されたStateMachine
     */
    private StateMachine<FacilityState, FacilityEvent> createFacilityStateMachine(Facility facility) {
        try {
            // 新しいState Machineインスタンスを作成
            StateMachine<FacilityState, FacilityEvent> machine = facilityStateMachine;
            
            // State Machineを現在のエンティティ状態に設定
            machine.getStateMachineAccessor().doWithAllRegions(access -> {
                access.resetStateMachine(new org.springframework.statemachine.support.DefaultStateMachineContext<>(
                    facility.getStatus(), null, null, null));
            });
            
            return machine;
        } catch (Exception e) {
            logger.error("Failed to create Facility state machine for entity state: {}", facility.getStatus(), e);
            throw new IllegalStateException("Failed to create Facility state machine", e);
        }
    }
}
