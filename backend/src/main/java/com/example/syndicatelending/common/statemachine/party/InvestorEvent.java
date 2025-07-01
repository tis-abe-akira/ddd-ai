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
    FACILITY_PARTICIPATION
}