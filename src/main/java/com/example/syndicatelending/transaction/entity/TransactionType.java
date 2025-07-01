package com.example.syndicatelending.transaction.entity;

/**
 * 取引タイプを表すenum
 * 
 * シンジケートローン管理システムにおける全ての取引タイプを定義し、
 * type-safeな取引分類を提供する。
 */
public enum TransactionType {
    /**
     * ドローダウン（資金引出）
     * - 借り手がFacilityから資金を引き出す取引
     * - Loan作成とAmountPie配分を伴う
     */
    DRAWDOWN,
    
    /**
     * 支払い（元本・利息返済）
     * - 借り手からの元本・利息返済取引
     * - PaymentDistribution配分を伴う
     * - 投資家の投資額更新（元本返済時）
     */
    PAYMENT,
    
    /**
     * ファシリティ投資
     * - 投資家のFacility参加による投資記録
     * - SharePie比率での按分投資額を記録
     */
    FACILITY_INVESTMENT,
    
    /**
     * 手数料支払い
     * - 管理手数料、取引手数料等の支払い取引
     * - 手数料タイプ別の配分処理を伴う
     */
    FEE_PAYMENT,
    
    /**
     * 取引（将来拡張用）
     * - ファシリティの売買取引
     * - セカンダリーマーケット取引等
     */
    TRADE,
    
    /**
     * 精算（将来拡張用）
     * - ファシリティ終了時の最終精算
     * - 残余資金の投資家への返還等
     */
    SETTLEMENT
}