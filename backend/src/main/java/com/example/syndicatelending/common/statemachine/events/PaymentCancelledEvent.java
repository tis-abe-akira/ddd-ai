package com.example.syndicatelending.common.statemachine.events;

/**
 * Payment取り消し時に発行されるドメインイベント
 * 
 * このイベントは以下の場合に発行される：
 * - PaymentService.cancelPayment()による支払い取り消し時
 * 
 * 主な用途：
 * - 関連するDrawdownの保護状態の再評価
 * - 支払い取り消しによるビジネスルール制約の解除判定
 * - Event+StateMachineパターンの一貫性保持
 */
public class PaymentCancelledEvent {
    
    /** 取り消された支払いが関連するLoanのID */
    private final Long loanId;
    
    /** 取り消されたPaymentのID */
    private final Long paymentId;
    
    /** 取り消された支払いが関連するFacilityのID */
    private final Long facilityId;
    
    /** PaymentDetailのID（PaymentDetail基づく支払いの場合のみ） */
    private final Long paymentDetailId;
    
    /**
     * PaymentCancelledEventのコンストラクタ
     * 
     * @param loanId 取り消された支払いが関連するLoanのID
     * @param paymentId 取り消されたPaymentのID
     * @param facilityId 取り消された支払いが関連するFacilityのID
     */
    public PaymentCancelledEvent(Long loanId, Long paymentId, Long facilityId) {
        this(loanId, paymentId, facilityId, null);
    }
    
    /**
     * PaymentCancelledEventのコンストラクタ（PaymentDetail指定あり）
     * 
     * @param loanId 取り消された支払いが関連するLoanのID
     * @param paymentId 取り消されたPaymentのID
     * @param facilityId 取り消された支払いが関連するFacilityのID
     * @param paymentDetailId PaymentDetailのID（任意）
     */
    public PaymentCancelledEvent(Long loanId, Long paymentId, Long facilityId, Long paymentDetailId) {
        this.loanId = loanId;
        this.paymentId = paymentId;
        this.facilityId = facilityId;
        this.paymentDetailId = paymentDetailId;
    }
    
    public Long getLoanId() {
        return loanId;
    }
    
    public Long getPaymentId() {
        return paymentId;
    }
    
    public Long getFacilityId() {
        return facilityId;
    }
    
    public Long getPaymentDetailId() {
        return paymentDetailId;
    }
    
    /**
     * PaymentDetail基づく支払いの取り消しかどうかを判定
     * 
     * @return PaymentDetail基づく支払いの取り消しの場合true
     */
    public boolean isScheduledPaymentCancellation() {
        return paymentDetailId != null;
    }
    
    @Override
    public String toString() {
        return "PaymentCancelledEvent{" +
                "loanId=" + loanId +
                ", paymentId=" + paymentId +
                ", facilityId=" + facilityId +
                ", paymentDetailId=" + paymentDetailId +
                '}';
    }
}