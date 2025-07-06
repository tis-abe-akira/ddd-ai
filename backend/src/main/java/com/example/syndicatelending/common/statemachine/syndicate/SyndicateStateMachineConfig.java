package com.example.syndicatelending.common.statemachine.syndicate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.guard.Guard;

import java.util.EnumSet;

/**
 * Syndicateの状態管理を行うSpring State Machine設定クラス
 * 
 * シンジケートローンにおけるシンジケート団のライフサイクル管理を実現する。
 * Facility組成時にシンジケートを確定状態に変更し、重複組成を防止することで
 * 1 Syndicate = 1 Facility の業務制約を維持する。
 * 
 * 状態遷移：
 * DRAFT（作成直後・Facility組成可能） → ACTIVE（組成済み・確定状態）
 * 
 * 遷移条件：
 * FACILITY_CREATED イベント発生時
 */
@Configuration
public class SyndicateStateMachineConfig {

    /**
     * Syndicate用State Machine Bean
     * 
     * @return SyndicateのStateMachine
     * @throws Exception 設定エラー時
     */
    @Bean("syndicateStateMachine")
    public StateMachine<SyndicateState, SyndicateEvent> syndicateStateMachine() throws Exception {
        StateMachineBuilder.Builder<SyndicateState, SyndicateEvent> builder = 
            StateMachineBuilder.builder();

        builder.configureStates()
            .withStates()
            // 初期状態: DRAFT - Syndicate作成時はFacility組成可能な状態
            .initial(SyndicateState.DRAFT)
            // 利用可能な全状態: SyndicateState enum の全ての値を登録
            .states(EnumSet.allOf(SyndicateState.class));

        builder.configureTransitions()
            .withExternal()
                // 外部遷移: DRAFT状態からACTIVE状態への一方向遷移
                .source(SyndicateState.DRAFT).target(SyndicateState.ACTIVE)
                // トリガーイベント: Facility組成時に発火
                .event(SyndicateEvent.FACILITY_CREATED)
                // ガード条件: DRAFT状態でのみFacility組成を許可
                .guard(facilityCreationOnlyFromDraftGuard());

        return builder.build();
    }

    /**
     * Facility組成制約ガード
     * 
     * ACTIVE状態での重複Facility組成を防ぐビジネスルール制約。
     * Spring State Machineはガード条件が false を返す場合、
     * 遷移を拒否する。
     * 
     * @return ガード条件（DRAFT状態の場合のみ true）
     */
    private Guard<SyndicateState, SyndicateEvent> facilityCreationOnlyFromDraftGuard() {
        return context -> {
            // 現在の状態を取得
            SyndicateState currentState = context.getStateMachine().getState().getId();
            // DRAFT状態の場合のみFacility組成を許可
            return SyndicateState.DRAFT.equals(currentState);
        };
    }
}