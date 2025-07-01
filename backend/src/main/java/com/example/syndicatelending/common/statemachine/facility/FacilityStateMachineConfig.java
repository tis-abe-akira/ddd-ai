package com.example.syndicatelending.common.statemachine.facility;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

import java.util.EnumSet;

/**
 * Facilityの状態管理を行うSpring State Machine設定クラス
 * 
 * シンジケートローンにおける融資枠（Facility）のライフサイクル管理を実現する。
 * ドローダウン実行時に融資枠を確定状態に変更し、以降の変更を禁止することで
 * BoundedContext間の整合性を保つ。
 * 
 * 状態遷移：
 * DRAFT（作成直後・変更可能） → FIXED（確定済み・変更不可）
 * 
 * 遷移条件：
 * DRAWDOWN_EXECUTED イベント発生時
 */
@Configuration
@EnableStateMachine
public class FacilityStateMachineConfig extends StateMachineConfigurerAdapter<FacilityState, FacilityEvent> {

    /**
     * 状態の定義と初期状態の設定
     * 
     * @param states 状態設定ビルダー
     * @throws Exception 設定エラー時
     */
    @Override
    public void configure(StateMachineStateConfigurer<FacilityState, FacilityEvent> states) throws Exception {
        states
            .withStates()
            // 初期状態: DRAFT - Facility作成時は変更可能な下書き状態
            .initial(FacilityState.DRAFT)
            // 利用可能な全状態: FacilityState enum の全ての値を登録
            .states(EnumSet.allOf(FacilityState.class));
    }

    /**
     * 状態遷移ルールの定義
     * 
     * ビジネスルール：
     * - ドローダウン実行時にFacilityを確定状態に変更（DRAFT → FIXED）
     * - 確定後は持分比率（SharePie）等の変更を禁止
     * - FIXED状態では2度目のドローダウンを禁止
     * - クロスBoundedContext整合性の維持
     * 
     * @param transitions 遷移設定ビルダー
     * @throws Exception 設定エラー時
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<FacilityState, FacilityEvent> transitions) throws Exception {
        transitions
            .withExternal()
                // 外部遷移: DRAFT状態からFIXED状態への一方向遷移
                .source(FacilityState.DRAFT).target(FacilityState.FIXED)
                // トリガーイベント: ドローダウン実行時に発火
                .event(FacilityEvent.DRAWDOWN_EXECUTED)
                // ガード条件: DRAFT状態でのみドローダウン実行を許可
                .guard(drawdownOnlyFromDraftGuard());
    }

    /**
     * ドローダウン実行制約ガード
     * 
     * FIXED状態での2度目のドローダウンを防ぐビジネスルール制約。
     * Spring State Machineはガード条件が false を返す場合、
     * 遷移を拒否する。
     * 
     * @return ガード条件（DRAFT状態の場合のみ true）
     */
    private Guard<FacilityState, FacilityEvent> drawdownOnlyFromDraftGuard() {
        return context -> {
            // 現在の状態を取得
            FacilityState currentState = context.getStateMachine().getState().getId();
            // DRAFT状態の場合のみドローダウンを許可
            return FacilityState.DRAFT.equals(currentState);
        };
    }
}