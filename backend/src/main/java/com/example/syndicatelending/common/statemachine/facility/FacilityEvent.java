package com.example.syndicatelending.common.statemachine.facility;

public enum FacilityEvent {
    DRAWDOWN_EXECUTED,  // Drawdown実行
    REVERT_TO_DRAFT,    // FIXED状態からDRAFT状態への復帰
    FACILITY_DELETED    // Facility削除（状態復旧処理用）
}