package com.example.syndicatelending.common.statemachine.handlers;

import com.example.syndicatelending.common.statemachine.events.PaymentCreatedEvent;
import com.example.syndicatelending.common.statemachine.events.PaymentCancelledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Paymentライフサイクルイベントハンドラー
 * 
 * PaymentCreatedEventを処理し、関連するエンティティの状態管理を行う。
 * 現在の実装では、Drawdownの保護はDrawdownService内で直接チェックしているため、
 * このハンドラーは主にログ記録と将来的な拡張のための基盤として機能する。
 * 
 * 将来的な拡張例：
 * - Loanの状態更新
 * - 通知システムとの連携
 * - 監査ログの記録
 * - 複雑なビジネスルール適用
 */
@Component
public class PaymentLifecycleEventHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentLifecycleEventHandler.class);
    
    /**
     * Payment作成イベントの処理
     * 
     * @param event PaymentCreatedEvent
     */
    @EventListener
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        logger.info("Payment created event received: loanId={}, paymentId={}, facilityId={}, isScheduled={}", 
                   event.getLoanId(), event.getPaymentId(), event.getFacilityId(), event.isScheduledPayment());
        
        // 現在の実装では、Drawdownの保護はDrawdownService内で直接実装
        // このイベントハンドラーは将来的な拡張のための基盤として機能
        
        if (event.isScheduledPayment()) {
            logger.debug("Scheduled payment executed for PaymentDetail: {}", event.getPaymentDetailId());
            // PaymentDetail基づく支払いの場合の追加処理（将来的な拡張ポイント）
        } else {
            logger.debug("Manual payment created for Loan: {}", event.getLoanId());
            // 手動支払いの場合の追加処理（将来的な拡張ポイント）
        }
        
        // 注意: 現在のアーキテクチャでは、Drawdownの保護ガードは
        // DrawdownService.deleteDrawdown()およびupdateDrawdown()内で
        // PaymentRepositoryを直接チェックすることで実装されている。
        // 
        // この設計により、以下の利点がある：
        // 1. 同期的な検証（削除・更新時点での即座なチェック）
        // 2. トランザクション境界内での一貫性保証
        // 3. シンプルで理解しやすい実装
        
        logger.debug("Payment lifecycle event processing completed for payment: {}", event.getPaymentId());
    }

    /**
     * Payment取り消しイベントの処理
     * 
     * @param event PaymentCancelledEvent
     */
    @EventListener
    public void handlePaymentCancelled(PaymentCancelledEvent event) {
        logger.info("Payment cancelled event received: loanId={}, paymentId={}, facilityId={}, isScheduled={}", 
                   event.getLoanId(), event.getPaymentId(), event.getFacilityId(), event.isScheduledPaymentCancellation());
        
        // 現在の実装では、Drawdownの保護状態は取り消し時点で自動的に再評価される
        // DrawdownService.hasPayments()メソッドが最新の状態をチェックするため、
        // 特別な保護解除処理は不要。
        
        if (event.isScheduledPaymentCancellation()) {
            logger.debug("Scheduled payment cancelled for PaymentDetail: {}", event.getPaymentDetailId());
            // PaymentDetail基づく支払い取り消しの場合の追加処理（将来的な拡張ポイント）
        } else {
            logger.debug("Manual payment cancelled for Loan: {}", event.getLoanId());
            // 手動支払い取り消しの場合の追加処理（将来的な拡張ポイント）
        }
        
        // 注意: Event+StateMachineパターンの一貫性のため、PaymentCancelledEventを追加
        // これにより、PaymentCreatedEvent/PaymentCancelledEventの対称性を保持し、
        // 将来的な拡張（通知、監査ログ、複雑なビジネスルール）に対応可能
        
        logger.debug("Payment cancellation event processing completed for payment: {}", event.getPaymentId());
    }
}