package com.example.syndicatelending.common.statemachine.loan;

/**
 * Loan（貸付）の状態遷移イベントを表すenum
 * 
 * 返済プロセスにおけるビジネスイベントに基づく状態変更を制御する。
 */
public enum LoanEvent {
    /**
     * 初回返済実行イベント
     * - DRAFT → ACTIVE への遷移トリガー
     * - 初回の元本または利息支払い時に発火
     */
    FIRST_PAYMENT,
    
    /**
     * 支払い遅延イベント
     * - ACTIVE → OVERDUE への遷移トリガー
     * - 返済期日を過ぎても未返済の場合に発火
     */
    PAYMENT_OVERDUE,
    
    /**
     * 遅延解消イベント
     * - OVERDUE → ACTIVE への遷移トリガー
     * - 遅延していた支払いを実行した場合に発火
     */
    OVERDUE_RESOLVED,
    
    /**
     * 最終返済実行イベント
     * - ACTIVE/OVERDUE → COMPLETED への遷移トリガー
     * - 全ての元本・利息返済完了時に発火
     */
    FINAL_PAYMENT
}