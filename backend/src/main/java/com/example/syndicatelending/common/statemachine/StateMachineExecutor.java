package com.example.syndicatelending.common.statemachine;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 共通StateMachine実行基盤
 * 
 * 各エンティティタイプのStateMachine実行で発生する重複パターンを抽象化し、
 * 統一的なStateMachine実行・作成・管理を提供する。
 * 
 * 主な機能：
 * - エンティティの現在状態に基づくStateMachine作成
 * - 統一的な状態遷移実行
 * - エラーハンドリング
 * - ログ出力
 */
@Component
public class StateMachineExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(StateMachineExecutor.class);

    /**
     * 汎用StateMachine実行メソッド
     * 
     * @param <S> 状態の型（例：FacilityState, BorrowerState等）
     * @param <E> イベントの型（例：FacilityEvent, BorrowerEvent等）
     * @param stateMachine 実行するStateMachine
     * @param currentState エンティティの現在状態
     * @param event 発火するイベント
     * @param entityId エンティティID（ログ用）
     * @param entityType エンティティタイプ（ログ用）
     * @return 遷移成功時 true
     */
    public <S, E> boolean executeTransition(
            StateMachine<S, E> stateMachine,
            S currentState,
            E event,
            Long entityId,
            String entityType) {
        
        try {
            logger.info("Starting {} state machine transition for ID: {}, event: {}, current state: {}", 
                       entityType, entityId, event, currentState);
            
            // StateMachineを現在のエンティティ状態に設定
            resetStateMachineToState(stateMachine, currentState);
            
            // State Machineを開始
            stateMachine.start();
            
            logger.info("{} state machine started for entity state: {}", 
                       entityType, stateMachine.getState().getId());
            
            // コンテキスト設定
            stateMachine.getExtendedState().getVariables().put("entityId", entityId);
            stateMachine.getExtendedState().getVariables().put("entityType", entityType);
            
            // イベント送信
            boolean result = stateMachine.sendEvent(event);
            
            if (result) {
                S newState = stateMachine.getState().getId();
                logger.info("{} state machine transition successful: {} -> {} for ID {}", 
                           entityType, currentState, newState, entityId);
            } else {
                logger.warn("{} state machine transition failed for ID {}: cannot execute {} from {}", 
                           entityType, entityId, event, currentState);
            }
            
            // State Machine停止
            stateMachine.stop();
            
            return result;
            
        } catch (Exception e) {
            logger.error("{} state transition failed for ID: {}", entityType, entityId, e);
            throw new IllegalStateException(
                String.format("%s state transition failed for ID: %d", entityType, entityId), e);
        }
    }

    /**
     * StateMachineを指定された状態にリセット
     * 
     * @param <S> 状態の型
     * @param <E> イベントの型
     * @param stateMachine リセットするStateMachine
     * @param currentState 設定する現在状態
     */
    private <S, E> void resetStateMachineToState(StateMachine<S, E> stateMachine, S currentState) {
        try {
            stateMachine.getStateMachineAccessor().doWithAllRegions(access -> {
                access.resetStateMachine(new DefaultStateMachineContext<>(
                    currentState, null, null, null));
            });
        } catch (Exception e) {
            logger.error("Failed to reset state machine to state: {}", currentState, e);
            throw new IllegalStateException("Failed to reset state machine", e);
        }
    }

    /**
     * エンティティの現在状態を検証
     * 
     * 状態遷移前の基本的な検証を行う
     * 
     * @param currentState 現在状態
     * @param entityId エンティティID
     * @param entityType エンティティタイプ
     * @throws IllegalStateException 状態が不正な場合
     */
    public void validateCurrentState(Object currentState, Long entityId, String entityType) {
        if (currentState == null) {
            throw new IllegalStateException(
                String.format("%s ID %d has null state", entityType, entityId));
        }
        
        logger.debug("Validated {} ID {} current state: {}", entityType, entityId, currentState);
    }

    /**
     * 状態遷移の事前チェック
     * 
     * 既に目標状態にある場合のスキップ判定
     * 
     * @param currentState 現在状態
     * @param targetState 目標状態
     * @param entityId エンティティID
     * @param entityType エンティティタイプ
     * @return 遷移が必要な場合 true、スキップの場合 false
     */
    public boolean shouldTransition(Object currentState, Object targetState, Long entityId, String entityType) {
        if (currentState.equals(targetState)) {
            logger.info("{} ID {} is already in {} state, skipping transition", 
                       entityType, entityId, targetState);
            return false;
        }
        return true;
    }

    /**
     * 状態遷移成功時の共通処理
     * 
     * @param entityId エンティティID
     * @param entityType エンティティタイプ
     * @param oldState 遷移前状態
     * @param newState 遷移後状態
     */
    public void logTransitionSuccess(Long entityId, String entityType, Object oldState, Object newState) {
        logger.info("{} ID {} successfully transitioned from {} to {}", 
                   entityType, entityId, oldState, newState);
    }

    /**
     * 状態遷移失敗時の共通処理
     * 
     * @param entityId エンティティID
     * @param entityType エンティティタイプ
     * @param currentState 現在状態
     * @param event 実行しようとしたイベント
     */
    public void logTransitionFailure(Long entityId, String entityType, Object currentState, Object event) {
        logger.warn("Failed to transition {} ID {} from state {} with event {}", 
                   entityType, entityId, currentState, event);
    }
}