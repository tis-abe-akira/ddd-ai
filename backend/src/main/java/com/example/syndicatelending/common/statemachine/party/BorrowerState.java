package com.example.syndicatelending.common.statemachine.party;

/**
 * Borrower（借り手）の状態を表すenum
 * 
 * シンジケートローンにおける借り手のライフサイクル管理を実現する。
 * Facility組成により重要情報の変更を制限し、データ整合性を保つ。
 */
public enum BorrowerState {
    /**
     * 下書き状態（作成直後・全フィールド変更可能）
     * - companyId, creditRating, creditLimit含め全て変更可能
     * - Facility未組成の状態
     */
    DRAFT,
    
    /**
     * アクティブ状態（Facility組成後・一部フィールド変更可能）
     * - 基本情報は変更可能、重要情報は制限あり
     * - Facility組成済みの状態
     */
    ACTIVE,
    
    /**
     * 完了状態（ローン完済後・変更禁止）
     * - 全フィールド変更不可（履歴保持のため）
     * - ローン完済・契約終了済みの状態
     * - 削除禁止：COMPLETED状態のBorrowerは削除不可（履歴保持のため）
     */
    COMPLETED
}