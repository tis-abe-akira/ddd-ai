package com.example.syndicatelending.common.statemachine.party;

/**
 * Borrower（借り手）の状態を表すenum
 * 
 * シンジケートローンにおける借り手のライフサイクル管理を実現する。
 * Facility組成により重要情報の変更を制限し、データ整合性を保つ。
 */
public enum BorrowerState {
    /**
     * アクティブ状態（作成直後・全フィールド変更可能）
     * - companyId, creditRating, creditLimit含め全て変更可能
     * - Facility未組成の状態
     */
    ACTIVE,
    
    /**
     * 制限状態（Facility組成後・一部フィールド変更禁止）
     * - companyId, creditRating, creditLimit は変更不可
     * - name, email, phoneNumber は変更可能
     * - Facility組成済みの状態
     */
    RESTRICTED
}