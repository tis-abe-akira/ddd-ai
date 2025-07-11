package com.example.syndicatelending.common.statemachine.events;

/**
 * Payment作成時に発行されるドメインイベント
 * 
 * このイベントは以下の場合に発行される：
 * - PaymentDetailに基づく支払い実行時
 * - 手動Payment作成時
 * 
 * 主な用途：
 * - 関連するDrawdownの保護状態管理
 * - 支払い実行によるビジネスルール制約の適用
 */
public class PaymentCreatedEvent {
    
    /** 支払いが関連するLoanのID */
    private final Long loanId;
    
    /** 作成されたPaymentのID */
    private final Long paymentId;
    
    /** 支払いが関連するFacilityのID */
    private final Long facilityId;
    
    /** PaymentDetailのID（PaymentDetail基づく支払いの場合のみ） */
    private final Long paymentDetailId;
    
    /**
     * PaymentCreatedEventのコンストラクタ
     * 
     * @param loanId 支払いが関連するLoanのID
     * @param paymentId 作成されたPaymentのID
     * @param facilityId 支払いが関連するFacilityのID
     */
    public PaymentCreatedEvent(Long loanId, Long paymentId, Long facilityId) {
        this(loanId, paymentId, facilityId, null);
    }
    
    /**
     * PaymentCreatedEventのコンストラクタ（PaymentDetail指定あり）
     * 
     * @param loanId 支払いが関連するLoanのID
     * @param paymentId 作成されたPaymentのID
     * @param facilityId 支払いが関連するFacilityのID
     * @param paymentDetailId PaymentDetailのID（任意）
     */
    public PaymentCreatedEvent(Long loanId, Long paymentId, Long facilityId, Long paymentDetailId) {
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
     * PaymentDetail基づく支払いかどうかを判定
     * 
     * @return PaymentDetail基づく支払いの場合true
     */
    public boolean isScheduledPayment() {
        return paymentDetailId != null;
    }
    
    @Override
    public String toString() {
        return "PaymentCreatedEvent{" +
                "loanId=" + loanId +
                ", paymentId=" + paymentId +
                ", facilityId=" + facilityId +
                ", paymentDetailId=" + paymentDetailId +
                '}';
    }
}