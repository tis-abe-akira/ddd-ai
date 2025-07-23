package com.example.syndicatelending.common.statemachine.syndicate;

public enum SyndicateState {
    DRAFT,      // 作成直後（Facility組成可能）
    ACTIVE,     // Facility組成済み（確定状態・再組成不可）
    COMPLETED   // 投資完了・契約終了済み（履歴保持のため）
}