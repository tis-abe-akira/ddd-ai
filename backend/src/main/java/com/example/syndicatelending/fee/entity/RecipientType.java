package com.example.syndicatelending.fee.entity;

/**
 * 手数料受取人タイプ
 * 
 * 手数料の受取人を分類し、適切な配分ロジックを決定するために使用する。
 * ビジネスルールに基づいて、各手数料タイプに対応する受取人を制御する。
 */
public enum RecipientType {
    /**
     * リードバンク
     * - 自動決定：Facilityから取得
     * - 適用手数料：MANAGEMENT_FEE, ARRANGEMENT_FEE
     */
    LEAD_BANK,
    
    /**
     * エージェントバンク
     * - 選択式：銀行リストから選択
     * - 適用手数料：AGENT_FEE, TRANSACTION_FEE
     */
    AGENT_BANK,
    
    /**
     * 投資家
     * - 選択式：投資家リストから選択
     * - 適用手数料：OTHER_FEE（個別支払い）
     */
    INVESTOR,
    
    /**
     * 投資家配分
     * - 自動配分：持分比率に基づいて配分
     * - 適用手数料：COMMITMENT_FEE, LATE_FEE
     */
    AUTO_DISTRIBUTE
}