package com.example.syndicatelending.common.statemachine.party;

/**
 * Borrower（借り手）の状態遷移イベントを表すenum
 * 
 * ビジネスイベントに基づく状態変更を制御する。
 */
public enum BorrowerEvent {
    /**
     * Facility参加イベント
     * - Syndicateを通じてFacilityに参加した時に発火
     * - ACTIVE → RESTRICTED への遷移トリガー
     * - 以降、companyId, creditRating, creditLimit変更不可
     */
    FACILITY_PARTICIPATION
}