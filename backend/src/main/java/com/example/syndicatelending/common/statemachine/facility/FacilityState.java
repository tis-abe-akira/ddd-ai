package com.example.syndicatelending.common.statemachine.facility;

public enum FacilityState {
    DRAFT,      // 作成直後（変更可能）
    ACTIVE,     // Drawdown実行後（変更不可・確定済み）
    COMPLETED   // 全ローン完済後（履歴保持のため）
}