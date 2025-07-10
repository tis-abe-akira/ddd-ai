package com.example.syndicatelending.common.statemachine.managers;

import com.example.syndicatelending.common.statemachine.StateMachineExecutor;
import com.example.syndicatelending.common.statemachine.party.InvestorState;
import com.example.syndicatelending.common.statemachine.party.InvestorEvent;
import com.example.syndicatelending.party.entity.Investor;
import com.example.syndicatelending.party.repository.InvestorRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Investor専用状態管理クラス
 * 
 * Investorの状態遷移を管理し、既存のStateMachine制約を活用する。
 * 主な責務：
 * - ACTIVE → RESTRICTED 状態遷移（Facility作成時）
 * - RESTRICTED → ACTIVE 状態遷移（Facility削除時）
 * - StateMachine制約の活用
 */
@Component
@Transactional
public class InvestorStateManager {
    
    private static final Logger logger = LoggerFactory.getLogger(InvestorStateManager.class);
    
    private final InvestorRepository investorRepository;
    private final StateMachineExecutor stateMachineExecutor;
    private final StateMachine<InvestorState, InvestorEvent> investorStateMachine;
    
    public InvestorStateManager(
            InvestorRepository investorRepository,
            StateMachineExecutor stateMachineExecutor,
            @Qualifier("investorStateMachine") StateMachine<InvestorState, InvestorEvent> investorStateMachine) {
        this.investorRepository = investorRepository;
        this.stateMachineExecutor = stateMachineExecutor;
        this.investorStateMachine = investorStateMachine;
    }
    
    /**
     * InvestorをRESTRICTED状態に遷移（Facility作成時）
     * 
     * @param investorId Investor ID
     */
    public void transitionToRestricted(Long investorId) {
        logger.info("Starting Investor state transition to RESTRICTED for ID: {}", investorId);
        
        Investor investor = investorRepository.findById(investorId)
            .orElseThrow(() -> new IllegalStateException("Investor not found: " + investorId));
        
        // 事前検証
        stateMachineExecutor.validateCurrentState(investor.getStatus(), investorId, "Investor");
        
        // 既にRESTRICTED状態の場合はスキップ
        if (!stateMachineExecutor.shouldTransition(
                investor.getStatus(), InvestorState.RESTRICTED, investorId, "Investor")) {
            return;
        }
        
        // 状態遷移実行
        boolean success = stateMachineExecutor.executeTransition(
            investorStateMachine,
            investor.getStatus(),
            InvestorEvent.FACILITY_PARTICIPATION,
            investorId,
            "Investor"
        );
        
        if (success) {
            // エンティティ状態更新
            investor.setStatus(InvestorState.RESTRICTED);
            investorRepository.save(investor);
            
            stateMachineExecutor.logTransitionSuccess(
                investorId, "Investor", "ACTIVE", "RESTRICTED");
        } else {
            stateMachineExecutor.logTransitionFailure(
                investorId, "Investor", investor.getStatus(), InvestorEvent.FACILITY_PARTICIPATION);
        }
    }
    
    /**
     * InvestorをACTIVE状態に遷移（Facility削除時）
     * 
     * @param investorId Investor ID
     */
    public void transitionToActive(Long investorId) {
        logger.info("Starting Investor state transition to ACTIVE for ID: {}", investorId);
        
        Investor investor = investorRepository.findById(investorId)
            .orElseThrow(() -> new IllegalStateException("Investor not found: " + investorId));
        
        // 事前検証
        stateMachineExecutor.validateCurrentState(investor.getStatus(), investorId, "Investor");
        
        // 既にACTIVE状態の場合はスキップ
        if (!stateMachineExecutor.shouldTransition(
                investor.getStatus(), InvestorState.ACTIVE, investorId, "Investor")) {
            return;
        }
        
        // 状態遷移実行
        boolean success = stateMachineExecutor.executeTransition(
            investorStateMachine,
            investor.getStatus(),
            InvestorEvent.FACILITY_DELETED,
            investorId,
            "Investor"
        );
        
        if (success) {
            // エンティティ状態更新
            investor.setStatus(InvestorState.ACTIVE);
            investorRepository.save(investor);
            
            stateMachineExecutor.logTransitionSuccess(
                investorId, "Investor", "RESTRICTED", "ACTIVE");
        } else {
            stateMachineExecutor.logTransitionFailure(
                investorId, "Investor", investor.getStatus(), InvestorEvent.FACILITY_DELETED);
        }
    }
    
    /**
     * Investorの現在状態を取得
     * 
     * @param investorId Investor ID
     * @return 現在の状態
     */
    public InvestorState getCurrentState(Long investorId) {
        Investor investor = investorRepository.findById(investorId)
            .orElseThrow(() -> new IllegalStateException("Investor not found: " + investorId));
        return investor.getStatus();
    }
    
    /**
     * Investorが削除可能かチェック
     * 
     * @param investorId Investor ID
     * @return ACTIVE状態の場合 true
     */
    public boolean canBeDeleted(Long investorId) {
        InvestorState currentState = getCurrentState(investorId);
        return currentState == InvestorState.ACTIVE;
    }
}