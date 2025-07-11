package com.example.syndicatelending.common.statemachine.party;

/**
 * Borrower（借り手）の状態を表すenum
 * 
 * シンジケートローンにおける借り手のライフサイクル管理を実現する。
 * Facility組成により重要情報の変更を制限し、データ整合性を保つ。
 * 
 * Borrowerは法人格・個人として継続的に存在するため、2状態のみを持つ：
 * - DRAFT: Facility未参加（削除可能、自由に変更可能）
 * - ACTIVE: Facility参加中（削除不可、重要情報変更制限）
 */
public enum BorrowerState {
    /**
     * 下書き状態（作成直後・全フィールド変更可能）
     * - companyId, creditRating, creditLimit含め全て変更可能
     * - Facility未組成の状態
     * - 削除可能
     */
    DRAFT,
    
    /**
     * アクティブ状態（Facility組成後・一部フィールド変更可能）
     * - 基本情報は変更可能、重要情報は制限あり
     * - Facility組成済みの状態
     * - 削除不可（Facility参加中のため）
     */
    ACTIVE
}