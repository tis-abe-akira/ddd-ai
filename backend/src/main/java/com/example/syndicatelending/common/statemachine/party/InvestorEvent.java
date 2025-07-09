package com.example.syndicatelending.common.statemachine.party;

/**
 * Investor（投資家）の状態遷移イベントを表すenum
 * 
 * ビジネスイベントに基づく状態変更を制御する。
 */
public enum InvestorEvent {
    /**
     * Facility参加イベント
     * - SharePieを通じてFacilityに参加した時に発火
     * - ACTIVE → RESTRICTED への遷移トリガー
     * - 以降、companyId, investmentCapacity変更不可
     */
    FACILITY_PARTICIPATION,

    /**
     * Facility削除イベント
     * - 参加していたFacilityが削除された時に発火
     * - RESTRICTED → ACTIVE への遷移トリガー
     * - 制限状態が解除され、重要フィールドの変更が再び可能になる
     */
    FACILITY_DELETED
}