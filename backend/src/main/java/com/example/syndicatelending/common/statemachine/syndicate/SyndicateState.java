package com.example.syndicatelending.common.statemachine.syndicate;

public enum SyndicateState {
    DRAFT,      // 作成直後（Facility組成可能）
    ACTIVE,     // Facility組成済み（確定状態・再組成不可）
    CLOSED      // 将来的な終了状態（現在は未使用）
}