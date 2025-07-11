package com.example.syndicatelending.common.statemachine.party;

/**
 * Investor（投資家）の状態を表すenum
 * 
 * シンジケートローンにおける投資家のライフサイクル管理を実現する。
 * Facility組成により重要情報の変更を制限し、データ整合性を保つ。
 * 
 * Investorは法人格・個人として継続的に存在するため、2状態のみを持つ：
 * - DRAFT: Facility未参加（削除可能、自由に変更可能）
 * - ACTIVE: Facility参加中（削除不可、重要情報変更制限）
 */
public enum InvestorState {
    /**
     * 下書き状態（作成直後・全フィールド変更可能）
     * - companyId, investmentCapacity含め全て変更可能
     * - Facility未参加の状態
     * - 削除可能
     */
    DRAFT,
    
    /**
     * アクティブ状態（Facility組成後・一部フィールド変更禁止）
     * - companyId, investmentCapacity は変更不可
     * - name, email, phoneNumber, investorType は変更可能
     * - Facility参加済みの状態
     * - 削除不可（Facility参加中のため）
     */
    ACTIVE
}