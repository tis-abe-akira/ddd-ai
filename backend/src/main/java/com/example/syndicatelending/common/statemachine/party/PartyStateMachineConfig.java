package com.example.syndicatelending.common.statemachine.party;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import java.util.EnumSet;

/**
 * Party（Borrower/Investor）の状態管理を行うSpring State Machine設定クラス
 * 
 * Facility組成による重要フィールドの変更制御を実現する。
 * Borrower/Investorそれぞれに独立したState Machineを提供。
 */
@Configuration
public class PartyStateMachineConfig {

    /**
     * Borrower用State Machine Bean
     * 
     * @return BorrowerのStateMachine
     * @throws Exception 設定エラー時
     */
    @Bean("borrowerStateMachine")
    public StateMachine<BorrowerState, BorrowerEvent> borrowerStateMachine() throws Exception {
        StateMachineBuilder.Builder<BorrowerState, BorrowerEvent> builder = 
            StateMachineBuilder.builder();

        builder.configureStates()
            .withStates()
            .initial(BorrowerState.DRAFT)
            .states(EnumSet.allOf(BorrowerState.class));

        builder.configureTransitions()
            .withExternal()
                .source(BorrowerState.DRAFT)
                .target(BorrowerState.ACTIVE)
                .event(BorrowerEvent.FACILITY_PARTICIPATION)
                .guard(borrowerFacilityParticipationGuard())
            .and()
            .withExternal()
                .source(BorrowerState.ACTIVE)
                .target(BorrowerState.DRAFT)
                .event(BorrowerEvent.FACILITY_DELETED)
                .guard(borrowerFacilityDeletionGuard());

        return builder.build();
    }


    /**
     * Investor用State Machine Bean
     * 
     * @return InvestorのStateMachine
     * @throws Exception 設定エラー時
     */
    @Bean("investorStateMachine")
    public StateMachine<InvestorState, InvestorEvent> investorStateMachine() throws Exception {
        StateMachineBuilder.Builder<InvestorState, InvestorEvent> builder = 
            StateMachineBuilder.builder();

        builder.configureStates()
            .withStates()
            .initial(InvestorState.DRAFT)
            .states(EnumSet.allOf(InvestorState.class));

        builder.configureTransitions()
            .withExternal()
                .source(InvestorState.DRAFT)
                .target(InvestorState.ACTIVE)
                .event(InvestorEvent.FACILITY_PARTICIPATION)
                .guard(investorFacilityParticipationGuard())
            .and()
            .withExternal()
                .source(InvestorState.ACTIVE)
                .target(InvestorState.DRAFT)
                .event(InvestorEvent.FACILITY_DELETED)
                .guard(investorFacilityDeletionGuard());

        return builder.build();
    }


    /**
     * Borrower Facility参加制約ガード
     * 
     * DRAFT状態からのみFacility参加を許可する。
     * 
     * @return ガード条件（DRAFT状態の場合のみ true）
     */
    private Guard<BorrowerState, BorrowerEvent> borrowerFacilityParticipationGuard() {
        return context -> {
            BorrowerState currentState = context.getStateMachine().getState().getId();
            return BorrowerState.DRAFT.equals(currentState);
        };
    }

    /**
     * Borrower Facility削除制約ガード
     * 
     * ACTIVE状態からのみFacility削除による状態復旧を許可する。
     * 
     * @return ガード条件（ACTIVE状態の場合のみ true）
     */
    private Guard<BorrowerState, BorrowerEvent> borrowerFacilityDeletionGuard() {
        return context -> {
            BorrowerState currentState = context.getStateMachine().getState().getId();
            return BorrowerState.ACTIVE.equals(currentState);
        };
    }

    /**
     * Investor Facility参加制約ガード
     * 
     * DRAFT状態からのみFacility参加を許可する。
     * 
     * @return ガード条件（DRAFT状態の場合のみ true）
     */
    private Guard<InvestorState, InvestorEvent> investorFacilityParticipationGuard() {
        return context -> {
            InvestorState currentState = context.getStateMachine().getState().getId();
            return InvestorState.DRAFT.equals(currentState);
        };
    }

    /**
     * Investor Facility削除制約ガード
     * 
     * ACTIVE状態からのみFacility削除による状態復旧を許可する。
     * 
     * @return ガード条件（ACTIVE状態の場合のみ true）
     */
    private Guard<InvestorState, InvestorEvent> investorFacilityDeletionGuard() {
        return context -> {
            InvestorState currentState = context.getStateMachine().getState().getId();
            return InvestorState.ACTIVE.equals(currentState);
        };
    }
}