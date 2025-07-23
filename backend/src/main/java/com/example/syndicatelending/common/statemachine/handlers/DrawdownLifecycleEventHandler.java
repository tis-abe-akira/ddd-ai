package com.example.syndicatelending.common.statemachine.handlers;

import com.example.syndicatelending.common.statemachine.events.DrawdownCreatedEvent;
import com.example.syndicatelending.common.statemachine.events.DrawdownDeletedEvent;
import com.example.syndicatelending.common.statemachine.managers.FacilityStateManager;
import com.example.syndicatelending.common.application.exception.BusinessRuleViolationException;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drawdownライフサイクルイベントハンドラー
 * 
 * Drawdown作成・削除イベントを受信し、関連エンティティの状態変更を
 * オーケストレーションする。
 * 
 * 処理対象イベント：
 * - DrawdownCreatedEvent: Drawdown作成時のFacility状態変更
 * - DrawdownDeletedEvent: Drawdown削除時のFacility状態復旧
 * 
 * このハンドラーは、既存のEntityStateServiceの
 * onDrawdownCreated/onDrawdownDeleted メソッドを
 * イベント軸で分割・再実装したものです。
 */
@Component
@Transactional
public class DrawdownLifecycleEventHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(DrawdownLifecycleEventHandler.class);
    
    private final FacilityStateManager facilityStateManager;
    
    public DrawdownLifecycleEventHandler(FacilityStateManager facilityStateManager) {
        this.facilityStateManager = facilityStateManager;
    }
    
    /**
     * Drawdown作成イベントハンドラー
     * 
     * Drawdown作成時の状態変更を実行：
     * - Facility: DRAFT → FIXED（確定状態に変更）
     * 
     * @param event Drawdown作成イベント
     */
    @EventListener
    public void handleDrawdownCreated(DrawdownCreatedEvent event) {
        logger.info("Handling DrawdownCreatedEvent for facility ID: {}, drawdown ID: {}", 
                   event.getFacilityId(), event.getDrawdownId());
        
        try {
            // FacilityをFIXED状態に遷移
            facilityStateManager.transitionToFixed(event.getFacilityId());
            
            logger.info("Successfully processed DrawdownCreatedEvent for facility ID: {}, drawdown ID: {}", 
                       event.getFacilityId(), event.getDrawdownId());
            
        } catch (Exception e) {
            logger.error("Failed to process DrawdownCreatedEvent for facility ID: {}, drawdown ID: {}", 
                        event.getFacilityId(), event.getDrawdownId(), e);
            // Re-throw BusinessRuleViolationException as-is to maintain proper exception semantics
            if (e instanceof BusinessRuleViolationException) {
                throw (BusinessRuleViolationException) e;
            }
            throw new IllegalStateException("Drawdown creation state management failed", e);
        }
    }
    
    /**
     * Drawdown削除イベントハンドラー
     * 
     * Drawdown削除時の状態復旧を実行：
     * - Facility: FIXED → DRAFT（編集可能状態に復旧）
     * 
     * @param event Drawdown削除イベント
     */
    @EventListener
    public void handleDrawdownDeleted(DrawdownDeletedEvent event) {
        logger.info("Handling DrawdownDeletedEvent for facility ID: {}, drawdown ID: {}", 
                   event.getFacilityId(), event.getDrawdownId());
        
        try {
            // FacilityをDRAFT状態に復旧
            facilityStateManager.transitionToDraft(event.getFacilityId());
            
            logger.info("Successfully processed DrawdownDeletedEvent for facility ID: {}, drawdown ID: {}", 
                       event.getFacilityId(), event.getDrawdownId());
            
        } catch (Exception e) {
            logger.error("Failed to process DrawdownDeletedEvent for facility ID: {}, drawdown ID: {}", 
                        event.getFacilityId(), event.getDrawdownId(), e);
            throw new IllegalStateException("Drawdown deletion state management failed", e);
        }
    }
}