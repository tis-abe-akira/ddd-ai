package com.example.syndicatelending.common.statemachine.loan;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.guard.Guard;

import java.util.EnumSet;

/**
 * Loan（貸付）の状態管理を行うSpring State Machine設定クラス
 * 
 * 返済プロセスにおける状態遷移を管理し、適切なビジネスルールを強制する。
 * 返済状況に応じた状態変更により、延滞管理等の業務を支援する。
 */
@Configuration
public class LoanStateMachineConfig {

    /**
     * Loan用State Machine Bean
     * 
     * @return LoanのStateMachine
     * @throws Exception 設定エラー時
     */
    @Bean("loanStateMachine")
    public StateMachine<LoanState, LoanEvent> loanStateMachine() throws Exception {
        StateMachineBuilder.Builder<LoanState, LoanEvent> builder = 
            StateMachineBuilder.builder();

        builder.configureStates()
            .withStates()
            .initial(LoanState.DRAFT)
            .states(EnumSet.allOf(LoanState.class));

        builder.configureTransitions()
            .withExternal()
                // DRAFT → ACTIVE (初回返済)
                .source(LoanState.DRAFT)
                .target(LoanState.ACTIVE)
                .event(LoanEvent.FIRST_PAYMENT)
                .guard(firstPaymentGuard())
            .and()
            .withExternal()
                // ACTIVE → OVERDUE (支払い遅延)
                .source(LoanState.ACTIVE)
                .target(LoanState.OVERDUE)
                .event(LoanEvent.PAYMENT_OVERDUE)
                .guard(paymentOverdueGuard())
            .and()
            .withExternal()
                // OVERDUE → ACTIVE (遅延解消)
                .source(LoanState.OVERDUE)
                .target(LoanState.ACTIVE)
                .event(LoanEvent.OVERDUE_RESOLVED)
                .guard(overdueResolvedGuard())
            .and()
            .withExternal()
                // ACTIVE → COMPLETED (最終返済)
                .source(LoanState.ACTIVE)
                .target(LoanState.COMPLETED)
                .event(LoanEvent.FINAL_PAYMENT)
                .guard(finalPaymentGuard())
            .and()
            .withExternal()
                // OVERDUE → COMPLETED (遅延状態からの最終返済)
                .source(LoanState.OVERDUE)
                .target(LoanState.COMPLETED)
                .event(LoanEvent.FINAL_PAYMENT)
                .guard(finalPaymentGuard());

        return builder.build();
    }

    /**
     * 初回返済ガード
     * 
     * DRAFT状態からのみ初回返済を許可する。
     * 
     * @return ガード条件（DRAFT状態の場合のみ true）
     */
    private Guard<LoanState, LoanEvent> firstPaymentGuard() {
        return context -> {
            LoanState currentState = context.getStateMachine().getState().getId();
            return LoanState.DRAFT.equals(currentState);
        };
    }

    /**
     * 支払い遅延ガード
     * 
     * ACTIVE状態からのみ遅延状態への遷移を許可する。
     * 
     * @return ガード条件（ACTIVE状態の場合のみ true）
     */
    private Guard<LoanState, LoanEvent> paymentOverdueGuard() {
        return context -> {
            LoanState currentState = context.getStateMachine().getState().getId();
            return LoanState.ACTIVE.equals(currentState);
        };
    }

    /**
     * 遅延解消ガード
     * 
     * OVERDUE状態からのみ遅延解消を許可する。
     * 
     * @return ガード条件（OVERDUE状態の場合のみ true）
     */
    private Guard<LoanState, LoanEvent> overdueResolvedGuard() {
        return context -> {
            LoanState currentState = context.getStateMachine().getState().getId();
            return LoanState.OVERDUE.equals(currentState);
        };
    }

    /**
     * 最終返済ガード
     * 
     * ACTIVE または OVERDUE状態からのみ完済への遷移を許可する。
     * 
     * @return ガード条件（ACTIVE または OVERDUE状態の場合のみ true）
     */
    private Guard<LoanState, LoanEvent> finalPaymentGuard() {
        return context -> {
            LoanState currentState = context.getStateMachine().getState().getId();
            return LoanState.ACTIVE.equals(currentState) || LoanState.OVERDUE.equals(currentState);
        };
    }
}