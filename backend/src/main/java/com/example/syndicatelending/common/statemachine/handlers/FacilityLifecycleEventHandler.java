package com.example.syndicatelending.common.statemachine.handlers;

import com.example.syndicatelending.common.statemachine.events.FacilityCreatedEvent;
import com.example.syndicatelending.common.statemachine.events.FacilityDeletedEvent;
import com.example.syndicatelending.common.statemachine.managers.SyndicateStateManager;
import com.example.syndicatelending.common.statemachine.managers.BorrowerStateManager;
import com.example.syndicatelending.common.statemachine.managers.InvestorStateManager;
import com.example.syndicatelending.facility.entity.Facility;
import com.example.syndicatelending.facility.entity.SharePie;
import com.example.syndicatelending.syndicate.entity.Syndicate;
import com.example.syndicatelending.syndicate.repository.SyndicateRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Facilityライフサイクルイベントハンドラー
 * 
 * Facility作成・削除イベントを受信し、関連エンティティの状態変更を
 * オーケストレーションする。
 * 
 * 処理対象イベント：
 * - FacilityCreatedEvent: Facility作成時の連鎖状態変更
 * - FacilityDeletedEvent: Facility削除時の連鎖状態復旧
 * 
 * このハンドラーは、既存のEntityStateServiceの
 * onFacilityCreated/onFacilityDeleted メソッドを
 * イベント軸で分割・再実装したものです。
 */
@Component
@Transactional
public class FacilityLifecycleEventHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(FacilityLifecycleEventHandler.class);
    
    private final SyndicateStateManager syndicateStateManager;
    private final BorrowerStateManager borrowerStateManager;
    private final InvestorStateManager investorStateManager;
    private final SyndicateRepository syndicateRepository;
    
    public FacilityLifecycleEventHandler(
            SyndicateStateManager syndicateStateManager,
            BorrowerStateManager borrowerStateManager,
            InvestorStateManager investorStateManager,
            SyndicateRepository syndicateRepository) {
        this.syndicateStateManager = syndicateStateManager;
        this.borrowerStateManager = borrowerStateManager;
        this.investorStateManager = investorStateManager;
        this.syndicateRepository = syndicateRepository;
    }
    
    /**
     * Facility作成イベントハンドラー
     * 
     * Facility組成時の連鎖的状態変更を実行：
     * 1. Syndicate: DRAFT → ACTIVE
     * 2. Borrower: ACTIVE → RESTRICTED
     * 3. Investor: ACTIVE → RESTRICTED（SharePieの全投資家）
     * 
     * @param event Facility作成イベント
     */
    @EventListener
    public void handleFacilityCreated(FacilityCreatedEvent event) {
        logger.info("Handling FacilityCreatedEvent for facility ID: {}", event.getFacilityId());
        
        try {
            Facility facility = event.getFacility();
            
            // Syndicateから関連エンティティを取得
            Syndicate syndicate = syndicateRepository.findById(facility.getSyndicateId())
                .orElseThrow(() -> new IllegalStateException("Syndicate not found: " + facility.getSyndicateId()));
            
            // 1. Syndicate状態遷移（DRAFT → ACTIVE）
            syndicateStateManager.transitionToActive(syndicate.getId());
            
            // 2. Borrower状態遷移（ACTIVE → RESTRICTED）
            borrowerStateManager.transitionToRestricted(syndicate.getBorrowerId());
            
            // 3. 関連Investor状態遷移（ACTIVE → RESTRICTED）
            List<Long> investorIds = getInvestorIdsFromFacility(facility, syndicate);
            for (Long investorId : investorIds) {
                investorStateManager.transitionToRestricted(investorId);
            }
            
            logger.info("Successfully processed FacilityCreatedEvent for facility ID: {}", event.getFacilityId());
            
        } catch (Exception e) {
            logger.error("Failed to process FacilityCreatedEvent for facility ID: {}", 
                        event.getFacilityId(), e);
            throw new IllegalStateException("Facility creation state management failed", e);
        }
    }
    
    /**
     * Facility削除イベントハンドラー
     * 
     * Facility削除時の連鎖的状態復旧を実行：
     * 1. Syndicate: ACTIVE → DRAFT
     * 2. Borrower: RESTRICTED → ACTIVE
     * 3. Investor: RESTRICTED → ACTIVE（SharePieの全投資家）
     * 
     * @param event Facility削除イベント
     */
    @EventListener
    public void handleFacilityDeleted(FacilityDeletedEvent event) {
        logger.info("Handling FacilityDeletedEvent for facility ID: {}", event.getFacilityId());
        
        try {
            Facility facility = event.getFacility();
            
            // Syndicateから関連エンティティを取得
            Syndicate syndicate = syndicateRepository.findById(facility.getSyndicateId())
                .orElseThrow(() -> new IllegalStateException("Syndicate not found: " + facility.getSyndicateId()));
            
            // 1. Syndicate状態復旧（ACTIVE → DRAFT）
            syndicateStateManager.transitionToDraft(syndicate.getId());
            
            // 2. Borrower状態復旧（RESTRICTED → ACTIVE）
            borrowerStateManager.transitionToActive(syndicate.getBorrowerId());
            
            // 3. 関連Investor状態復旧（RESTRICTED → ACTIVE）
            List<Long> investorIds = getInvestorIdsFromFacility(facility, syndicate);
            for (Long investorId : investorIds) {
                investorStateManager.transitionToActive(investorId);
            }
            
            logger.info("Successfully processed FacilityDeletedEvent for facility ID: {}", event.getFacilityId());
            
        } catch (Exception e) {
            logger.error("Failed to process FacilityDeletedEvent for facility ID: {}", 
                        event.getFacilityId(), e);
            throw new IllegalStateException("Facility deletion state management failed", e);
        }
    }
    
    /**
     * FacilityのSharePieからInvestor IDリストを取得
     * 
     * 既存のEntityStateServiceの実装を踏襲し、
     * LeadBankとSharePieの投資家IDを両方含めて返す
     * 
     * @param facility Facilityエンティティ
     * @param syndicate Syndicateエンティティ
     * @return Investor IDのリスト（LeadBank含む）
     */
    private List<Long> getInvestorIdsFromFacility(Facility facility, Syndicate syndicate) {
        // SharePieからInvestor IDを取得
        List<Long> investorIds = facility.getSharePies().stream()
            .map(SharePie::getInvestorId)
            .distinct()
            .collect(Collectors.toList());
        
        // LeadBank IDを追加（重複を避けるため、まず存在チェック）
        if (syndicate.getLeadBankId() != null && !investorIds.contains(syndicate.getLeadBankId())) {
            investorIds.add(syndicate.getLeadBankId());
        }
        
        logger.debug("Extracted {} investor IDs from facility ID: {}", 
                    investorIds.size(), facility.getId());
        
        return investorIds;
    }
}