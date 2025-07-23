package com.example.syndicatelending.common.statemachine.managers;

import com.example.syndicatelending.common.statemachine.StateMachineExecutor;
import com.example.syndicatelending.common.statemachine.facility.FacilityState;
import com.example.syndicatelending.common.statemachine.facility.FacilityEvent;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.repository.FacilityRepository;
import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facility専用状態管理クラス
 * 
 * Facilityの状態遷移を管理し、既存のStateMachine制約を活用する。
 * 主な責務：
 * - DRAFT → FIXED 状態遷移（Drawdown作成時）
 * - FIXED → DRAFT 状態遷移（Drawdown削除時）
 * - StateMachine制約の活用（Guard条件、遷移ルール）
 * - ビジネスルール検証
 */
@Component
@Transactional
public class FacilityStateManager {
    
    private static final Logger logger = LoggerFactory.getLogger(FacilityStateManager.class);
    
    private final FacilityRepository facilityRepository;
    private final StateMachineExecutor stateMachineExecutor;
    private final StateMachine<FacilityState, FacilityEvent> facilityStateMachine;
    
    public FacilityStateManager(
            FacilityRepository facilityRepository,
            StateMachineExecutor stateMachineExecutor,
            @Qualifier("facilityStateMachine") StateMachine<FacilityState, FacilityEvent> facilityStateMachine) {
        this.facilityRepository = facilityRepository;
        this.stateMachineExecutor = stateMachineExecutor;
        this.facilityStateMachine = facilityStateMachine;
    }
    
    /**
     * FacilityをFIXED状態に遷移（Drawdown作成時）
     * 
     * @param facilityId Facility ID
     * @throws BusinessRuleViolationException 2度目のドローダウン等のビジネスルール違反
     */
    public void transitionToFixed(Long facilityId) {
        logger.info("Starting Facility state transition to FIXED for ID: {}", facilityId);
        
        Facility facility = facilityRepository.findById(facilityId)
            .orElseThrow(() -> new IllegalStateException("Facility not found: " + facilityId));
        
        // 事前検証
        stateMachineExecutor.validateCurrentState(facility.getStatus(), facilityId, "Facility");
        
        // 既にFIXED状態の場合は2度目のドローダウンを禁止
        if (facility.getStatus() == FacilityState.ACTIVE) {
            throw new BusinessRuleViolationException(
                "FIXED状態のFacilityに対して2度目のドローダウンはできません。現在の状態: " + facility.getStatus());
        }
        
        // 状態遷移実行
        boolean success = stateMachineExecutor.executeTransition(
            facilityStateMachine,
            facility.getStatus(),
            FacilityEvent.DRAWDOWN_EXECUTED,
            facilityId,
            "Facility"
        );
        
        if (success) {
            // エンティティ状態更新
            facility.setStatus(FacilityState.ACTIVE);
            facilityRepository.save(facility);
            
            stateMachineExecutor.logTransitionSuccess(
                facilityId, "Facility", "DRAFT", "FIXED");
        } else {
            stateMachineExecutor.logTransitionFailure(
                facilityId, "Facility", facility.getStatus(), FacilityEvent.DRAWDOWN_EXECUTED);
            throw new BusinessRuleViolationException(
                "Facility状態遷移に失敗しました。現在の状態: " + facility.getStatus());
        }
    }
    
    /**
     * FacilityをDRAFT状態に遷移（Drawdown削除時）
     * 
     * @param facilityId Facility ID
     */
    public void transitionToDraft(Long facilityId) {
        logger.info("Starting Facility state transition to DRAFT for ID: {}", facilityId);
        
        Facility facility = facilityRepository.findById(facilityId)
            .orElseThrow(() -> new IllegalStateException("Facility not found: " + facilityId));
        
        // 事前検証
        stateMachineExecutor.validateCurrentState(facility.getStatus(), facilityId, "Facility");
        
        // 既にDRAFT状態の場合はスキップ
        if (!stateMachineExecutor.shouldTransition(
                facility.getStatus(), FacilityState.DRAFT, facilityId, "Facility")) {
            return;
        }
        
        // 状態遷移実行
        boolean success = stateMachineExecutor.executeTransition(
            facilityStateMachine,
            facility.getStatus(),
            FacilityEvent.REVERT_TO_DRAFT,
            facilityId,
            "Facility"
        );
        
        if (success) {
            // エンティティ状態更新
            facility.setStatus(FacilityState.DRAFT);
            facilityRepository.save(facility);
            
            stateMachineExecutor.logTransitionSuccess(
                facilityId, "Facility", "FIXED", "DRAFT");
        } else {
            stateMachineExecutor.logTransitionFailure(
                facilityId, "Facility", facility.getStatus(), FacilityEvent.REVERT_TO_DRAFT);
            
            // DRAFT状態復帰の失敗は警告レベル（通常はビジネスルール検証済み）
            logger.warn("Facility state transition to DRAFT failed for ID: {}, current state: {}", 
                       facilityId, facility.getStatus());
        }
    }
    
    /**
     * Facilityの現在状態を取得
     * 
     * @param facilityId Facility ID
     * @return 現在の状態
     */
    public FacilityState getCurrentState(Long facilityId) {
        Facility facility = facilityRepository.findById(facilityId)
            .orElseThrow(() -> new IllegalStateException("Facility not found: " + facilityId));
        return facility.getStatus();
    }
    
    /**
     * Facilityが変更可能かチェック
     * 
     * @param facilityId Facility ID
     * @return DRAFT状態の場合 true
     */
    public boolean canBeModified(Long facilityId) {
        FacilityState currentState = getCurrentState(facilityId);
        return currentState == FacilityState.DRAFT;
    }
    
    /**
     * Facilityが確定済みかチェック
     * 
     * @param facilityId Facility ID
     * @return FIXED状態の場合 true
     */
    public boolean isFixed(Long facilityId) {
        FacilityState currentState = getCurrentState(facilityId);
        return currentState == FacilityState.ACTIVE;
    }
}