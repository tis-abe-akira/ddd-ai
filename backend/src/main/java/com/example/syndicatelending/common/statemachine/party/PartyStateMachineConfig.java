package com.example.syndicatelending.common.statemachine.party;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.guard.Guard;

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
            .initial(BorrowerState.ACTIVE)
            .states(EnumSet.allOf(BorrowerState.class));

        builder.configureTransitions()
            .withExternal()
                .source(BorrowerState.ACTIVE)
                .target(BorrowerState.RESTRICTED)
                .event(BorrowerEvent.FACILITY_PARTICIPATION)
                .guard(borrowerFacilityParticipationGuard());

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
            .initial(InvestorState.ACTIVE)
            .states(EnumSet.allOf(InvestorState.class));

        builder.configureTransitions()
            .withExternal()
                .source(InvestorState.ACTIVE)
                .target(InvestorState.RESTRICTED)
                .event(InvestorEvent.FACILITY_PARTICIPATION)
                .guard(investorFacilityParticipationGuard());

        return builder.build();
    }

    /**
     * Borrower Facility参加制約ガード
     * 
     * ACTIVE状態からのみFacility参加を許可する。
     * 
     * @return ガード条件（ACTIVE状態の場合のみ true）
     */
    private Guard<BorrowerState, BorrowerEvent> borrowerFacilityParticipationGuard() {
        return context -> {
            BorrowerState currentState = context.getStateMachine().getState().getId();
            return BorrowerState.ACTIVE.equals(currentState);
        };
    }

    /**
     * Investor Facility参加制約ガード
     * 
     * ACTIVE状態からのみFacility参加を許可する。
     * 
     * @return ガード条件（ACTIVE状態の場合のみ true）
     */
    private Guard<InvestorState, InvestorEvent> investorFacilityParticipationGuard() {
        return context -> {
            InvestorState currentState = context.getStateMachine().getState().getId();
            return InvestorState.ACTIVE.equals(currentState);
        };
    }
}