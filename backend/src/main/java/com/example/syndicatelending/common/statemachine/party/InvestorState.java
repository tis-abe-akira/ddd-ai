package com.example.syndicatelending.common.statemachine.party;

/**
 * Investor（投資家）の状態を表すenum
 * 
 * シンジケートローンにおける投資家のライフサイクル管理を実現する。
 * Facility組成により重要情報の変更を制限し、データ整合性を保つ。
 */
public enum InvestorState {
    /**
     * 下書き状態（作成直後・全フィールド変更可能）
     * - companyId, investmentCapacity含め全て変更可能
     * - Facility未参加の状態
     */
    DRAFT,
    
    /**
     * アクティブ状態（Facility組成後・一部フィールド変更禁止）
     * - companyId, investmentCapacity は変更不可
     * - name, email, phoneNumber, investorType は変更可能
     * - Facility参加済みの状態
     */
    ACTIVE,
    
    /**
     * 完了状態（投資終了後・変更禁止）
     * - 全フィールド変更不可（履歴保持のため）
     * - 投資完了・契約終了済みの状態
     * - 削除禁止：COMPLETED状態のInvestorは削除不可（履歴保持のため）
     */
    COMPLETED
}