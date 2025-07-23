package com.example.syndicatelending.common.statemachine.managers;

import com.example.syndicatelending.common.statemachine.StateMachineExecutor;
import com.example.syndicatelending.common.statemachine.syndicate.SyndicateState;
import com.example.syndicatelending.common.statemachine.syndicate.SyndicateEvent;
import com.example.syndicatelending.syndicate.entity.Syndicate;
import com.example.syndicatelending.syndicate.repository.SyndicateRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Syndicate専用状態管理クラス
 * 
 * Syndicateの状態遷移を管理し、既存のStateMachine制約を活用する。
 * 主な責務：
 * - DRAFT → ACTIVE 状態遷移（Facility作成時）
 * - ACTIVE → DRAFT 状態遷移（Facility削除時）
 * - StateMachine制約の活用
 */
@Component
@Transactional
public class SyndicateStateManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SyndicateStateManager.class);
    
    private final SyndicateRepository syndicateRepository;
    private final StateMachineExecutor stateMachineExecutor;
    private final StateMachine<SyndicateState, SyndicateEvent> syndicateStateMachine;
    
    public SyndicateStateManager(
            SyndicateRepository syndicateRepository,
            StateMachineExecutor stateMachineExecutor,
            @Qualifier("syndicateStateMachine") StateMachine<SyndicateState, SyndicateEvent> syndicateStateMachine) {
        this.syndicateRepository = syndicateRepository;
        this.stateMachineExecutor = stateMachineExecutor;
        this.syndicateStateMachine = syndicateStateMachine;
    }
    
    /**
     * SyndicateをACTIVE状態に遷移（Facility作成時）
     * 
     * @param syndicateId Syndicate ID
     */
    public void transitionToActive(Long syndicateId) {
        logger.info("Starting Syndicate state transition to ACTIVE for ID: {}", syndicateId);
        
        Syndicate syndicate = syndicateRepository.findById(syndicateId)
            .orElseThrow(() -> new IllegalStateException("Syndicate not found: " + syndicateId));
        
        // 事前検証
        stateMachineExecutor.validateCurrentState(syndicate.getStatus(), syndicateId, "Syndicate");
        
        // 既にACTIVE状態の場合はスキップ
        if (!stateMachineExecutor.shouldTransition(
                syndicate.getStatus(), SyndicateState.ACTIVE, syndicateId, "Syndicate")) {
            return;
        }
        
        // 状態遷移実行（簡略化されたビジネスルール検証）
        boolean success = executeSimplifiedTransition(
            syndicate, SyndicateEvent.FACILITY_CREATED, SyndicateState.ACTIVE);
        
        if (success) {
            // エンティティ状態更新
            syndicate.setStatus(SyndicateState.ACTIVE);
            syndicateRepository.save(syndicate);
            
            stateMachineExecutor.logTransitionSuccess(
                syndicateId, "Syndicate", "DRAFT", "ACTIVE");
        } else {
            stateMachineExecutor.logTransitionFailure(
                syndicateId, "Syndicate", syndicate.getStatus(), SyndicateEvent.FACILITY_CREATED);
        }
    }
    
    /**
     * SyndicateをDRAFT状態に遷移（Facility削除時）
     * 
     * @param syndicateId Syndicate ID
     */
    public void transitionToDraft(Long syndicateId) {
        logger.info("Starting Syndicate state transition to DRAFT for ID: {}", syndicateId);
        
        Syndicate syndicate = syndicateRepository.findById(syndicateId)
            .orElseThrow(() -> new IllegalStateException("Syndicate not found: " + syndicateId));
        
        // 事前検証
        stateMachineExecutor.validateCurrentState(syndicate.getStatus(), syndicateId, "Syndicate");
        
        // 既にDRAFT状態の場合はスキップ
        if (!stateMachineExecutor.shouldTransition(
                syndicate.getStatus(), SyndicateState.DRAFT, syndicateId, "Syndicate")) {
            return;
        }
        
        // 状態遷移実行（簡略化されたビジネスルール検証）
        boolean success = executeSimplifiedTransition(
            syndicate, SyndicateEvent.FACILITY_DELETED, SyndicateState.DRAFT);
        
        if (success) {
            // エンティティ状態更新
            syndicate.setStatus(SyndicateState.DRAFT);
            syndicateRepository.save(syndicate);
            
            stateMachineExecutor.logTransitionSuccess(
                syndicateId, "Syndicate", "ACTIVE", "DRAFT");
        } else {
            stateMachineExecutor.logTransitionFailure(
                syndicateId, "Syndicate", syndicate.getStatus(), SyndicateEvent.FACILITY_DELETED);
        }
    }
    
    /**
     * Syndicateの現在状態を取得
     * 
     * @param syndicateId Syndicate ID
     * @return 現在の状態
     */
    public SyndicateState getCurrentState(Long syndicateId) {
        Syndicate syndicate = syndicateRepository.findById(syndicateId)
            .orElseThrow(() -> new IllegalStateException("Syndicate not found: " + syndicateId));
        return syndicate.getStatus();
    }
    
    /**
     * 簡略化された状態遷移実行
     * 
     * 既存のEntityStateServiceでの実装を踏襲し、
     * ビジネスルール検証を簡略化したStateMachine実行
     * 
     * @param syndicate Syndicateエンティティ
     * @param event 発火イベント
     * @param targetState 目標状態
     * @return 遷移成功時 true
     */
    private boolean executeSimplifiedTransition(
            Syndicate syndicate, SyndicateEvent event, SyndicateState targetState) {
        
        // 基本的な遷移条件チェック
        boolean canTransition = false;
        
        if (event == SyndicateEvent.FACILITY_CREATED && 
            syndicate.getStatus() == SyndicateState.DRAFT) {
            canTransition = true;
        } else if (event == SyndicateEvent.FACILITY_DELETED && 
                   syndicate.getStatus() == SyndicateState.ACTIVE) {
            canTransition = true;
        }
        
        if (!canTransition) {
            logger.warn("Invalid Syndicate transition: Cannot execute {} from state {} for ID {}", 
                       event, syndicate.getStatus(), syndicate.getId());
            return false;
        }
        
        logger.info("Syndicate transition validation passed for ID {}: {} -> {}", 
                   syndicate.getId(), syndicate.getStatus(), targetState);
        
        // 既存のEntityStateServiceの実装を踏襲し、
        // ビジネスルール検証が完了している前提で遷移を許可
        return true;
    }
}