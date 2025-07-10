package com.example.syndicatelending.common.statemachine.managers;

import com.example.syndicatelending.common.statemachine.StateMachineExecutor;
import com.example.syndicatelending.common.statemachine.party.BorrowerState;
import com.example.syndicatelending.common.statemachine.party.BorrowerEvent;
import com.example.syndicatelending.party.entity.Borrower;
import com.example.syndicatelending.party.repository.BorrowerRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Borrower専用状態管理クラス
 * 
 * Borrowerの状態遷移を管理し、既存のStateMachine制約を活用する。
 * 主な責務：
 * - ACTIVE → RESTRICTED 状態遷移（Facility作成時）
 * - RESTRICTED → ACTIVE 状態遷移（Facility削除時）
 * - StateMachine制約の活用
 */
@Component
@Transactional
public class BorrowerStateManager {
    
    private static final Logger logger = LoggerFactory.getLogger(BorrowerStateManager.class);
    
    private final BorrowerRepository borrowerRepository;
    private final StateMachineExecutor stateMachineExecutor;
    private final StateMachine<BorrowerState, BorrowerEvent> borrowerStateMachine;
    
    public BorrowerStateManager(
            BorrowerRepository borrowerRepository,
            StateMachineExecutor stateMachineExecutor,
            @Qualifier("borrowerStateMachine") StateMachine<BorrowerState, BorrowerEvent> borrowerStateMachine) {
        this.borrowerRepository = borrowerRepository;
        this.stateMachineExecutor = stateMachineExecutor;
        this.borrowerStateMachine = borrowerStateMachine;
    }
    
    /**
     * BorrowerをRESTRICTED状態に遷移（Facility作成時）
     * 
     * @param borrowerId Borrower ID
     */
    public void transitionToRestricted(Long borrowerId) {
        logger.info("Starting Borrower state transition to RESTRICTED for ID: {}", borrowerId);
        
        Borrower borrower = borrowerRepository.findById(borrowerId)
            .orElseThrow(() -> new IllegalStateException("Borrower not found: " + borrowerId));
        
        // 事前検証
        stateMachineExecutor.validateCurrentState(borrower.getStatus(), borrowerId, "Borrower");
        
        // 既にRESTRICTED状態の場合はスキップ
        if (!stateMachineExecutor.shouldTransition(
                borrower.getStatus(), BorrowerState.RESTRICTED, borrowerId, "Borrower")) {
            return;
        }
        
        // 状態遷移実行
        boolean success = stateMachineExecutor.executeTransition(
            borrowerStateMachine,
            borrower.getStatus(),
            BorrowerEvent.FACILITY_PARTICIPATION,
            borrowerId,
            "Borrower"
        );
        
        if (success) {
            // エンティティ状態更新
            borrower.setStatus(BorrowerState.RESTRICTED);
            borrowerRepository.save(borrower);
            
            stateMachineExecutor.logTransitionSuccess(
                borrowerId, "Borrower", "ACTIVE", "RESTRICTED");
        } else {
            stateMachineExecutor.logTransitionFailure(
                borrowerId, "Borrower", borrower.getStatus(), BorrowerEvent.FACILITY_PARTICIPATION);
        }
    }
    
    /**
     * BorrowerをACTIVE状態に遷移（Facility削除時）
     * 
     * @param borrowerId Borrower ID
     */
    public void transitionToActive(Long borrowerId) {
        logger.info("Starting Borrower state transition to ACTIVE for ID: {}", borrowerId);
        
        Borrower borrower = borrowerRepository.findById(borrowerId)
            .orElseThrow(() -> new IllegalStateException("Borrower not found: " + borrowerId));
        
        // 事前検証
        stateMachineExecutor.validateCurrentState(borrower.getStatus(), borrowerId, "Borrower");
        
        // 既にACTIVE状態の場合はスキップ
        if (!stateMachineExecutor.shouldTransition(
                borrower.getStatus(), BorrowerState.ACTIVE, borrowerId, "Borrower")) {
            return;
        }
        
        // 状態遷移実行
        boolean success = stateMachineExecutor.executeTransition(
            borrowerStateMachine,
            borrower.getStatus(),
            BorrowerEvent.FACILITY_DELETED,
            borrowerId,
            "Borrower"
        );
        
        if (success) {
            // エンティティ状態更新
            borrower.setStatus(BorrowerState.ACTIVE);
            borrowerRepository.save(borrower);
            
            stateMachineExecutor.logTransitionSuccess(
                borrowerId, "Borrower", "RESTRICTED", "ACTIVE");
        } else {
            stateMachineExecutor.logTransitionFailure(
                borrowerId, "Borrower", borrower.getStatus(), BorrowerEvent.FACILITY_DELETED);
        }
    }
    
    /**
     * Borrowerの現在状態を取得
     * 
     * @param borrowerId Borrower ID
     * @return 現在の状態
     */
    public BorrowerState getCurrentState(Long borrowerId) {
        Borrower borrower = borrowerRepository.findById(borrowerId)
            .orElseThrow(() -> new IllegalStateException("Borrower not found: " + borrowerId));
        return borrower.getStatus();
    }
    
    /**
     * Borrowerが削除可能かチェック
     * 
     * @param borrowerId Borrower ID
     * @return ACTIVE状態の場合 true
     */
    public boolean canBeDeleted(Long borrowerId) {
        BorrowerState currentState = getCurrentState(borrowerId);
        return currentState == BorrowerState.ACTIVE;
    }
}