package com.example.syndicatelending.common.statemachine.syndicate;

public enum SyndicateEvent {
    FACILITY_CREATED,   // Facility組成実行
    FACILITY_DELETED,   // Facility削除（状態復旧）
    CLOSE               // 将来的な終了イベント（現在は未使用）
}