package com.example.syndicatelending.facility.statemachine;

public enum FacilityState {
    DRAFT,      // 作成直後（変更可能）
    FIXED       // Drawdown実行後（変更不可・確定済み）
}